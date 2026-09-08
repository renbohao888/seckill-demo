package com.renbohao.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 秒杀商品实体。
 *
 * <p>stock：剩余可秒杀库存。扣减时用「stock > 0 条件更新」保证不超卖，
 * 无需额外的乐观锁版本号。</p>
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 剩余可秒杀库存 */
    private Integer stock;
}
