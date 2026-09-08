package com.renbohao.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 高并发秒杀练习项目启动入口。
 *
 * <p>业务背景：模拟"限量抢购"场景，核心难点是<b>高并发下不超卖</b>。</p>
 * <p>实现要点：</p>
 * <ol>
 *   <li><b>Redis 预减库存</b>：用 Lua 脚本原子判断并扣减 Redis 中的库存，快速拦截大部分请求，减轻数据库压力；</li>
 *   <li><b>数据库乐观锁兜底</b>：真正落库时用 version 乐观锁 / stock>0 条件更新，保证数据库层面绝不超卖；</li>
 *   <li><b>接口限流</b>：基于 Redis 计数器做简单的"每人每秒限一次"，避免恶意刷单。</li>
 * </ol>
 */
@SpringBootApplication
public class SeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }
}
