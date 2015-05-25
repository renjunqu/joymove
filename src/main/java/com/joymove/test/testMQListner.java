package com.joymove.test;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.futuremove.cacheServer.test.Human;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;

public class testMQListner {
	public static void main(String[] args) throws IOException{
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/cacheServerBeans.xml");
		 Connection conn = (Connection)context.getBean("rabbitmqConn");
		 Channel channel = conn.createChannel();
		 channel.addShutdownListener(new testShutListener());
		 testMQConsumer consumer = new testMQConsumer();
		 channel.basicConsume("qqq2", true,"qrjCus",consumer);
		
	}

}
