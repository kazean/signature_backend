package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        try (JedisPool jedisPool = new JedisPool("127.0.0.1", 6379)) {
            try (Jedis jedis = jedisPool.getResource()) {
                // hash
                // hset
                jedis.hset("users:2:info", "name", "greg2");

                HashMap<String, String> userInfo = new HashMap<>();
                userInfo.put("email", "greg3@fastcampus.co.kr");
                userInfo.put("phone", "010-xxxx-yyyy");
                jedis.hset("users:2:info", userInfo);

                // hdel
                jedis.hdel("users:2:info", "phone");

                // get, getall
                System.out.println(jedis.hget("users:2:info", "email"));
                Map<String, String> user2Info = jedis.hgetAll("users:2:info");
                user2Info.forEach((k, v) -> System.out.printf("%s %s%n", k,v));

                // hincr
                jedis.hincrBy("users:2:info", "visits", 30);
            }
        }
    }
}