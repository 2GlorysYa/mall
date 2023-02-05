package com.atguigu.gulimall.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    /**
     * 创建exchange，queue，binding
     * 收发消息
     */
    @Test
    public void createExchange() {
        // DirectExchange exchange = new DirectExchange();
        String s = new String();
        // amqpAdmin.declareExchange();
    }

}
