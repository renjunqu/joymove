package com.joymove.interceptors;

import java.io.BufferedReader;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.json.simple.*;
import org.json.simple.parser.*;

import com.joymove.util.JsonHashUtils;



public class JsonTransformer extends HandlerInterceptorAdapter {
	
	@Override  
	public boolean preHandle(HttpServletRequest req,
			HttpServletResponse response, Object handler) throws Exception {
		
		  
		  	  try {
				 
					 
		  		            BufferedReader re = req.getReader();
					    	Hashtable<String, Object> jsonObj = JsonHashUtils.strToJSONHash(re);
					    	req.setAttribute("jsonArgs", jsonObj);
					 	    
					    	
					    	
					    	return true;
					 
				  
				  
			  } catch(Exception e) {
				  System.out.println(e);
				 
				  
			  }  
			 
			  JSONObject reObj = new JSONObject();
			  reObj.put("result", "10003");
			  reObj.put("errMsg", "change to json failed");
			  response.setContentType("application/json;charset=UTF-8");           
			  response.setHeader("Cache-Control", "no-cache");
			  response.getWriter().write(reObj.toString()); 
		      return false;
		}
	
}
