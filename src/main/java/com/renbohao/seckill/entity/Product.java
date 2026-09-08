package com.renbohao.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 秒杀商品实体。
 *
 * <p>stock：剩余库存；version：乐观锁版本号，每次扣减 +1，
 * 用于防止并发下"读到旧库存→扣减"导致超卖。</p>
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 剩余可秒杀库存 */
    private Integer stock;

    /** 乐观锁版本号 */
    private Integer version;
}
