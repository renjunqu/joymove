package com.joymove.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.Property;
import com.googlecode.ehcache.annotations.TriggersRemove;




public class testEhcache {
	
	
	
	
 @Cacheable(cacheName="testCache", 
	        keyGenerator = @KeyGenerator (
	                name = "HashCodeCacheKeyGenerator",
	                properties = @Property( name="includeMethod", value="false" )
	            )
	        )
	public String getDog(String haha){
		System.out.println("inside print dog");
		return "dog";
	}
 
 @TriggersRemove(cacheName="testCache", 
	        keyGenerator = @KeyGenerator (
	                name = "HashCodeCacheKeyGenerator",
	                properties = @Property( name="includeMethod", value="false" )
	            )
	        )
    public String removeDog(String haha){
    	return "haha";
    }
	
	
	@Cacheable(cacheName = "testCache")  
	public String getDoglikeDog(Map<String, Object> likeCondition){
		System.out.println("inside print dog like dog ");
		return "dog";
	}
	
	@Cacheable(cacheName = "testCache")  
	public String getDogEntity(testEhcacheEntity entity){
		System.out.println("inside print dog entity ");
		return "dog";
	}
	
	
	public static void main(String [] args){
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:**/applicationContext.xml");
		testEhcache tE = (testEhcache)context.getBean("testEhcache");
		/*
		//System.out.println(Arrays.asList(context.getBeanDefinitionNames()));
		testEhcacheEntity entity = new testEhcacheEntity();
		testEhcacheEntity entity2 = new testEhcacheEntity();
		entity.setId(2);
		String re = tE.getDogEntity(entity);
		System.out.println("re is " + re);
		
		entity2.setId(2);
		re = tE.getDogEntity(entity2);
		System.out.println("re is " + re);
		System.out.println("over");
		*/
		
		String re = tE.getDog("haha");
		re = tE.getDog("hehe");
		tE.removeDog("hehe");
		re = tE.getDog("haha");
		re = tE.getDog("hehe");
		
		
		
	}

}
