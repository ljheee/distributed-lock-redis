-- unlock key
--  释放锁成功，则返回 1，否则返回0
local key     = KEYS[1]
local content = KEYS[2]
local value = redis.call('get', key)
if value == content then
--  redis.call('decr', "count")
  return redis.call('del', key);
end
return 0