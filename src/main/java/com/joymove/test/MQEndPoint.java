package com.joymove.test;



import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * represent a connection with queue
 * @author wx
 *
 */
public abstract class MQEndPoint {

	protected Channel channel;
	protected Connection connection;
	protected String endPointName;

	public MQEndPoint(String endpointName,String ip, int port) throws IOException{

		this.endPointName = endpointName;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(ip);
		factory.setPort(port);
		connection = factory.newConnection();
		channel = connection.createChannel();
		channel.queueDeclare(endpointName, false, false, false, null);

	}

	/***
	 * close channel and connection
	 * @throws IOException
	 */
	 public void close() throws IOException{
		 this.channel.close();
		 this.connection.close();
	 }
}
