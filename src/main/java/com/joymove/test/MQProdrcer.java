package com.joymove.test;

import java.io.IOException;

import org.json.simple.JSONObject;

public class MQProdrcer {
	
	public static void main(String[] args){
		 try {
        	 MQProducer mqProducer = new MQProducer("cloudmove", "123.56.106.52", 5672);
        	for (int i = 0; i < 10000; i++) {
        		JSONObject jsonObject = new JSONObject();
        	    jsonObject.put("test"+i, "hello world");
        		mqProducer.sendMessage(jsonObject.toString());
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
}
