package com.deltaqin.redis.r01_string;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.List;

/**
 * @author deltaqin
 * @date 2021/6/26 上午10:32
 */
public class SetNx {
    public static void main(String[] args) throws Exception {
        Jedis jedis = new Jedis("10.211.55.4", 6379);

        // 最简单的缓存读写示例 set  get  del
        jedis.set("key1", "value1");
        System.out.println(jedis.get("key1"));
        jedis.del("lock_test");

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



        // 博客的发布、修改与查看

        // 批量和单独for多少次的效率是不一样的 msetnx
        Long publishBlogResult = jedis.msetnx("article:1:title", "学习Redis",
                "article:1:content", "如何学好redis的使用",
                "article:1:author", "deltaqin",
                "article:1:time", "2020-01-01 00:00:00");
        System.out.println("发布博客的结果：" + publishBlogResult);  // 0

        // mget
        List<String> blog = jedis.mget("article:1:title", "article:1:content",
                "article:1:author", "article:1:time");
        System.out.println("查看博客：" + blog); // [学习Redis, 如何学好redis的使用, deltaqin, 2020-01-01 00:00:00]

        // mset
        String updateBlogResult = jedis.mset("article:1:title", "修改后的学习redis",
                "article:1:content", "修改后的如何学好redis的使用");
        System.out.println("修改博客的结果：" + updateBlogResult); // OK

        // 返回一个数组 mget
        blog = jedis.mget("article:1:title", "article:1:content",
                "article:1:author", "article:1:time");
        System.out.println("再次查看博客：" + blog); // [修改后的学习redis, 修改后的如何学好redis的使用, deltaqin, 2020-01-01 00:00:00]

        // 返回包含的字数 strlen
        Long blogLength = jedis.strlen("article:1:content");
        System.out.println("博客的长度统计：" + blogLength); // 38

        // 截取字符串  getrange
        String blogContentPreview = jedis.getrange(
                "article:1:content", 0, 5);
        System.out.println("博客内容预览：" + blogContentPreview); // 修改

        // 操作日志的审计功能
        // 追加 append
        jedis.del("operation_log_2020_01_01");
        jedis.setnx("operation_log_2020_01_01", "");

        for(int i = 0; i < 10; i++) {
            jedis.append("operation_log_2020_01_01", "今天的第" + (i + 1) + "条操作日志\n");
        }

        String operationLog = jedis.get("operation_log_2020_01_01");
        System.out.println("今天所有的操作日志：\n" + operationLog);

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
