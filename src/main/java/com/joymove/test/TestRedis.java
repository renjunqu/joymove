package com.joymove.test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
public class TestRedis {
   public static void main(String[] args) {
      //Connecting to Redis server on localhost
	  JedisPool pool = new JedisPool(new JedisPoolConfig(), "123.56.106.52");
	  byte[] temp = new byte[10];
	  try{
		 System.out.println("start to get a pool");
		 
		 System.in.read(temp, 0, 10);
		 Jedis jedis = pool.getResource();
		 System.out.println("start to write");
		 System.in.read(temp, 0, 10);
		
		
		 jedis.set("foo", "bar");
		 System.out.println("start to read");
		 System.in.read(temp, 0, 10);
		 String foobar = jedis.get("foo");
		 System.out.println("read result is "+foobar);
		 System.in.read(temp, 0, 10);
		 System.out.println("test error read");
		 System.in.read(temp, 0, 10);
		 foobar = jedis.get("foo");
		 System.in.read(temp, 0, 10);
		 System.out.println("Over");
		 
		 
	  } catch(Exception e){
		  //e.printStackTrace();
		  System.out.println("exception happens");
		  try {
		  System.in.read(temp, 0, 10);
		  } catch(Exception e1){
			  
		  }
		  Jedis jedis = pool.getResource();
		   
		  System.out.println("get pool again");
		  try {
		  System.in.read(temp, 0, 10);
		  } catch(Exception e1){
			  
		  }
			
	  }
	   
     
 }
}

