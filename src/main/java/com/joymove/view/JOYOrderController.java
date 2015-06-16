package com.joymove.view;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.futuremove.cacheServer.service.CarService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joymove.entity.JOYOrder;
import com.joymove.service.JOYCarService;
import com.joymove.service.JOYOrderService;



@Controller("JOYOrderController")
@RequestMapping("/ordermgr")
public class JOYOrderController {

	final static Logger logger = LoggerFactory.getLogger(JOYOrderController.class);



	@Resource(name = "JOYOrderService")
	private JOYOrderService joyOrderService;

	@Resource(name = "JOYCarService")
	private JOYCarService joyCarService;

	@Resource(name = "carService")
	private CarService cacheCarService;

	
	

	/***            ****/
	@RequestMapping(value = "/listHistoryOrder", method = { RequestMethod.POST, RequestMethod.GET })
    public @ResponseBody JSONObject listHistoryOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
		 logger.trace("listHistoryOrder method was invoked...");
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
				 if(order.carId==null) {
					 orderJSON.put("carId", order.carVinNum);
				 } else {
					 orderJSON.put("carId", order.carId);
				 }
				 orderJSON.put("fee", order.getTotalFee());
				 orderJSON.put("miles", order.getTotalFee()*3.1314);
				 orderJSON.put("destinations", order.destination);
				 orderJSON.put("startLongitude", order.startLongitude);
				 orderJSON.put("startLatitude", order.startLatitude);
				 orderJSON.put("stopLatitude", order.stopLatitude);
				 orderJSON.put("stopLongitude", order.stopLongitude);
				 orderArray.add(orderJSON);
			 }
			 Reobj.put("result", "10000");
		
			 
		 } catch(Exception e) {
			 Reobj.put("result", "10001");
			 logger.error(e.getStackTrace().toString());
		 }
		 return Reobj;
    }
	
	
	
	
	
	

}
