package com.joymove.test;


import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQConsumer extends MQEndPoint implements Runnable, Consumer{

	final static Logger logger = LoggerFactory.getLogger(MQConsumer.class);



	public MQConsumer(String endpointName, String ip, int port) throws IOException {
		super(endpointName, ip, port);
	}

	public void handleCancel(String arg0) throws IOException {}

	public void handleCancelOk(String arg0) {}

	/** Called when consumer is registered. */
	public void handleConsumeOk(String arg0) {}

	/** Called when new message is available.*/
	public void handleDelivery(String arg0, Envelope arg1,
			BasicProperties arg2, byte[] arg3) throws IOException {
			String res = new String(arg3);
			logger.trace(res);
			/*JSONParser parser=new JSONParser();
			 try {
			Map json = (Map)parser.parse(res);
			
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	}

	public void handleRecoverOk(String arg0) {}

	public void handleShutdownSignal(String arg0, ShutdownSignalException arg1) {}

	public void run() {

		try {
			channel.basicConsume(endPointName, true,this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
