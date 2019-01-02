package com.ljheee.lock.lua;

import com.ljheee.lock.IDistributedLock;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * https://juejin.im/entry/5a5f3a496fb9a01cbe655abf
 *
 * 使用 原生Jedis执行lua脚本
 */
public class LuaRedisLock implements IDistributedLock {


    private Jedis jedis = null;
    private static final int LOCK_MAX_EXIST_TIME = 5;  //一个线程持有锁的最大时间，单位s


    private String LOCK_SCRIPT = "-- Set a lock\n" +
            "--  如果获取锁成功，则返回 1\n" +
            "local key     = KEYS[1]\n" +
            "local content = KEYS[2]\n" +
            "local ttl     = ARGV[1]\n" +
            "local lockSet = redis.call('setnx', key, content)\n" +
            "if lockSet == 1 then\n" +
            "  redis.call('pexpire', key, ttl)\n" +
            "--  redis.call('incr', \"count\")\n" +
            "else\n" +
            "  -- 如果value相同，则认为是同一个线程的请求，则认为重入锁\n" +
            "  local value = redis.call('get', key)\n" +
            "  if(value == content) then\n" +
            "    lockSet = 1;\n" +
            "    redis.call('pexpire', key, ttl)\n" +
            "  end\n" +
            "end\n" +
            "return lockSet";
    private String UN_LOCK_SCRIPT = "-- unlock key\n" +
            "local key     = KEYS[1]\n" +
            "local content = KEYS[2]\n" +
            "local value = redis.call('get', key)\n" +
            "if value == content then\n" +
            "--  redis.call('decr', \"count\")\n" +
            "  return redis.call('del', key);\n" +
            "end\n" +
            "return 0";

    // 线程变量
    private ThreadLocal<String> threadKeyId = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return UUID.randomUUID().toString();
        }
    };

    public LuaRedisLock() {
        jedis = new Jedis("127.0.0.1", 6379);
    }



    @Override
    public boolean lock(String key) throws Exception {
        List<String> keys = new ArrayList<>();
        keys.add(key);//key
        keys.add(threadKeyId.get());//content

        List<String> args = new ArrayList<>();
        // 防止死锁(获得锁后挂了)设置过期时间
        args.add(String.valueOf(LOCK_MAX_EXIST_TIME * 1000));//ttl redis的单位要给 毫秒

        Object eval = jedis.eval(LOCK_SCRIPT, keys, args);
        System.out.println(eval);

        if (Integer.parseInt(eval.toString()) == 1) {
            return true;
        }
        return false;
    }

    @Override
    public void unLock(String key) throws Exception {
        Object eval = jedis.eval(UN_LOCK_SCRIPT, 2, key, threadKeyId.get());
        System.out.println(eval);
        if (Integer.parseInt(eval.toString()) == 1) {
            System.out.println("released lock");
        }
    }

    public static void main(String[] args) throws Exception {
        LuaRedisLock luaRedisLock = new LuaRedisLock();
        String lockKey = "aLock";
        if (luaRedisLock.lock(lockKey)) {
            System.out.println("get lock....");
        }
        Thread.sleep(1000);
        luaRedisLock.unLock(lockKey);

    }

}
