package com.joymove.amqp.consumers;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
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
import com.futuremove.cacheServer.utils.SpringContextUtils;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("rabbitmqConsumer")
public class CMConsumer implements Consumer{
	
	public final Logger logger = LoggerFactory.getLogger(CMConsumer.class);

	@Autowired
	public List<EventHandler> handerList;






	public void handleCancel(String arg0) throws IOException {}

	public void handleCancelOk(String arg0) {}

	/** Called when consumer is registered. */
	public void handleConsumeOk(String arg0) {}

	/** Called when new message is available.*/
	public void handleDelivery(String arg0, Envelope arg1, BasicProperties arg2, byte[] arg3) {
		try {
		    	String res = new String(arg3);
				logger.warn(res);
				JSONParser parser=new JSONParser();
			 
				 Map json = (Map)parser.parse(res);
				 logger.warn("show the parse result");
				 logger.warn(json.toString());
			     Integer eventType = Integer.parseInt(json.get("type").toString());
			     for(EventHandler handler:handerList) {
					 if(handler.getEventType() == eventType) {
						 logger.warn("the to called handler is " + handler.toString());
						 JSONObject json_data = (JSONObject)json.get("data");
						 handler.handleData(json_data);
					 }
				 }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}

	public void handleRecoverOk(String arg0) {
		logger.trace("recover ok");
	}

	public void handleShutdownSignal(String arg0, ShutdownSignalException arg1) throws  ShutdownSignalException  {
		logger.warn("now the channel shutdown !!!"+arg0);
		logger.warn("reason "+arg1.getReason());
		
	}
}
