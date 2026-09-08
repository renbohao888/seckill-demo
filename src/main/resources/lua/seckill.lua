-- ============================================================
-- 秒杀库存预减（原子）脚本
-- KEYS[1] : 库存 key（如 seckill:stock:{productId}）
-- ARGV[1] : 本次扣减数量（默认 1）
-- 返回   : 扣减后的剩余库存；若库存不足返回 -1
-- 说明   : GET + DECRBY 放在同一个 Lua 脚本内执行，Redis 单线程特性
--          保证"判断-扣减"是原子的，避免并发下把库存扣成负数。
-- ============================================================
local stock = tonumber(redis.call('GET', KEYS[1]) or '-1')
local need  = tonumber(ARGV[1])
if stock < need then
    return -1
end
redis.call('DECRBY', KEYS[1], need)
return stock - need
