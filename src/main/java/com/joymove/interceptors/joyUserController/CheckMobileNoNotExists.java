package com.joymove.interceptors.joyUserController;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.json.simple.*;
import org.json.simple.parser.*;

import com.joymove.util.JsonHashUtils;
import com.joymove.entity.JOYUser;
import com.joymove.service.JOYUserService;
import java.math.BigDecimal;



public class CheckMobileNoNotExists  extends  HandlerInterceptorAdapter{
	
	private JOYUserService   joyUserService;

	public JOYUserService getJoyUserService() {
		return joyUserService;
	}

	public void setJoyUserService(JOYUserService joyUserService) {
		this.joyUserService = joyUserService;
	}
	
	

	


	@Override  
	public boolean preHandle(HttpServletRequest req,
			HttpServletResponse response, Object handler) throws Exception {
		    Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
		    
		  JSONObject reObj = new JSONObject();
		  reObj.put("result", "10003");
			
		  JOYUser user = new JOYUser();
		  user.mobileNo = (String)jsonObj.get("mobileNo"); //setMobileNo((String)jsonObj.get("mobileNo"));
		  List<JOYUser> users = joyUserService.getNeededList(user);
		  
		 
	      if(users.size()==1) {
	    	  user = users.get(0);
	    	  req.setAttribute("cUser", user);
	    	  return true;
	      } 
	    	
	      response.setContentType("application/json;charset=UTF-8");           
		  response.setHeader("Cache-Control", "no-cache");
		  response.getWriter().write(reObj.toString()); 
	      return false;
		}
	
}
