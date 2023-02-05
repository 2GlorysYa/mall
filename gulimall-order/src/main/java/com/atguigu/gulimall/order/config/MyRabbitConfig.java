package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 使用json序列化机制，进行消息转换
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     *
     * 1. 服务器收到消息就回调
     *      1. 配置文件声明spring.rabbitmq.publisher-confirms=true
     *      2. 设置确认回调ConfirmCallback
     * 2. 消息正确抵达队列进行回调
     *      1. spring.rabbitmq.publisher-returns=true
     *      2. spring.rabbitmq.template.mandatory=true
     * 3. 消费者确认 （保证每个消息被正确消费，此时broker才可以删除这个消息）
     *      1. 默认是自动确认ack的，只要消息被接收到，客户端会自动确认，服务器就会移除这个消息
     *
     *      手动确认模式：只要没有ack。消息就一直是unacked状态，即使consumer宕机，消息也不回丢失
     *      会重新变成ready，下一次有新的consumer连接进来就发给他
     */
    @PostConstruct  // 等MyRabbitConfig构造器执行完后再执行这个方法
    public void initRabbitTemplate() {

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 只要消息抵达broker就ack=true
             * @param correlationData 当前消息的唯一关联数据(这个是消息的唯一id)
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            // 设置确认回调
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println(correlationData + " " + ack + " " + cause);
            }
        });


        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 设置消息抵达队列的确认回调，只要消息没有投递给制定队列，就触发这个失败回调
             * @param message 投递失败的消息信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本内容
             * @param s1    当时这个消息发给哪个交换机
             * @param s2    当时这个消息使用的哪个routing-key
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String s1, String s2) {
                System.out.println();
            }
        });

    }


}
