package com.deltaqin.redis.r01_string;

import redis.clients.jedis.Jedis;

/**
 * @author deltaqin
 * @date 2021/6/26 上午10:42
 */
public class 自增减 {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("10.211.55.4", 6379);


        // 唯一ID生成器
        // 自增incr
        jedis.del("order_id_counter");

        for(int i = 0; i < 10; i++) {
            Long orderId = jedis.incr("order_id_counter");
            //Long key = jedis.incrBy("key", 1);
            System.out.println("生成的第" + (i + 1) + "个唯一ID：" + orderId);
        }

        // 博客的点赞计数器  incr   decr
        jedis.del("article:1:dianzan");

        for(int i = 0; i < 10; i++) {
            jedis.incr("article:1:dianzan");
        }
        Long dianzanCounter = Long.valueOf(jedis.get("article:1:dianzan"));
        System.out.println("博客的点赞次数为：" + dianzanCounter);

        jedis.decr("article:1:dianzan");
        dianzanCounter = Long.valueOf(jedis.get("article:1:dianzan"));
        System.out.println("再次查看博客的点赞次数为：" + dianzanCounter);
    }
}
