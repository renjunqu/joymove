package com.joymove.amqp.handler.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymove.amqp.handler.EventHandler;
import com.futuremove.cacheServer.test.slf4j.testSlf4J;
import com.futuremove.cacheServer.utils.ConfigUtils;
import com.futuremove.cacheServer.utils.HttpPostUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

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
