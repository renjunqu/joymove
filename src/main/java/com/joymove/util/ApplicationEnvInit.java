package com.joymove.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.quartz.Scheduler;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.joymove.amqp.listeners.CMShutdownListener;
import com.futuremove.cacheServer.utils.ConfigUtils;
import com.futuremove.cacheServer.utils.SpringContextUtils;
import com.mongodb.MongoClient;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ApplicationEnvInit implements ServletContextListener {
	

	

	static Channel channel = null;


	public void contextDestroyed(ServletContextEvent event) {

		if(ApplicationEnvInit.channel!=null) {
			try {

	      		
	      		WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
	      		Connection conn = (Connection)context.getBean("rabbitmqConn");
	      		conn.close();
	      		ApplicationEnvInit.channel.close();
	      		Scheduler scheduler = (Scheduler)context.getBean("scheduler");
	      		scheduler.shutdown();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return;
		
	}


	public void contextInitialized(ServletContextEvent event) {
       //start the  amqp  consumer
		
		
		try {


			    String queueName = ConfigUtils.getPropValues("amqp.queue");
			    WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
				Connection conn = (Connection)context.getBean("rabbitmqConn");
				Consumer consumer = (Consumer)context.getBean("rabbitmqConsumer");

				ApplicationEnvInit.channel = conn.createChannel();
				ApplicationEnvInit.channel.basicConsume(queueName, true,"qrjCus1",consumer);
				CMShutdownListener  listener = new  CMShutdownListener();
				ApplicationEnvInit.channel.addShutdownListener(listener);
				
        		Scheduler scheduler = (Scheduler)context.getBean("scheduler");
        		scheduler.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
		return;
	}
	
	public static void main(String[]args){

        // ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext-mvc.xml");
		// Consumer consumer = (Consumer)context.getBean("rabbitmqConsumer");


		
	}

}
