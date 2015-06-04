package com.joymove.test;

import org.json.simple.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component("DoPost")
public class DoPost {
	
	public void doPost() throws  Exception{
		System.out.println("hello doPost");
		throw new Exception("heelo world");
	}
	public static void main(String [] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:**/applicationContext-mvc.xml");
		try {
			DoPost dopost = (DoPost) context.getBean("DoPost");
			dopost.doPost();
		} catch(Exception e){
			System.out.println("show stack trace ===>>>>>>>>>>>>>>>>>>>");
			e.printStackTrace();
			System.out.println("show stack trace over <<<<<<<<<<<<<<<<<<<<<<<===>>>>>>>>>>>>>>>>>>>");
		}
	}
}
