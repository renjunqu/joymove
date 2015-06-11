package com.joymove.interceptors.joyCarController;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.json.simple.*;

import com.joymove.entity.JOYOrder;
import com.joymove.service.JOYOrderService;


public class CheckOrderNotExists extends HandlerInterceptorAdapter {
	
	private JOYOrderService   joyOrderService;
	
	
	

	public JOYOrderService getJoyOrderService() {
		return joyOrderService;
	}




	public void setJoyOrderService(JOYOrderService joyOrderService) {
		this.joyOrderService = joyOrderService;
	}

	
	



	@Override  
	public boolean preHandle(HttpServletRequest req,
			HttpServletResponse response, Object handler) throws Exception {
		    Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
		    
			JSONObject reObj = new JSONObject();
			Object orderId = jsonObj.get("orderId");
			
			try {
				   JOYOrder orderFilter = new JOYOrder();
					Map<String,Object> likeCondition = new HashMap<String, Object>();
    				likeCondition.put("id", orderId);
				    orderFilter.id = Integer.parseInt(String.valueOf(orderId));
    				JOYOrder order =  joyOrderService.getNeededRecord(orderFilter);
    				if(order !=null) {
    					req.setAttribute("cOrder", order);
    					return true;
    				} 
 			} catch(Exception e){
 				System.out.println(e.toString());
				
			}
			
		  reObj.put("result","10003");
	      response.setContentType("application/json;charset=UTF-8");           
		  response.setHeader("Cache-Control", "no-cache");
		  response.getWriter().write(reObj.toString()); 
	      return false;
		}
	
}
