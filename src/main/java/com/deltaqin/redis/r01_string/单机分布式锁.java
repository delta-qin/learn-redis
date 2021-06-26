package com.deltaqin.redis.r01_string;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.List;

/**
 * @author deltaqin
 * @date 2021/6/26 上午10:32
 */
public class 单机分布式锁 {
    public static void main(String[] args) throws Exception {
        Jedis jedis = new Jedis("10.211.55.4", 6379);

        // 最简单的缓存读写示例 set  get  del
        jedis.set("key1", "value1");
        System.out.println(jedis.get("key1"));
        jedis.del("lock_test");  // 先删掉

        // 最简单的基于nx选项实现的分布式锁  SetParams.setParams().nx()
        String result = jedis.set("lock_test", "value_test",
                SetParams.setParams().nx());
        System.out.println("第一次加锁的结果：" + result); // ok

        result = jedis.set("lock_test", "value_test",
                SetParams.setParams().nx());
        System.out.println("第二次加锁的结果：" + result); // null

        jedis.del("lock_test");  // 锁删掉

        result = jedis.set("lock_test", "value_test",
                SetParams.setParams().nx());
        System.out.println("第二次加锁的结果：" + result); // ok

    }
}
