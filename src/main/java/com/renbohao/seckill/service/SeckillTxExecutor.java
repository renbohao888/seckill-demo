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
     * 乐观锁扣减库存并写入订单。
     *
     * @param productId 商品 id
     * @param version   读到的版本号（乐观锁条件）
     * @param userId    用户 id
     * @return true-成功；false-扣减失败（版本冲突或库存已耗尽）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deductAndOrder(Long productId, Integer version, Long userId) {
        // 乐观锁更新：只有版本号一致且库存>0 时才扣减
        int rows = productMapper.deductStockByVersion(productId, version);
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
