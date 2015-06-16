package com.joymove.amqp.listeners;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.impl.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMShutdownListener implements ShutdownListener  {

	final static Logger logger = LoggerFactory.getLogger(CMShutdownListener.class);

	@Override
	public void shutdownCompleted(ShutdownSignalException cause) {
		// TODO Auto-generated method stub
		if (cause.isHardError())
		  {
			logger.trace("amq: -- hareware error");
		    Connection conn = (Connection)cause.getReference();
		    if (!cause.isInitiatedByApplication())
		    {
		      Method reason = (Method)cause.getReason();
		      logger.trace("amq: reason : "+cause.getReason().toString());
		      
		    } else {
		    	logger.trace("shutdown by app itself");
		    }
		    
		  } else {
			logger.trace("amq: -- not hareware error");
		    Channel ch = (Channel)cause.getReference();
		    logger.trace("amq: reason : "+cause.getReason().toString());
		    Method reason = (Method)cause.getReason();
		    
		  }
		
		
	}
	

}
