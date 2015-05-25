package com.joymove.test.testMina;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.lang.Thread;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;


public class TestHandler extends IoHandlerAdapter {
	public String id;
	public TestHandler(){
		
		super();
		id = String.valueOf(UUID.randomUUID());
	}
	@Override
	public void sessionCreated(IoSession session) {
	//	String out = "TestHandler sessionCreated invoked"; 
	//	System.out.println(out);
	}
	public void exceptionCaught(IoSession session, Throwable cause){
	//	System.out.println("exceptionCaught, called");
	//	System.out.println(cause);
		cause.printStackTrace();
	}
	

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		Thread thread = Thread.currentThread();
		long thread_id = thread.getId();
		System.out.println(message.getClass());
		String out = "messageReceived called"; 
		Long sId = session.getId();
		System.out.println(out);
		System.out.println("wait for 10 seconds with sId "+sId +" with thread id "+thread_id +"with my id " + this.id );
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (Exception e) {
			
		}
		System.out.println("wait 10 s over for sID" + sId + "with thread id "+thread_id + "with my id "+ this.id);
		session.write("hello");
	}
	@Override
	public void messageSent(IoSession session, Object message)
			throws Exception {
		String out = "messageSent called "; 
	//	System.out.println(out);
	}
	
	
	
    
}
