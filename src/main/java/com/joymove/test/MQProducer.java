package com.joymove.test;



import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang.SerializationUtils;


/**
 * message producer, serialize Object to bytes
 * @author wx
 *
 */
public class MQProducer extends MQEndPoint{

	public MQProducer(String endpointName,String ip, int port) throws IOException {
		super(endpointName,ip, port);
	}

	public void sendMessage(Serializable object) throws IOException {
        channel.basicPublish("",endPointName, null, SerializationUtils.serialize(object));
    }

}
