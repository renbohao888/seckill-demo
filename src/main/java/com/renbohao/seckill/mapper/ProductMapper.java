package com.renbohao.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renbohao.seckill.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 秒杀商品 Mapper。
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 原子扣减库存：仅当"库存 > 0"时才扣减，保证库存永不为负（不超卖）。
     *
     * <p>相比 version 乐观锁，这种方式在高并发下**不会因版本冲突丢单**：
     * 每个成功请求都真实扣减 1，直到库存为 0；并发由数据库行锁串行化，安全且高效。</p>
     *
     * @param id 商品 id
     * @return 影响行数：1-成功，0-失败（库存已耗尽或商品不存在）
     */
    @Update("UPDATE product SET stock = stock - 1 WHERE id = #{id} AND stock > 0")
    int deductStock(@Param("id") Long id);
}
