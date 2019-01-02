package com.ljheee.lock.basic;

import com.ljheee.lock.IDistributedLock;
import redis.clients.jedis.Jedis;

/**
 * 基本锁
 * 原理:利用redis的setnx,如果不存在某个key则设置值，设置成功则表示取得锁成功。
 * 缺点:如果获取锁后的进程在没有执行完就挂了，则锁永远不会释放。
 * 改进型
 * 改进:在基本形式锁上setnx后设置expire,保证超时后也能自动释放锁。
 * 缺点: setnx与expire不是一个原子操作，可能执行完setnx该进程就挂了。
 */
public class BasicRedisLock implements IDistributedLock {

    private String LOCK = "redis_lock";
    private Jedis jedis = null;

    public BasicRedisLock() {
        jedis = new Jedis("127.0.0.1", 6379);
    }

    @Override
    public boolean lock(String key) throws Exception {


        synchronized (LOCK) {
            if (setnx(key)) {
                jedis.expire(key, 60);//防止 获取锁后的进程挂了，永远不释放锁。
                return true;
            }
        }//setnx与expire不是-个原子操作,借助synchronized
        return false;
    }

    @Override
    public void unLock(String key) throws Exception {
        jedis.del(key);
    }


    private boolean setnx(String key) {
        if (jedis.setnx(key, System.currentTimeMillis() + "") == 1L) {
            return true;
        }
        return false;
    }
}
