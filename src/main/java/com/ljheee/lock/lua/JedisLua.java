package com.ljheee.lock.lua;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * Jedis执行lua脚本 示例
 */
public class JedisLua {

    public static void main(String[] args) {

        Jedis jedis = new Jedis("127.0.0.1", 6379);

        String script = "return {KEYS[1], KEYS[2], ARGV[1], ARGV[2], ARGV[3]}";
        List<String> keys = new ArrayList<>();
        keys.add("key1");
        keys.add("key2");

        List<String> args2 = new ArrayList<>();
        args2.add("first");
        args2.add("second");
        args2.add("third");
        Object eval = jedis.eval(script, keys, args2);

        System.out.println(eval);
    }
}
