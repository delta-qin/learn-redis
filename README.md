# learn-redis

笔记总结：
https://www.yuque.com/docs/share/f0fd2a5b-6c6f-467f-9600-54dc91b15122?# 《Redis代码场景实践》

- 不同数据结构的场景实践

## string
### 博客不好的实现
```java
jedis.set("key1", "value1");
jedis.get("key1");
jedis.del("lock_test");
jedis.incr("blog_id_counter");
Long key = jedis.incrBy("key", 1);
```


```java
jedis.set("lock_test", "value_test", SetParams.setParams().nx());
```


```java
Long publishBlogResult = jedis.msetnx("article:1:title", "学习Redis",
                "article:1:content", "如何学好redis的使用",
                "article:1:author", "deltaqin",
                "article:1:time", "2020-01-01 00:00:00");
List<String> blog = jedis.mget("article:1:title", "article:1:content",
                "article:1:author", "article:1:time");
String updateBlogResult = jedis.mset("article:1:title", "修改后的学习redis",
                "article:1:content", "修改后的如何学好redis的使用");
```


```java
jedis.strlen("article:1:content");
jedis.getrange("article:1:content", 0, 5);
jedis.append("operation_log_2020_01_01", "今天的第" + (i + 1) + "条操作日志\n");
```
## hash
_hash就是一个hashmap，还自带变量名_
```java
jedis.hset("short_url_access_count", shortUrl, "0");
jedis.hset("url_mapping", shortUrl, url);
jedis.hget("short_url_access_count", shortUrl);
```
### 博客好
一篇文章的所有属性在一个hash里面，key是一篇文章，field是不同属性，val是不同值
```java

jedis.hexists("article::" + id, "title");
jedis.hmset("article::" + id, blog);  # blog 是 hashmap
Map<String, String> blog = jedis.hgetAll("article::" + id);
jedis.hincrBy("article::" + id, "like_count", 1);
```
### session
```java
SimpleDateFormat dateFormat = new SimpleDateFormat(
    "yyyy-MM-dd HH:mm:ss");

// 设置24小时过期
Calendar calendar = Calendar.getInstance();
calendar.setTime(new Date());
calendar.add(Calendar.HOUR, 24);
Date expireTime = calendar.getTime();

jedis.hset("sessions",
           "session::" + token, String.valueOf(userId));
jedis.hset("sessions::expire_time",
           "session::" + token, dateFormat.format(expireTime));


jedis.hget("sessions", "session::" + token);

// 检测过期
jedis.hget("sessions::expire_time",  "session::" + token);
SimpleDateFormat dateFormat = new SimpleDateFormat(
    "yyyy-MM-dd HH:mm:ss");
Date expireTimeDate = dateFormat.parse(expireTime);

Date now = new Date();

if(now.after(expireTimeDate)) {
    return false;
}

```
## list
### 博客分页
```java
jedis.hmset("article::" + id, blog);
jedis.lpush("blog_list", String.valueOf(id));

// 分页查询
//  pageNo * pageSize - 1; 计数是从0开始的
int startIndex = (pageNo - 1) * pageSize;
int endIndex = pageNo * pageSize - 1;
return jedis.lrange("blog_list", startIndex, endIndex);
```
### 队列FIFO

- 秒杀请求。邮件服务器
```java
jedis.lpush("sec_kill_request_queue", secKillRequest);
jedis.rpop("sec_kill_request_queue");
```
_阻塞式获取发送邮件任务_
```java
// 第一个是阻塞时间
jedis.brpop(5, "send_mail_task_queue");
```
### 待办
```java
// 插入待办事项
// 在 targetTodoEvent 前面  "插入的待办事项"
linsert(String key, ListPosition where, String pivot, String value);
jedis.linsert("todo_event::" + userId,  ListPosition.BEFORE
			, targetTodoEvent, "插入的待办事项");

// 修改一个待办事项
jedis.lset("todo_event::" + userId, index, updatedTodoEvent);
// 完成一个待办事项
jedis.lrem("todo_event::" + userId, 0, todoEvent);
```
## set
### 博客标签
```java
jedis.sadd("article::" + id + "::tags", tags);
Set<String> tags = jedis.smembers("article::" + id + "::tags");
```
### 抽奖
```java
jedis.sadd("lottery_draw_event::" + lotteryDrawEventId +"::candidates", 
           String.valueOf(userId));
jedis.srandmember("lottery_draw_event::" + lotteryDrawEventId +"::candidates", 
                  count);
```
### 微博案例
```java
// 关注
jedis.sadd("user::" + followUserId + "::followers", String.valueOf(userId));
jedis.sadd("user::" + userId + "::follow_users", String.valueOf(followUserId));

// 取消关注
jedis.srem("user::" + followUserId + "::followers", String.valueOf(userId));
jedis.srem("user::" + userId + "::follow_users", String.valueOf(followUserId));

jedis.smembers("user::" + userId + "::followers");
jedis.smembers("user::" + userId + "::follow_users");

// 查看个数
jedis.scard("user::" + userId + "::followers");
jedis.scard("user::" + userId + "::follow_users");

// 获取用户跟其他用户之间共同关注的人有哪些，注意都是follow_users
jedis.sinter("user::" + userId + "::follow_users",
             "user::" + otherUserId + "::follow_users");

// 获取给我推荐的可关注人
// 我关注的某个好友关注的一些人，我没关注那些人，此时推荐那些人给我
jedis.sdiff("user::" + otherUserId + "::follow_users",
            "user::" + userId + "::follow_users");
```
### 朋友圈点赞
```java
// 点赞和取消点赞
jedis.sadd("moment_like_users::" + momentId, String.valueOf(userId));
jedis.srem("moment_like_users::" + momentId, String.valueOf(userId));

// 查看自己是否对某条朋友圈点赞过
jedis.sismember("moment_like_users::" + momentId, String.valueOf(userId));

// 获取你的一条朋友圈有哪些人点赞了
jedis.smembers("moment_like_users::" + momentId);

// 获取你的一条朋友圈被几个人点赞了
jedis.scard("moment_like_users::" + momentId);
```
### 商品搜索案例
```java
//添加商品的时候附带一些关键词
// 一个关键词就加一个，某个关键词下面可能有多个商品
jedis.sadd("keyword::" + keyword + "::products", String.valueOf(productId));

// 根据多个关键词搜索商品（）
keywordSetKeys.add("keyword::" + keyword + "::products");
String[] keywordArray = keywordSetKeys.toArray(new String[keywordSetKeys.size()]);
jedis.sinter(keywordArray);
```
### 网站UV统计
```java
jedis.sadd("user_access::" + today, String.valueOf(userId));
jedis.scard("user_access::" + today);
```
### 投票统计
```java
// 投票
jedis.sadd("vote_item_users::" + voteItemId, String.valueOf(userId));
// 检查用户对投票项是否投过票
jedis.sismember("vote_item_users::" + voteItemId, String.valueOf(userId));
// 获取一个投票项被哪些人投票了
jedis.smembers("vote_item_users::" + voteItemId);
// 获取一个投票项被多少人投票了
jedis.scard("vote_item_users::" + voteItemId);
```
## zset
### 推荐商品
```java
// 继续购买商品
jedis.zincrby("continue_purchase_products::" + productId, 1, 
              String.valueOf(otherProductId));

// 推荐其他人购买过的其他商品.3个
Set<Tuple> jedis.zrevrangeWithScores("continue_purchase_products::" + 
                                     productId, 0, 2);

```
### 新闻
```java
jedis.zadd("news", timestamp, String.valueOf(newsId));
jedis.zrevrangeByScoreWithScores("news", maxTimestamp, minTimestamp, index, count);
```
### 排行榜
```java
// 0 s是初始分数
jedis.zadd("music_ranking_list", 0, String.valueOf(songId));
jedis.zincrby("music_ranking_list", score, String.valueOf(songId));

// 获取排名
jedis.zrevrank("music_ranking_list", String.valueOf(songId));
// 排行榜
jedis.zrevrangeWithScores("music_ranking_list", 0, 2);
```
### 自动补全
将字符的每一个追加之后作为key，value就是整句话，分数是当前日期
使用的时候直接获取对应的key即可
```java
public void add(String keyword) {
    char[] keywordCharArray = keyword.toCharArray();

    StringBuffer potentialKeyword = new StringBuffer("");

    // 我喜欢学习

    // 我：时间+我喜欢学习
    // 我喜：时间+我喜欢学习

    // 我爱大家
    // 我：时间+我爱大家

    for(char keywordChar : keywordCharArray) {
        potentialKeyword.append(keywordChar);

        jedis.zincrby(
            "potential_Keyword::" + potentialKeyword.toString() + "::keywords",
            new Date().getTime(),
            keyword);
    }
}

/**
     * 获取自动补全列表
     * @param potentialKeyword
     * @return
     */
public Set<String> getAutoCompleteList(String potentialKeyword) {
    return jedis.zrevrange("potential_Keyword::" + potentialKeyword + "::keywords",
                           0, 2);
}
```
