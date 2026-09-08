package com.renbohao.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renbohao.seckill.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀订单 Mapper。
 */
@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {
}
