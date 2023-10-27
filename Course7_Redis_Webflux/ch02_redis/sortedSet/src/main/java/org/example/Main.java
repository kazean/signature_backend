package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        try (var jedisPool = new JedisPool("127.0.0.1", 6379)) {
            try (var jedis = jedisPool.getResource()) {
                //sorted set
                var scores = new HashMap<String, Double>();
                scores.put("user1", 100D);
                scores.put("user2", 30D);
                scores.put("user3", 50D);
                scores.put("user4", 80D);
                scores.put("user5", 15D);
                jedis.zadd("game2:scores", scores);

                List<String> zrange = jedis.zrange("game2:scores", 0, Long.MAX_VALUE);
                zrange.forEach(System.out::println);

//                List<Tuple> tuples = jedis.zrangeWithScores("game2:scores", 0, Long.MAX_VALUE);
//                tuples.forEach(i -> System.out.println("%s %f".formatted(i.getElement(), i.getScore())));
                System.out.println(jedis.zcard("game2:scores"));

                jedis.zincrby("game2:scores", 100.0, "user5");
                List<Tuple> tuples = jedis.zrangeWithScores("game2:scores", 0, Long.MAX_VALUE);
                tuples.forEach(i -> System.out.println("%s %f".formatted(i.getElement(), i.getScore())));
            }
        }
    }
}