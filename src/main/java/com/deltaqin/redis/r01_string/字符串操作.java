package com.deltaqin.redis.r01_string;

import redis.clients.jedis.Jedis;

/**
 * @author deltaqin
 * @date 2021/6/26 上午10:41
 */
public class 字符串操作 {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("10.211.55.4", 6379);


        // 返回包含的字数 strlen
        Long blogLength = jedis.strlen("article:1:content");
        System.out.println("博客的长度统计：" + blogLength);

        // 截取字符串
        String blogContentPreview = jedis.getrange(
                "article:1:content", 0, 5);
        System.out.println("博客内容预览：" + blogContentPreview);

        // 追加 append
        jedis.del("operation_log_2020_01_01");
        jedis.setnx("operation_log_2020_01_01", "");

        for(int i = 0; i < 10; i++) {
            jedis.append("operation_log_2020_01_01", "今天的第" + (i + 1) + "条操作日志\n");
        }

        String operationLog = jedis.get("operation_log_2020_01_01");
        System.out.println("今天所有的操作日志：\n" + operationLog);
    }
}
