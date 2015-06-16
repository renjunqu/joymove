package com.joymove.test;

import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class testShutListener implements ShutdownListener {

	@Override
	public void shutdownCompleted(ShutdownSignalException cause) {
		// TODO Auto-generated method stub
	//	logger.trace("shuwdown !!!");
	//	logger.trace(cause.getReason().toString());
		
	}

}
