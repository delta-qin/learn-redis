package com.deltaqin.redis.r03_list;

/**
 * @author deltaqin
 * @date 2021/6/26 下午2:27
 */

import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * 注册之后发送邮件的案例
 */
public class SendMailDemo {

    Jedis jedis = new Jedis("10.211.55.4", 6379);

    /**
     * 让发送邮件任务入队列
     * @param sendMailTask
     */
    public void enqueueSendMailTask(String sendMailTask) {
        jedis.lpush("send_mail_task_queue", sendMailTask);
    }

    /**
     * 阻塞式获取发送邮件任务
     * @return
     */
    public List<String> takeSendMailTask() {
        return jedis.brpop(5, "send_mail_task_queue");
    }

    public static void main(String[] args) {
        SendMailDemo demo = new SendMailDemo();

        System.out.println("尝试阻塞式的获取发送邮件任务......");
        List<String> sendMailTasks = demo.takeSendMailTask();

        demo.enqueueSendMailTask("第一个邮件发送任务");
        sendMailTasks = demo.takeSendMailTask();
        System.out.println(sendMailTasks);
    }

}
