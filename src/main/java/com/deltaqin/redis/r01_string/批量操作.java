package com.deltaqin.redis.r01_string;

import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author deltaqin
 * @date 2021/6/26 上午10:37
 */
public class 批量操作 {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("10.211.55.4", 6379);

        // 批量和单独for 的效率是不一样的

        // 博客的发布、修改与查看
        // msetnx  返回Long
        // mget 返回List
        // mset 返回字符串
        Long publishBlogResult = jedis.msetnx("article:1:title", "学习Redis",
                "article:1:content", "如何学好redis的使用",
                "article:1:author", "deltaqin",
                "article:1:time", "2020-01-01 00:00:00");
        System.out.println("发布博客的结果：" + publishBlogResult);

        List<String> blog = jedis.mget("article:1:title", "article:1:content",
                "article:1:author", "article:1:time");
        System.out.println("查看博客：" + blog);

        String updateBlogResult = jedis.mset("article:1:title", "修改后的学习redis",
                "article:1:content", "修改后的如何学好redis的使用");
        System.out.println("修改博客的结果：" + updateBlogResult);

        blog = jedis.mget("article:1:title", "article:1:content",
                "article:1:author", "article:1:time");
        System.out.println("再次查看博客：" + blog);
    }
}
