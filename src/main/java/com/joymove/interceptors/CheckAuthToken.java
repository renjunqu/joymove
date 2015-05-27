package com.joymove.interceptors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.json.simple.*;
import org.json.simple.parser.*;

import com.joymove.entity.JOYUser;
import com.joymove.util.JsonHashUtils;
import com.joymove.entity.JOYUser;
import com.joymove.service.JOYUserService;



public class CheckAuthToken extends HandlerInterceptorAdapter {
	private JOYUserService joyUserService;
	
	

	public JOYUserService getJoyUserService() {
		return joyUserService;
	}



	public void setJoyUserService(JOYUserService joyUserService) {
		this.joyUserService = joyUserService;
	}



	@Override  
	public boolean preHandle(HttpServletRequest req,
		  HttpServletResponse response, Object handler) throws Exception {
		  JSONObject reObj = new JSONObject();
		  reObj.put("result", "10004");
		  Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
		
		  String [] notCheckURL = new String[] {
				  "/joymove/usermgr/checkUserNameExist.c",
				  "/joymove/usermgr/checkUserIdExist.c",
				  "/joymove/usermgr/checkMobileNoExist.c",
				  "/joymove/usermgr/dynamicPwsGen.c",
				  "/joymove/usermgr/dynamicPwsVeri.c",
				  "/joymove/usermgr/login.c",
				  "/joymove/usermgr/register.c",
				  "/joymove/rent/getNearByAvailableCars.c",
				  "/joymove/rent/getNearByBusyCars.c",
				  "/joymove/rent/getNearByParks.c",
				  "/joymove/rent/getNearByPowerBars.c",
				  "/joymove/rent/checkOrderStatus.c",
				  "/joymove/rent/changeBatonMode.c",
				  "/joymove/rent/updateDestination.c",
				  "/joymove/renthis/viewRentHistory.c",
				  "/joymove/renthis/delRentHistory.c",
				  "/joymove/file/addImage.c",
				  "/joymove/config/addRedis.c",
				  "/joymove/config/updateRedis.c",
				  "/joymove/usermgr/viewBaseInfo.c",
				  "/joymove/usermgr/updatePwd.c",
				  "/joymove/usermgr/updateIma",
				  "/joymove/usermgr/getIma",
				  "/joymove/usermgr/updateInfo",
				  "/joymove/usermgr/updateDriverAuthInfo",
				  "/joymove/usermgr/viewJifen",
				  "/joymove/usermgr/viewCoupon",
				  "/joymove/checkUserMobileNo"
		  }; 
		  for(String whiteList:notCheckURL){
			  String URI = (String)req.getRequestURI();
			  if(URI.equals(whiteList))
				  return true;
		  }
		
		  String authToken = String.valueOf(jsonObj.get("authToken"));
		  
		  if(jsonObj !=null && authToken!=null && authToken.length() > 0) {

			 try {
				     JOYUser toAuthUser = new JOYUser();
				     toAuthUser.authToken = authToken;
					 List<JOYUser> users = joyUserService.getNeededUser(toAuthUser);
					 
					 if(users.size() > 0) {
						 JOYUser cUser = users.get(0);
						
						 String userToken = cUser.authToken; //getAuthToken();
						 if(userToken.equals(authToken)) {
							 return true;
							
							
						 }//if(userToken
					 }
			 } catch(Exception e) {
				
				 reObj.put("result","10004");
				 e.printStackTrace();
			 }
		  } 
		  
		  response.setContentType("application/json;charset=UTF-8");           
		  response.setHeader("Cache-Control", "no-cache");
		  response.getWriter().write(reObj.toString()); 
	      return false;
		}
	
}
