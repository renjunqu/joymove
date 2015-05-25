package com.joymove.interceptors;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.json.simple.*;
import org.json.simple.parser.*;

import com.joymove.util.JsonHashUtils;



public class JsonChecker extends HandlerInterceptorAdapter {
	
	@Override  
	public boolean preHandle(HttpServletRequest req,
			HttpServletResponse response, Object handler) throws Exception {
		
		  String URI = req.getRequestURI();
		  System.out.println(URI);
		  Hashtable<String,String[]> jsonCheckProps = new Hashtable<String,String[]>();
	    	
		 
	      jsonCheckProps.put("/joymove/rent/unlockBike.c", new String[] {
	    			"orderId","userId","bikeCode"
	    	});
	      jsonCheckProps.put("/joymove/rent/unlockBikeAck.c", new String[] {
	    			"orderId","userId","bikeCode"
	    	});
	      jsonCheckProps.put("/joymove/rent/lockBike.c", new String[] {
	    			"orderId"
	    	});
	      jsonCheckProps.put("/joymove/rent/lockBikeAck.c", new String[] {
	    			"orderId"
	    	});
	      jsonCheckProps.put("/joymove/rent/lockBike.c", new String[] {
	    			"orderId"
	    	});
	      jsonCheckProps.put("/joymove/rent/lockBikeAck.c", new String[] {
	    			"orderId"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/registerMBKUser.c", new String[] {
	    		  "userId","username","mobileNo","userpwd"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/modifyMBKUser.c", new String[] {
	    		  "userId"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/modifyMBKUserpwd.c", new String[] {
	    		  "userId","userpwd"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/viewMBKUserInfo.c", new String[] {
	    		  "userId"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/login.c", new String[] {
	    		  "mobileNo","password"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/checkUserIdExist.c", new String[] {
	    		  "userId"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/mobileNoExists.c", new String[] {
	    		  "mobileNo"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/checkUserNameExist.c", new String[] {
	    		  "username"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/dynamicPwsGen.c", new String[] {
	    		  "mobileNo"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/dynamicPwsVeri.c", new String[] {
	    		  "phoneNo","number"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/mbkbalance.c", new String[] {
	    		  "userId"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/mbkcoupon.c", new String[] {
	    		  "userId"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/viewJifen.c", new String[] {
	    		  "userId"
	    	});
	      jsonCheckProps.put("/joymove/renthis/viewRentHistory.c", new String[] {
	    		  "userId","pageStart","pageEnd"
	    	});
	      jsonCheckProps.put("/joymove/renthis/delRentHistory.c", new String[] {
	    		  "orderId"
	    	});
	      jsonCheckProps.put("/joymove/renthis/distanceRownum.c", new String[] {
	    		  "userId"
	    	});
	      jsonCheckProps.put("/joymove/renthis/timeTrip.c", new String[] {
	    		  "userId","orderId"
	    	});
	      jsonCheckProps.put("/joymove/renthis/couPonPay.c", new String[] {
	    		  "userId","couponIds"
	    	});
	      jsonCheckProps.put("/joymove/social/rank.c", new String[] {
	    		  "userId"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/viewMBKUserInfo.c", new String[] {
	    		  "userId"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/modifyMBKUserImage.c", new String[] {
	    		  "userId","image"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/faultImage.c", new String[] {
	    		  "userId","content","typeTime","image"
	    	});
	      
	      jsonCheckProps.put("/joymove/usermgr/register.c", new String[] {
	    		  "mobileNo","code","password"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/viewBaseInfo.c", new String[] {
	    		  "mobileNo"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/updatePwd.c", new String[] {
	    		  "mobileNo","password","changepassword"
	    	});	
	      jsonCheckProps.put("/joymove/usermgr/updateIma.c", new String[] {
	    		  "mobileNo","image"
	    	});	
	      jsonCheckProps.put("/joymove/usermgr/getIma.c", new String[] {
	    		  "mobileNo"
	    	});	
	      jsonCheckProps.put("/joymove/usermgr/updateInfo.c", new String[] {
	    		  "mobileNo"
	    	});	
	      jsonCheckProps.put("/joymove/usermgr/updateDriverAuthInfo.c", new String[] {
	    		  "mobileNo","driverNumber","image"
	    	});	
	      jsonCheckProps.put("/joymove/usermgr/viewJifen.c", new String[] {
	    		  "mobileNo"
	    	});
	      jsonCheckProps.put("/joymove/usermgr/viewCoupon.c", new String[] {
	    		  "mobileNo"
	    	});	
	      jsonCheckProps.put("/joymove/usermgr/checkUserMobileNo.c", new String[] {
	    		  "mobileNo"
	    	});
	      
	      
		  if(req.getAttribute("jsonArgs") !=null) {
			  Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			  String reqURI = (String)req.getRequestURI();
			  System.out.println(reqURI);
			  String[] neededProps = jsonCheckProps.get(req.getRequestURI());
			  if(neededProps != null && neededProps.length>0 && JsonHashUtils.checkJsonProps(jsonObj,neededProps)) {
				 
				     return true;
			  } else if(neededProps == null) {
				  
				  return true;
			  }
		  }
		  JSONObject reObj = new JSONObject();
		  reObj.put("result","10003");
		  response.setContentType("application/json;charset=UTF-8");           
		  response.setHeader("Cache-Control", "no-cache");
		  response.getWriter().write(reObj.toString()); 
	      return false;
		}
	
}
