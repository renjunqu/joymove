package com.joymove.interceptors;

import java.io.BufferedReader;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.json.simple.*;
import org.json.simple.parser.*;

import com.joymove.util.JsonHashUtils;



public class JsonTransformer extends HandlerInterceptorAdapter {

	final static Logger logger = LoggerFactory.getLogger(JsonTransformer.class);


	@Override  
	public boolean preHandle(HttpServletRequest req,
			HttpServletResponse response, Object handler) throws Exception {
		
		  
		  	  try {
				 
					 
		  		            BufferedReader re = req.getReader();
					    	Hashtable<String, Object> jsonObj = JsonHashUtils.strToJSONHash(re);
					    	req.setAttribute("jsonArgs", jsonObj);
					 	    
					    	
					    	
					    	return true;
					 
				  
				  
			  } catch(Exception e) {
				  logger.error(e.getStackTrace().toString());
				 
				  
			  }  
			 
			  JSONObject reObj = new JSONObject();
			  reObj.put("result", "10003");
			  reObj.put("errMsg", "数据格式有错误");
			  response.setContentType("application/json;charset=UTF-8");           
			  response.setHeader("Cache-Control", "no-cache");
			  response.getWriter().write(reObj.toString()); 
		      return false;
		}
	
}
