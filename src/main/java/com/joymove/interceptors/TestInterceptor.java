package com.joymove.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.json.simple.*;
import org.json.simple.parser.*;

public class TestInterceptor extends HandlerInterceptorAdapter{
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		//logger.trace("I am interceptor2 "+ request.getRequestURI());
		JSONObject ttt = new JSONObject();
		ttt.put("hello", "world");
		response.setContentType("text/x-json;charset=UTF-8");           
	    response.setHeader("Cache-Control", "no-cache");
	    response.getWriter().write(ttt.toString());    
		return true;
	}
	
}
