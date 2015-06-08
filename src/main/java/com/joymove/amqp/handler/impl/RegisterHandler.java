package com.joymove.amqp.handler.impl;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymove.amqp.handler.EventHandler;
import org.springframework.stereotype.Component;

@Component("RegisterHandler")
public class RegisterHandler  implements EventHandler {
	final static Logger logger = LoggerFactory.getLogger(RegisterHandler.class);

	public int getEventType() {
	     return 1;
	}
	public boolean handleData(JSONObject json) {
		boolean error=true;
		try {
				logger.error("registerCarAck method was invoked...");
				logger.info("qrj: cloudmove tell the car send register packet to him ");
				return false;
		} catch(Exception e){
			error = true;
			 logger.info("error to send data to joymove");
		}
		return error;
	}

}
