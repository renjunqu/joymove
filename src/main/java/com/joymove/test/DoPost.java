package com.joymove.test;

import com.joymove.entity.JOYNCar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Component("DoPost")
public class DoPost {

	final static Logger logger = LoggerFactory.getLogger(DoPost.class);


	public  void  doPost() throws  Exception{
		logger.trace("hello doPost");
		throw new Exception("heelo world");
	}
	public static void main(String [] args)  throws Exception {

		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:**/applicationContext-mvc.xml");

		DoPost post = (DoPost)context.getBean("DoPost");
		post.doPost();
		return ;
	}
}
