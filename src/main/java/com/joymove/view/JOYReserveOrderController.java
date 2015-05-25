package com.joymove.view;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoaderListener;

import com.joymove.service.*;
import com.joymove.entity.*;
import java.util.Timer;
import java.util.TimerTask;




@Scope("prototype")
@Controller("JOYReserveOrderController")
public class JOYReserveOrderController {
	@Resource(name = "JOYReserveOrderService")
	private JOYReserveOrderService joyReserveOrderService;
	@Resource(name = "JOYCarService")
	private JOYCarService joyCarService;


	@RequestMapping(value={"reserve/createReserveOrder"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject createReserveOrder(HttpServletRequest req){
		 System.out.println("createReserve method was invoked...");
		 JSONObject Reobj=new JSONObject();
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 Reobj.put("result", "10001");
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 likeCondition.put("carId", jsonObj.get("carId"));
			 likeCondition.put("delFlag", JOYReserveOrder.NODEL_FLAG);
			 likeCondition.put("expSeconds", JOYReserveOrder.EXPIRE_SECONDS);
			 
			 List<JOYReserveOrder> reOrders = joyReserveOrderService.getNeededReserveOrder(likeCondition);
			 likeCondition.put("id", jsonObj.get("carId"));
		      //check the state of this car, it muse be in car_free
		      List<JOYCar> cars = joyCarService.getCarById(likeCondition);
			 if(reOrders.size()==0 && cars.size()==1) {
				     JOYCar car = cars.get(0);
				     //check the car's state
				     if(car.state==JOYCar.STATE_FREE) {
					      JOYReserveOrder reorder = new   JOYReserveOrder();
					      reorder.carId = (((Long)jsonObj.get("carId")).intValue());
					      reorder.mobileNo = ((String)jsonObj.get("mobileNo"));
					      boolean result = joyReserveOrderService.insertReserveOrder(reorder);
					      if(result){
					    	  Reobj.put("result","10000");
					    	  
					      } else {
					    	  Reobj.put("result","10001");
					    	  Reobj.put("errMsg","reserve failed"); 
					      }
				     }
			 } else {
				 JOYReserveOrder cReserveOrder = reOrders.get(0);
				 Reobj.put("mobileNo", cReserveOrder.mobileNo);
				 Reobj.put("errMsg", "already reserved");
			 }
		 } catch(Exception e){
			 Reobj.put("errMsg",e.toString());
		 }
		 
		 return Reobj;
	}
	
	
	@RequestMapping(value="reserve/cancelReserveOrder", method=RequestMethod.POST)
	public  @ResponseBody JSONObject cancelReserveOrder(HttpServletRequest req){
		 System.out.println("getNearByAvailableCars method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 
		//       sdfdsfdsf
		 Reobj.put("result", "10001");
		 
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 likeCondition.put("mobileNo", jsonObj.get("mobileNo"));
			 likeCondition.put("delFlag", JOYReserveOrder.NODEL_FLAG);
			 likeCondition.put("expSeconds", JOYReserveOrder.EXPIRE_SECONDS);
			 List<JOYReserveOrder> reOrders = joyReserveOrderService.getNeededReserveOrder(likeCondition);
			 for(JOYReserveOrder order:reOrders) {
				 joyReserveOrderService.updateReserveOrderDelFlag(order);
			 }
			 Reobj.put("result", "10000");
		 } catch(Exception e) {
			 Reobj.put("result", "10001");
			 Reobj.put("errMsg", e.toString());
			 System.out.println(e);
		 }
		 return Reobj;
	}
	
	
	

}
