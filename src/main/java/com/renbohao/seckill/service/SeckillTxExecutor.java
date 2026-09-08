package com.renbohao.seckill.service;

import com.renbohao.seckill.entity.SeckillOrder;
import com.renbohao.seckill.mapper.ProductMapper;
import com.renbohao.seckill.mapper.SeckillOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 秒杀"落库"事务执行器。
 *
 * <p>把"扣库存 + 写订单"收敛到独立 Bean 的 @Transactional 方法中，
 * 保证两者要么都成功、要么都回滚（原子性），避免"库存扣了但订单没写入"的数据不一致。</p>
 */
@Service
public class SeckillTxExecutor {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SeckillOrderMapper orderMapper;

    /**
     * 条件更新扣库存并写入订单。
     *
     * @param productId 商品 id
     * @param userId    用户 id
     * @return true-成功；false-扣减失败（库存已耗尽或商品不存在）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deductAndOrder(Long productId, Long userId) {
        // 原子扣减：只有库存 > 0 才扣减，保证不超卖且不丢单
        int rows = productMapper.deductStock(productId);
        if (rows == 0) {
            return false;
        }
        // 扣减成功，写入订单
        SeckillOrder order = new SeckillOrder();
        order.setProductId(productId);
        order.setUserId(userId);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        return true;
    }
}
