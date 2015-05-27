package com.joymove.test;

import org.json.simple.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;

public class DoPost {
	
	public static JSONObject doPost(String url,JSONObject json){
		
		JSONObject jsonObject = new JSONObject();
		return jsonObject;
	}
	public static void main(String [] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:**/applicationContext.xml");
		DataSource ds = (DataSource)context.getBean("dataSource");

		System.out.println("get dataSource ok");
		try {
			ds.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
