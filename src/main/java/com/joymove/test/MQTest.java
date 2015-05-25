package com.joymove.test;

import java.io.IOException;

public class MQTest {
	
	public static  void test(){
		MQConsumer mq;
		try {
			mq = new MQConsumer("cloudmove", "123.56.106.52",5672);
			Thread thred = new Thread(mq);
			thred.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
	
	public static void main(String[] args){
		
		try {
			MQTest.test();
			System.out.println("sfsdf");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
