-- ============================================================
-- 秒杀练习项目建库建表脚本
-- 使用：mysql -u root -p < init.sql
-- 说明：先建库、建表，再插入一条测试商品
-- ============================================================
CREATE DATABASE IF NOT EXISTS `seckill_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `seckill_db`;

-- 秒杀商品表：stock 剩余库存（用 stock>0 条件更新保证不超卖）
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name`    VARCHAR(64) NOT NULL COMMENT '商品名称',
    `stock`   INT         NOT NULL DEFAULT 0 COMMENT '剩余可秒杀库存',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 秒杀订单表
DROP TABLE IF EXISTS `seckill_order`;
CREATE TABLE `seckill_order` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `product_id`  BIGINT      NOT NULL COMMENT '商品ID',
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    PRIMARY KEY (`id`),
    KEY `idx_product` (`product_id`),
    KEY `idx_user`    (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- 测试数据：库存 10 件
INSERT INTO `product` (`name`, `stock`) VALUES
('测试秒杀商品', 10);
