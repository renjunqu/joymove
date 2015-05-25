package com.joymove.view;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mongodb.morphia.Datastore;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.futuremove.cacheServer.utils.ConfigUtils;
import com.futuremove.cacheServer.utils.HttpPostUtils;
import com.joymove.entity.JOYCar;
import com.joymove.entity.JOYNCar;
import com.joymove.entity.JOYOrder;
import com.joymove.entity.JOYReserveOrder;
import com.joymove.entity.JOYUser;
import com.joymove.service.JOYNCarService;
import com.joymove.service.JOYNOrderService;
import com.joymove.service.JOYNReserveOrderService;
import com.joymove.service.JOYOrderService;
import com.joymove.service.JOYUserService;

@Scope("prototype")
@Controller("JOYNCarController")
public class JOYNCarController {
	
	final static Logger logger = LoggerFactory.getLogger("com.joymove.view");

	@Resource(name = "JOYNCarService")
	private JOYNCarService  joyNCarService;
	@Resource(name = "carService")
	private CarService      cacheCarService;
	@Resource(name = "JOYNOrderService")
	private JOYNOrderService joyNOrderService;
	@Resource(name = "JOYOrderService")
	private JOYOrderService joyOrderService;
	@Resource(name = "JOYUserService")
	private JOYUserService joyUserService;

	
	

	public JOYNCarController(JOYNCarService joyNCarService,
			CarService cacheCarService, JOYNOrderService joyNOrderService) {
		super();
		this.joyNCarService = joyNCarService;
		this.cacheCarService = cacheCarService;
		this.joyNOrderService = joyNOrderService;
	}

	public JOYNCarController() {
		super();
		// TODO Auto-generated constructor stub
	}

	/*   ============business proc =================*/
	
	
	@RequestMapping(value={"newcar/getNearByAvailableCars","newcar/getNearByBusyCars"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject findCars(HttpServletRequest req){
		logger.error("newcar findCars method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 JSONArray  carArray  = new JSONArray();
			//       sdfdsfdsf
		 Reobj.put("result", "10001");
		 Reobj.put("cars", carArray);
		 
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 likeCondition.put("userPositionX", jsonObj.get("userLongitude")==null ? 0.0: jsonObj.get("userLongitude") );
			 likeCondition.put("userPositionY", jsonObj.get("userLatitude")==null ? 0.0: jsonObj.get("userLatitude") );
			 likeCondition.put("scope", jsonObj.get("scope")==null ? 50 : jsonObj.get("scope") );
			 String URI = req.getRequestURI();
			 List<Car> cars = null;
			 if(URI.contains("getNearByAvailableCars")) {
				 cars = cacheCarService.getFreeCarByScope(likeCondition);
			 } else {
				 cars = cacheCarService.getBusyCarByScope(likeCondition);
			 }
			 if(cars!=null && cars.size()>0) {
				 for(Car car:cars) {
					 JSONObject car_json = new JSONObject();
					 car_json.put("carId", car.getVinNum());
					 car_json.put("longitude", car.getLongitude());
					 car_json.put("latitude", car.getLatitude());
					 likeCondition.put("vinNum",car.getVinNum());
					 List<JOYNCar>  ncars = joyNCarService.getNeededCar(likeCondition);
					 JOYNCar ncar = ncars.get(0);
					 
					 
					 car_json.put("desp", ncar.licensenum);
					 System.out.println("license num is "+ncar.licensenum);
					 if(URI.contains("getNearByAvailableCars")) {
						 
					 } else {
						 long timeScope = jsonObj.get("timeScope")==null? 60000:Long.valueOf(jsonObj.get("timeScope").toString());
						 car_json.put("eta",  (int)(Math.random()*timeScope) + System.currentTimeMillis());
					 }
					 carArray.add(car_json);
				 }
				 Reobj.put("result", "10000");
			 } else {
				 Reobj.put("result", "10002");
			 }
		 } catch(Exception e){
			 logger.error(e.toString());
			 Reobj.put("errMsg",e.toString());
		 }
		 return Reobj;
	}
	
	
	
	public static void main_test_get_cars(String []args){
		
		
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:**/applicationContext-mvc.xml");
			CarService carService = (CarService)context.getBean("carService");
			 Map<String,Object> likeCondition = new HashMap<String, Object>();
			 likeCondition.put("userPositionX",  116.1);
			 likeCondition.put("userPositionY",   39.78);
			 likeCondition.put("scope", 50000000);
			 List<Car> cars  = carService.getBusyCarByScope(likeCondition);
			 if(cars.size()>0) {
				 Car car = cars.get(0);
				 System.out.println(car.getLatitude());
				 System.out.println(car.getLongitude());
			 }
			 System.out.println("over");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/*******  operator want to insert a new car  *************/
	@RequestMapping(value={"newcar/registerCarReq"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject registerCarReq(HttpServletRequest req){
		 logger.error("registerCarReq method was invoked...");
		 
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 try {
			 
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 JOYNCar car =new JOYNCar();
			 car.vinNum = ((String)jsonObj.get("vinNum"));
			 car.registerState = (0);
			 car.RSAPubKey = (String.valueOf(UUID.randomUUID()));
			 car.RSAPriKey = (String.valueOf(UUID.randomUUID()));
			 logger.debug("qrj: try to add a new car with vin Num " + car.vinNum);
			 joyNCarService.insertCar(car);
			 logger.debug("qrj: insert ok, then send the vin number  code the cloudmove");
			 String timeStr = String.valueOf(System.currentTimeMillis());
			 String url = ConfigUtils.getPropValues("cloudmove.registerCar");
			 String data = "vin="+car.vinNum+"&time="+timeStr;
			 String result = HttpPostUtils.post(url, data);
			 logger.info("qrj: send data to cloudmove  success,data is ");
			 logger.info(data);
			 logger.info("qrj: now show the results");
			 logger.info(result);
			 Reobj.put("result", "10000");
		 } catch(Exception e){
			 logger.debug(e.toString());
		 }
		 return Reobj;
	}
	
	
	
	/*******  receive regiter packet from the cloudmove *************/
	@RequestMapping(value={"newcar/registerCarAck"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject registerCarAck(HttpServletRequest req){
		 logger.error("registerCarAck method was invoked...");
		 
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 
		 try {
			 logger.info("qrj: cloudmove tell the car send register packet to him ");
		 } catch(Exception e){
			 logger.debug(e.toString());
		 }
		 return Reobj;
	}
	
	/*****     send key to car failed or success ************/
	@RequestMapping(value={"newcar/sendKeyReq"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject sendKeyReq(HttpServletRequest req){
		 logger.error("registerCarAck method was invoked...");
		 Map<String,Object> condition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 try {
			 logger.debug("get the send key report from clouemove");
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 condition.put("vinNum", jsonObj.get("vinNum"));
			 List<JOYNCar> cars =  joyNCarService.getNeededCar(condition);
			 JOYNCar car = cars.get(0);
			
			 String url = ConfigUtils.getPropValues("cloudmove.sendKey");
			 String timeStr = String.valueOf(System.currentTimeMillis());
			 String data = "vin="+car.vinNum+"&cert="+car.RSAPubKey+"&time="+timeStr;
			 String result = HttpPostUtils.post(url, data);
			 logger.info("qrj: send data to cloudmove  success,data is ");
			 logger.info(data);
			 logger.info("qrj: now show the results");
			 logger.info(result);
			 Reobj.put("result", "10000");
		 } catch(Exception e){
			 logger.error("qrj:" + e.toString());
		 }
	
		 return Reobj;
	}
	
	
	
	 
	/*****     send key to car failed or success ************/
	@RequestMapping(value={"newcar/sendKeyAck"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject sendKeyAck(HttpServletRequest req){
		 logger.error("registerCarAck method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 
		 try {
			 logger.debug("get the send key report from clouemove");
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 JOYNCar car =new JOYNCar();
			 car.registerState = (1);
			 car.vinNum = ((String)jsonObj.get("vinNum"));
			 logger.debug("update car's register state ");
			 joyNCarService.updateCarRegisterState(car);
			 //add a new car entity to mongo 
			 logger.debug("now ,save the new car info into mongo");
			 Car cacheCar = cacheCarService.getByVinNum(car.vinNum);
			 if(cacheCar==null) {
				 cacheCar = new Car();
				 cacheCar.setVinNum(car.vinNum);
				 cacheCar.setLongitude(0.0);
				 cacheCar.setLatitude(0.0);
				 cacheCar.setState(Car.state_free);
				 cacheCarService.save(cacheCar);
				 logger.debug("now , register car ack ok");
			 }
		 } catch(Exception e){
			 logger.error(e.toString());
			 
		 }
	
		 return Reobj;
	}
	
	/***********  send code to car success or failed  *******************/
	//this method called by amqp
	@RequestMapping(value={"newcar/sendCodeAck"}, method=RequestMethod.POST)
	public   @ResponseBody JSONObject  sendCodeAck(HttpServletRequest req){
		 logger.error("sendCodeAck method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj = new JSONObject();
		 Reobj.put("result", "10001");
		 
		 try {
			 logger.debug("get the send code report from clouemove");
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 String vinNum = (String)jsonObj.get("vinNum");
			 Car car = new Car();
			 car.setVinNum(vinNum);
			 car = cacheCarService.getByVinNum(vinNum);
			 if(car.getState()==Car.state_wait_code) {
				 
				 JOYOrder order = new JOYOrder();
	    	     order.mobileNo = (car.getOwner());
	    	     order.carVinNum = (car.getVinNum());
	    	     joyNOrderService.insertNOrder(order);
	    	     //if exception happens, the car will be state in wait_code stage,wait the user's cancel
	    	     cacheCarService.updateCarStateBusy(car);
			 }
			 
			 Reobj.put("result", "10000");
			 logger.debug("now , send car ack ok");
		 } catch(Exception e){
			 logger.error(e.toString());
			 
		 }
	
		 return Reobj;
	}
	
	/*****     remote lock闂侀潧妫旈懣鍣塴ock闂侀潧妫旂粻宄歡e blow ************/
	@RequestMapping(value={"newcar/lock","newcar/unlock","newcar/blow","newcar/vague"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject carOperations(HttpServletRequest req){
		 logger.error("carOperations method was invoked...");
		 Map<String,Object> condition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");;
			 String vinNum = (String)jsonObj.get("carId");
			 String URI = req.getRequestURI();
			 String url = null;
			 String data = null; 
			 String timeStr = String.valueOf(System.currentTimeMillis());
			 if(URI.contains("unlock")) {
				 url = ConfigUtils.getPropValues("cloudmove.unlock");
				 data = "vin="+vinNum+"&time="+timeStr;
			 } else if(URI.contains("lock")) {
				 url = ConfigUtils.getPropValues("cloudmove.lock");
				 data = "vin="+vinNum+"&time="+timeStr;
			 } else if(URI.contains("blow")) {
				 url = ConfigUtils.getPropValues("cloudmove.blow");
				 data = "vin="+vinNum+"&time="+timeStr +"&duration=10&interval=1";
				 
			 } else if(URI.contains("vague")) {
				 url = ConfigUtils.getPropValues("cloudmove.vague");
				 data = "vin="+vinNum+"&time=" + timeStr + "&duration=10&interval=1";
			 }
			 String result = HttpPostUtils.post(url, data);
			 logger.info("qrj: send data to cloudmove  success,data is ");
			 logger.info(data);
			 logger.info("qrj: now show the results");
			 logger.info(result);
			 Reobj.put("result", "10000");
		 } catch(Exception e){
			 logger.error("qrj:" + e.toString());
		 }
	
		 return Reobj;
	}
	
	
	
	
	
	@RequestMapping(value={"newcar/rentCarReq"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject rentCarReq(HttpServletRequest req){
		 logger.error("rentCarReq method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 Car cacheCar = null;
		 Car car  = null;
		 try {    
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 String vinNum = jsonObj.get("carId").toString();
			 String mobileNo = jsonObj.get("mobileNo").toString();
			//first check the user's auth state =======================================================
			 JOYUser user = new JOYUser();
			 user.mobileNo = mobileNo; //setMobileNo(mobileNo);
			 String authStateErrMsg = joyUserService.checkUserState(user);
			 if(authStateErrMsg!=null) {
				 Reobj.put("result","10004");
				 Reobj.put("errMsg", authStateErrMsg);
				 return Reobj;
			 }
			 //then check tue user's rent state
			 cacheCar = new Car();
			 cacheCar.setOwner(mobileNo);
			 cacheCar.setState(Car.state_free);
			 cacheCar =  cacheCarService.getByOwnerAndNotState(cacheCar);
			 if(cacheCar!=null && cacheCar.getVinNum().equals(vinNum)) {
				 car = cacheCar;
			 } else if(cacheCar==null){
				 	car  = cacheCarService.getByVinNum(vinNum); 
			 } else {
				 Reobj.put("errMsg", "alread rent or reserved");
				 return Reobj;
			 }
			 //check the order state 
			 likeCondition.put("mobileNo", jsonObj.get("mobileNo"));
			 likeCondition.put("delMark", JOYOrder.NON_DEL_MARK);
			 List<JOYOrder> orders = joyOrderService.getNeededOrder(likeCondition);
			 if(orders.size()>0) {
			     Reobj.put("errMsg", "has not payed order");
				 return Reobj;
				 
			 }
			 //then test the car ===================================================================
			 if(car!=null && ((car.getState() == Car.state_free) || (car.getState()==Car.state_reserved  && car.getOwner().equals(mobileNo)))) {
				 car.setOwner(mobileNo);
				 cacheCarService.updateCarStateWaitCode(car);
				 car  = cacheCarService.getByVinNum(car.getVinNum()); 
				 //check if update success
				 if(car.getState()==Car.state_wait_code && car.getOwner().equals(mobileNo)) {
						 //send auth code to cloudmove  *************************************
						 String timeStr = String.valueOf(System.currentTimeMillis());
						 String url = ConfigUtils.getPropValues("cloudmove.sendAuth");
						 String data = "time="+timeStr+"&vin="+car.getVinNum()+"&auth=123456";
						 String result = HttpPostUtils.post(url, data);
						 logger.info("send data to cloudmove  success,data is ");
						 logger.info(data);
						 logger.info("now show the results");
						 logger.info(result);
						 Reobj.put("result", "10000");  
				 } else {
					 Reobj.put("errMsg", "租车失败，该车已经被其他用户占用，请换租其他车辆");
				 } 
				 
			 }  else if (car!=null && car.getState() == Car.state_wait_code  && car.getOwner().equals(mobileNo)){
				 //trust in cm, this state will be changed
				 /*
				 JSONObject json = new JSONObject();
				 json.put("vin", car.getVinNum());
				 json.put("auth","123456");
				 SimpleDateFormat   dateFormatter   =   new   SimpleDateFormat   ("yyyy-MM-dd   HH:mm:ss     ");   
				 json.put("time", dateFormatter.format(new   Date(System.currentTimeMillis())));
				 String url = ConfigUtils.getPropValues("cloudmove.sendAuth");
				 String result = HttpPostUtils.post(url, json);
				 logger.info("send data to cloudmove  success,data is ");
				 logger.info(json.toJSONString());
				 logger.info("now show the results");
				 logger.info(result);
				 Reobj.put("result", "10000");
				 */
			 } else  {
				 Reobj.put("errMsg", "Car state not right");
			 }
		 } catch(Exception e){
			 Reobj.put("errMsg", e.toString());
			 logger.error(e.toString());
		 }
	
		 return Reobj;
	}
	
	
	
	
	@RequestMapping(value={"newcar/rentCarAck"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject rentCarAck(HttpServletRequest req){
		logger.error("rentCarAck method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		 try {
			 //check if the car in state_wait_code or state_busy, and the owner equals the mobileNo
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 String vinNum = (String)jsonObj.get("carId");
			 String mobileNo = (String)jsonObj.get("mobileNo");
			 Car car = cacheCarService.getByVinNum(vinNum);
			 if(mobileNo.equals(car.getOwner())) {
				 if(car.getState() == Car.state_wait_code) {
					 Reobj.put("result", "10001");
					 Reobj.put("errMsg","not ready");
				 } else if (car.getState() == Car.state_busy) {
					 // the order alreay created, now return the orderId
					 likeCondition.put("carVinNum", car.getVinNum());
					 likeCondition.put("mobileNo", car.getOwner());
					 likeCondition.put("delMark", JOYOrder.NON_DEL_MARK);
					 List<JOYOrder> orders = joyNOrderService.getNeededOrder(likeCondition);
					 JOYOrder cOrder = orders.get(0);
					 Reobj.put("orderId", cOrder.id);
		    	     Reobj.put("carId", cOrder.carVinNum);
		    	     Reobj.put("startTime", cOrder.startTime.getTime());
		    	     Reobj.put("authCode", "123456");
					 Reobj.put("result", "10000");
				 }
			 } else {
				 Reobj.put("errMsg", "wrong state");
			 }
		 } catch(Exception e){
			 logger.error(e.toString());
			 Reobj.put("errMsg",e.toString());
		 }
		 return Reobj;
	}
	
	@RequestMapping(value={"newcar/rentCarTerminate"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject rentCarTerminate(HttpServletRequest req){
		 System.out.println("rentCarTerminate method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		 try {
			//check if the car in state_wait_code or state_busy, and the owner equals the mobileNo
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 String vinNum = (String)jsonObj.get("carId");
			 String mobileNo = (String)jsonObj.get("mobileNo");
			 Car car = cacheCarService.getByVinNum(vinNum);
			 if(mobileNo.equals(car.getOwner()) ) {
				 	
				     if(car.getState() == Car.state_wait_code ){
				    	 boolean result = joyNOrderService.updateOrderCancel(car);
				    	 if(result) {
				    		 Reobj.put("result", "10000");
				    	 }
				     } else if(car.getState() == Car.state_busy){
				    	 joyNOrderService.updateOrderTermiate(car);
				    	 Reobj.put("result", "10000");
				     }
			 } else {
				 	Reobj.put("errMsg","not ordered");		 
			 }
			
		 } catch(Exception e){
			 logger.error(e.toString());
			 Reobj.put("errMsg",e.toString());		 			 
		 }
		 return Reobj;
	}
	
	
	@RequestMapping(value="newcar/changeBatonMode", method=RequestMethod.POST)
	public  @ResponseBody JSONObject changeBatonMode(HttpServletRequest req){
		 System.out.println("new car changeBatonMode method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			   JOYOrder cOrder = new JOYOrder();
			   cOrder.mobileNo = ((String)jsonObj.get("mobileNo"));
			   cOrder.carVinNum = (jsonObj.get("carId").toString());
	    	   cOrder.delMark = (JOYReserveOrder.NODEL_FLAG);
	    	   cOrder.batonMode = (((Long)jsonObj.get("batonMode")).intValue());
	    	   joyNOrderService.changeNBatonMode(cOrder);			 
			   Reobj.put("result", "10000");
		 } catch(Exception e){
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	@RequestMapping(value="newcar/updateDestination", method=RequestMethod.POST)
	public  @ResponseBody JSONObject updateDestination(HttpServletRequest req){
		 System.out.println("new car updateDestination method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 JOYOrder cOrder = new JOYOrder();
			 cOrder.mobileNo = ((String)jsonObj.get("mobileNo"));
			 cOrder.carVinNum = (jsonObj.get("carId").toString());
	    	 cOrder.delMark = (JOYReserveOrder.NODEL_FLAG);
	    	 
	    	 String destination = jsonObj.get("destination").toString();
	    	 cOrder.destination = (destination);
	    	 joyNOrderService.updateNDestination(cOrder);	 
			 Reobj.put("result", "10000");
		 } catch(Exception e){
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	


	
	
	
	
	

}
