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
     * 乐观锁扣减库存：只有"当前版本号 = 期望版本号"且"库存 > 0"时才扣减，
     * 否则返回 0（表示更新失败，说明已经被别人抢先扣减或库存耗尽）。
     *
     * @param id      商品 id
     * @param version 期望的（读到的）版本号
     * @return 影响行数：1-成功，0-失败
     */
    @Update("UPDATE product SET stock = stock - 1, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND stock > 0")
    int deductStockByVersion(@Param("id") Long id, @Param("version") Integer version);
}
