package com.joymove.view;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joymove.entity.JOYCar;
import com.joymove.entity.JOYOrder;
import com.joymove.service.JOYCarService;
import com.joymove.service.JOYOrderService;


@Scope("prototype")
@Controller("JOYOrderController")
@RequestMapping("/ordermgr")
public class JOYOrderController {

	@Resource(name = "JOYOrderService")
	private JOYOrderService joyOrderService;

	@Resource(name = "JOYCarService")
	private JOYCarService joyCarService;
	
	

	/***            ****/
	@RequestMapping(value = "/listHistoryOrder", method = { RequestMethod.POST, RequestMethod.GET })
    public @ResponseBody JSONObject listHistoryOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
		 System.out.println("listHistoryOrder method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 JSONArray  orderArray  = new JSONArray();
		//       sdfdsfdsf
		 Reobj.put("result", "10001");
		 Reobj.put("orders", orderArray);
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)request.getAttribute("jsonArgs");;
			 likeCondition.put("mobileNo", jsonObj.get("mobileNo"));
			 if(jsonObj.get("startId")!=null && Integer.parseInt(jsonObj.get("startId").toString())!=0) {
				 likeCondition.put("startId", Long.parseLong(jsonObj.get("startId").toString()) );
			 }
			 likeCondition.put("delMark", JOYOrder.DEL_MARK);
			 
			 List<JOYOrder> orders = joyOrderService.getNeededOrder(likeCondition);
			 Reobj.put("orderCount", orders.size());
			 for(JOYOrder order:orders) {
				 JSONObject orderJSON = new JSONObject();
				 orderJSON.put("orderId",order.id);
				 orderJSON.put("startTime", order.startTime);
				 orderJSON.put("stopTime", order.stopTime);
				 orderJSON.put("carId", order.carId);
				 orderJSON.put("fee", order.getTotalFee());
				 orderJSON.put("miles", order.getTotalFee()*3.1314);
				 orderJSON.put("destinations", order.destination);
				 likeCondition.put("id", order.carId);
				 List<JOYCar> cars = joyCarService.getCarById(likeCondition);
				 JOYCar currCar = cars.get(0);
				 orderJSON.put("startLongitude", currCar.positionX);
				 orderJSON.put("startLatitude", currCar.positionX);
				 orderArray.add(orderJSON);
			 }
			 Reobj.put("result", "10000");
		
			 
		 } catch(Exception e) {
			 Reobj.put("result", "10001");
			 System.out.println(e);
		 }
		 return Reobj;
    }
	
	
	
	
	
	

}
