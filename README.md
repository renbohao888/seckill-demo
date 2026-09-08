# 高并发秒杀练习项目（seckill-demo）

一个面向 **测试开发方向** 的练习项目：用 `Spring Boot + Redis + MySQL` 实现"限量抢购"，
核心目标是 **高并发下不超卖**，并附带完整的**接口测试用例 / JMeter 压测脚本 / 测试报告模板**。

> 适合用于简历"项目经历"，也是练手"并发 + 压测 + 防超卖"的经典场景。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 21、Spring Boot 3.5、Spring Web、Lombok |
| 数据 | MySQL 8、MyBatis-Plus 3.5 |
| 缓存/并发 | Redis（String）+ Lua 脚本、`stock > 0` 条件更新 |
| 测试 | Postman、JMeter |

## 核心设计（为什么会话讲"防超卖"）

1. **Redis 库存预减（第一道闸）**：用 Lua 脚本原子地"判断库存 > 0 再扣减"，
   把绝大部分请求在 Redis 层快速拦截，**减轻数据库压力**。
2. **数据库条件更新兜底（最终保证）**：落库时用
   `UPDATE product SET stock = stock - 1 WHERE id = ? AND stock > 0`，
   原子地扣减，只有真正扣减成功才写订单，**数据库层面绝不超卖、且不丢单**。
3. **接口限流（简单固定窗口）**：Redis 计数器限制"同一用户 1 秒 1 次"，防恶意刷单。
4. **失败补偿**：若 Redis 预减成功但数据库扣减失败（库存已抢空），
   会把 Redis 里预减的库存 `INCR` 回补，保持 Redis 与 DB 一致。

> 一句话讲清：**Redis 是"快速拦截 + 扛流量"；数据库条件更新是"最终一致 + 不超卖"的兜底。**

## 实测结果（200 并发 @ 库存 10）

| 指标 | 结果 |
|---|---|
| 并发请求 | 200 |
| 成功单量 | **10**（卖光，无超卖） |
| 失败·已抢光 | 190 |
| 失败·手慢了 | 0（无丢单） |
| QPS | ~433 |
| DB 剩余库存 | 0（无负数） |
| Redis 剩余库存 | 0（与 DB 一致） |

## 目录结构

```
seckill-demo/
├── pom.xml
├── sql/init.sql                      建库建表 + 测试数据
├── src/main/resources/
│   ├── application.yml               配置（数据库/Redis）
│   └── lua/seckill.lua               Redis 预减 Lua 脚本
├── src/main/java/com/renbohao/seckill/
│   ├── SeckillApplication.java       启动入口
│   ├── common/Result.java            统一返回结构
│   ├── entity/Product.java、SeckillOrder.java
│   ├── mapper/ProductMapper.java、SeckillOrderMapper.java
│   ├── service/SeckillService.java   限流+预减+兜底+补偿
│   ├── service/SeckillTxExecutor.java 扣库存+写订单（同一事务）
│   └── controller/SeckillController.java
└── docs/ + jmeter/                   测试用例、压测脚本、报告模板
```

## 运行方法

1. 建库建表：
   ```sql
   mysql -u root -p < sql/init.sql
   ```
2. 改 `src/main/resources/application.yml` 里的数据库、Redis 连接。
3. 启动（自动把 DB 库存预热到 Redis）：
   ```bash
   mvnw spring-boot:run
   ```
4. 发起秒杀：
   ```bash
   curl -X POST "http://localhost:8081/api/seckill?productId=1&userId=101"
   ```

## 压测（JMeter）

见 `jmeter/秒杀压测.jmx` 与 `docs/测试报告模板.md`，可发并发请求验证"成功单量 = 库存、无超卖"。

## 关键验证点（测试开发视角）

- 并发 N 请求同一商品，**成功下单数 ≤ 库存数**（无超卖）。
- 压测后：`SELECT COUNT(*) FROM seckill_order WHERE product_id=1` 应 **≤ 10**；
  `SELECT stock FROM product WHERE id=1` 应 **≥ 0**。
- Redis 中 `seckill:stock:1` 与 DB `stock` 最终一致（无负数、无残留负数）。
