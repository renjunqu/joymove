package com.joymove.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.rabbitmq.client.Consumer;
/**
 * Created by qurj on 15/5/8.
 */
public class ExpatAttributes {

    public static void main(String[]args){
        System.out.println("Hello");
         ApplicationContext context = new ClassPathXmlApplicationContext("classpath:**/applicationContext-mvc.xml");
         Consumer consumer = (Consumer)context.getBean("rabbitmqConsumer");
    }

}
