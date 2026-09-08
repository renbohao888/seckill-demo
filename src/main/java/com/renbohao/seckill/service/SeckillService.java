package com.renbohao.seckill.service;

import com.renbohao.seckill.common.Result;
import com.renbohao.seckill.entity.Product;
import com.renbohao.seckill.mapper.ProductMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * 秒杀业务服务：负责"限流 → Redis 预减 → 数据库条件更新兜底 → 落订单"整条链路。
 */
@Slf4j
@Service
public class SeckillService {

    /** 每人限制：1 秒内最多请求 1 次（简单固定窗口限流） */
    private static final int LIMIT_PER_SECOND = 1;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SeckillTxExecutor txExecutor;

    /** 预加载的 Lua 脚本 */
    private final DefaultRedisScript<Long> deductScript = new DefaultRedisScript<>();

    public SeckillService() {
        // 读取 classpath 下的 Lua 脚本内容
        try {
            String lua = new String(
                    new ClassPathResource("lua/seckill.lua").getInputStream().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8);
            deductScript.setScriptText(lua);
            deductScript.setResultType(Long.class);
        } catch (Exception e) {
            throw new IllegalStateException("加载 seckill.lua 失败", e);
        }
    }

    /**
     * 启动时把数据库中的库存预热到 Redis，供 Lua 预减使用。
     * 说明：真实项目可用定时/回调/消息保持 Redis 与 DB 库存一致；此处为教学简化。
     */
    @PostConstruct
    public void initStockToRedis() {
        List<Product> products = productMapper.selectList(null);
        for (Product p : products) {
            String key = stockKey(p.getId());
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().set(key, String.valueOf(p.getStock()));
                log.info("预热库存：商品 {} 库存 {} 已写入 Redis", p.getId(), p.getStock());
            }
        }
    }

    /**
     * 秒杀下单主流程。
     *
     * @param productId 商品 id
     * @param userId    用户 id
     * @return 统一返回结果
     */
    public Result<Void> seckill(Long productId, Long userId) {
        // 1) 简单限流：同一用户 1 秒内只允许一次
        if (!allowRequest(userId)) {
            return Result.fail("请求过于频繁，请稍后再试");
        }

        // 2) Redis 预减库存（快速拦截，减轻数据库压力）
        Long remain = luaDeduct(productId);
        if (remain == null || remain < 0) {
            return Result.fail("已抢光，下次早点来");
        }

        // 3) 数据库条件更新扣减库存 + 写订单（同一事务，原子且不超卖、不丢单）
        boolean ok = txExecutor.deductAndOrder(productId, userId);
        if (!ok) {
            // 扣减失败：说明库存已被抢空，回补 Redis 预减的 1 个，保持 Redis/DB 一致
            redisTemplate.opsForValue().increment(stockKey(productId));
            return Result.fail("手慢了，已被抢完");
        }

        return Result.ok(null, "抢购成功，剩余库存：" + remain);
    }

    /** 用 Lua 原子预减库存；返回扣减后的剩余库存，不足时返回 -1 */
    private Long luaDeduct(Long productId) {
        return redisTemplate.execute(deductScript, Collections.singletonList(stockKey(productId)), "1");
    }

    /** 简单固定窗口限流：key=seckill:limit:{userId}，1 秒内计数不超过 LIMIT_PER_SECOND */
    private boolean allowRequest(Long userId) {
        String key = "seckill:limit:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(1));
        }
        return count != null && count <= LIMIT_PER_SECOND;
    }

    private String stockKey(Long productId) {
        return "seckill:stock:" + productId;
    }
}
