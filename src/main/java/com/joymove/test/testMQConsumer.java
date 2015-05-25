package com.joymove.test;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.joymove.amqp.handler.EventHandler;
import com.joymove.amqp.handler.impl.*;
import com.futuremove.cacheServer.test.slf4j.testSlf4J;
import com.futuremove.cacheServer.utils.SpringContextUtils;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class testMQConsumer implements Consumer{
	
	
	 
	
	
	public testMQConsumer() {
		
	
		
	}
	
	
	

	public void handleCancel(String arg0) throws IOException {}

	public void handleCancelOk(String arg0) {}

	/** Called when consumer is registered. */
	public void handleConsumeOk(String arg0) {}

	
	public void handleRecoverOk(String arg0) {
		System.out.println("recover ok");
	}

	public void handleShutdownSignal(String arg0, ShutdownSignalException arg1) throws  ShutdownSignalException  {
		
		
	}




	@Override
	public void handleDelivery(String arg0, Envelope arg1,
			BasicProperties arg2, byte[] arg3) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("show data");
		System.out.print(arg3.toString());	
	}
	
/*
	public void startConsume() throws IOException{
			channel.basicConsume(queueName, true,"qrjCus",this);
	}
*/
}
