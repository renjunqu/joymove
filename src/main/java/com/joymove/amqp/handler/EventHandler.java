package com.joymove.amqp.handler;

import java.util.Map;

import org.json.simple.JSONObject;

public interface  EventHandler {


	
	public  boolean handleData(JSONObject json);

	public int getEventType();

}
