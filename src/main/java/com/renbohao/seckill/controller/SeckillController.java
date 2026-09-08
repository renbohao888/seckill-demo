package com.renbohao.seckill.controller;

import com.renbohao.seckill.common.Result;
import com.renbohao.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒杀接口。
 */
@RestController
@RequestMapping("/api")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 发起秒杀抢购。
     *
     * <p>请求示例：POST /api/seckill?productId=1&amp;userId=101</p>
     * <p>返回：{code:200,msg:"抢购成功，剩余库存：n"} 或 {code:400,msg:"已抢光/手慢了"}</p>
     */
    @PostMapping("/seckill")
    public Result<Void> seckill(@RequestParam Long productId, @RequestParam Long userId) {
        return seckillService.seckill(productId, userId);
    }
}
