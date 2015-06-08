package com.joymove.view;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.futuremove.cacheServer.concurrent.CarOpLock;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.joymove.service.*;




@Controller("JOYNReserveOrderController")
public class JOYNReserveOrderController {
	@Resource(name = "carService")
	private CarService      cacheCarService;


	@Resource(name = "JOYNReserveOrderService")
	private JOYNReserveOrderService joyNReserveOrderService;

	public JOYNReserveOrderController() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/*************  business proc   ********************/
	
	
	@RequestMapping(value="newcar/createReserveOrder", method=RequestMethod.POST)
	public  @ResponseBody JSONObject createReserveOrder(HttpServletRequest req){
		 System.out.println("getNearByParks method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 JSONArray  parkArray  = new JSONArray();
		 ReentrantLock optLock = null;
		
		 Reobj.put("result", "10001");
		 Car cacheCar = null;
		 try {
			 //then test the car ===================================================================

			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 //first check if the user already rent or reserve a car
			    cacheCar = new Car();
			    cacheCar.setOwner((String)jsonObj.get("mobileNo"));
			    cacheCar.setState(Car.state_free);
			 	cacheCar =  cacheCarService.getByOwnerAndNotState(cacheCar);
			 if(cacheCar==null) {
				 //first check the car's state  
				 optLock = CarOpLock.getCarLock((String) jsonObj.get("carId"));
				 //start lock //锁锁锁  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				 if(optLock.tryLock()) {
					 cacheCar = cacheCarService.getByVinNum((String)jsonObj.get("carId"));
					 if(cacheCar.getState()==Car.state_free) {
						 //do the reserve action
						 cacheCar.setOwner((String)jsonObj.get("mobileNo"));
						 cacheCarService.updateCarStateReserved(cacheCar);
						 joyNReserveOrderService.insertReserveOrder(cacheCar);
					 } else {
						 Reobj.put("errMsg", "car not free");
					 }
					 optLock.unlock();
				  }
			 } else {
				 Reobj.put("errMsg", "already reserved or rented");
			 } 
			
			 
		 } catch(Exception e) {
			 if(optLock!=null && optLock.getHoldCount()>0) {

				 if(cacheCar!=null) {
					 cacheCarService.updateCarStateFree(cacheCar);
				 }
				 optLock.unlock();
			 }
			 Reobj.put("result", "10001");
			 System.out.println(e);
		 }
		 return Reobj;
	}

	
	@RequestMapping(value="newcar/cancelReserveOrder", method=RequestMethod.POST)
	public  @ResponseBody JSONObject cancelReserveOrder(HttpServletRequest req){
		 System.out.println("getNearByParks method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 JSONArray  parkArray  = new JSONArray();
		ReentrantLock optLock = null;
		
		 Reobj.put("result", "10001");
		 Car cacheCar = null;
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 cacheCar = new Car();
			 cacheCar.setOwner((String)jsonObj.get("mobileNo"));
			 cacheCar.setState(Car.state_free);
			 cacheCar =  cacheCarService.getByOwnerAndNotState(cacheCar);

			 if(cacheCar==null || cacheCar.getState()!=Car.state_reserved) {
				 Reobj.put("errMsg", "car not reserved by you ");
			 } else {
				 //clear the car state
				 optLock = CarOpLock.getCarLock(cacheCar.getVinNum());
				 if (optLock.tryLock()) {
					 joyNReserveOrderService.updateReserveOrderDelFlag((String) jsonObj.get("mobileNo"));
					 cacheCarService.clearExpireReserve(cacheCar);
					 Reobj.put("result", "10000");
					 optLock.unlock();
				 }
			 }
			 
		 } catch(Exception e) {
			 if(optLock!=null && optLock.getHoldCount()>0)
				 optLock.unlock();
			 Reobj.put("result", "10001");
			 System.out.println(e);
		 }
		 return Reobj;
	}

	
	

}
