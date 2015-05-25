package com.joymove.util;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by qurj on 15/5/13.
 */
public class JOYExceptionHandler implements HandlerExceptionResolver {


    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception ex) {
        // TODO Auto-generated method stub
       // System.out.println(ex.getStackTrace());
        Map<String,Object> context = new HashMap<String, Object>();
        context.put("exception",ex);
        //System.out.println("Hello errorPage");
        return new ModelAndView("errorPage",context);
    }

}
