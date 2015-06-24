package com.joymove.view;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.futuremove.cacheServer.concurrent.CarOpLock;
import com.joymove.entity.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.futuremove.cacheServer.utils.ConfigUtils;
import com.futuremove.cacheServer.utils.HttpPostUtils;
import com.joymove.service.JOYNCarService;
import com.joymove.service.JOYNOrderService;
import com.joymove.service.JOYOrderService;
import com.joymove.service.JOYUserService;


@Controller("JOYNCarController")
public class JOYNCarController {
	
	final static Logger logger = LoggerFactory.getLogger(JOYNCarController.class);

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
					 JOYNCar filterObj = new JOYNCar();
					 filterObj.vinNum = car.getVinNum();
					 List<JOYNCar>  ncars = joyNCarService.getNeededList(filterObj);
					 JOYNCar ncar = ncars.get(0);
					 
					 
					 car_json.put("desp", ncar.licensenum);
					 car_json.put("ifBlueTeeth",ncar.ifBlueTeeth);
					 car_json.put("carInfo",ncar.carInfo);
					 car_json.put("imageUrl",ncar.imageUrl);
					 car_json.put("powerType",ncar.powerType);
					 car_json.put("powerPercent",ncar.powerPercent);
					 logger.trace("license num is " + ncar.licensenum);
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
			 logger.error("exception:",e);
			 Reobj.put("errMsg",e.toString());
		 }
		 return Reobj;
	}

	@RequestMapping(value={"newcar/getCarDetailInfo"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject getCarDetailInfo(HttpServletRequest req){
		logger.trace("getCarDetailInfo method was invoked...");
		Map<String,Object> likeCondition = new HashMap<String, Object>();
		JSONObject Reobj=new JSONObject();
		//       sdfdsfdsf
		Reobj.put("result", "10001");

		try {
			Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");;
			JOYNCar carFilter = new JOYNCar();
			String  vinNum = String.valueOf(jsonObj.get("carId"));
			Car cacheCar = cacheCarService.getByVinNum(vinNum);
			carFilter.vinNum = vinNum;
			JOYNCar ncar = joyNCarService.getNeededRecord(carFilter);
			if(ncar==null || cacheCar==null) {
				Reobj.put("result","10002");
				Reobj.put("errMsg","车辆ID不正确");
			} else {
				Reobj.put("longitude",  cacheCar.getLongitude());
				Reobj.put("latitude",  cacheCar.getLatitude());
				Reobj.put("desp",  ncar.licensenum);
				Reobj.put("ifBlueTeeth",ncar.ifBlueTeeth);
				Reobj.put("carInfo",ncar.carInfo);
				Reobj.put("imageUrl",ncar.imageUrl);
				Reobj.put("powerType",ncar.powerType);
				Reobj.put("powerPercent",ncar.powerPercent);
			}
		} catch(Exception e) {
			Reobj.put("result", "10001");
			logger.trace("exception: ", e);
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
				 logger.trace(car.getLatitude().toString());
				 logger.trace(car.getLongitude().toString());
			 }
			 logger.trace("over");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace().toString());
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
			 joyNCarService.insertRecord(car);
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
	
	
	

	/*****     send key to car failed or success ************/
	@RequestMapping(value={"newcar/sendKeyReq"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject sendKeyReq(HttpServletRequest req){
		 logger.error("sendKeyReq method was invoked...");
		 Map<String,Object> condition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 try {
			 logger.debug("get the send key report from clouemove");
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 condition.put("vinNum", jsonObj.get("vinNum"));
			 JOYNCar filterObj = new JOYNCar();
			 filterObj.vinNum = String.valueOf(jsonObj.get("vinNum"));
			 List<JOYNCar> cars =  joyNCarService.getNeededList(filterObj);
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
	

	/*****     车辆操作 ************/
	@RequestMapping(value={"newcar/lock","newcar/unlock","newcar/blow","newcar/vague"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject carOperations(HttpServletRequest req){
		 logger.error("carOperations method was invoked...");
		 Map<String,Object> condition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");;
			 String vinNum = (String)jsonObj.get("carId");
			 JOYNCar filterObj = new JOYNCar();
			 filterObj.vinNum = vinNum;
			 String URI = req.getRequestURI();
			 String url = null;
			 String data = null; 
			 String timeStr = String.valueOf(System.currentTimeMillis());
			 String operation = "";
			 String result="";
			 JSONParser parser = new JSONParser();
			 Integer opResult = 0;
			 if(URI.contains("unlock")) {
				 operation  = "unlock";
				 url = ConfigUtils.getPropValues("cloudmove.unlock");
				 data = "vin="+vinNum+"&time="+timeStr;
				 result = HttpPostUtils.post(url, data);
				 JSONObject cmObj = (JSONObject)parser.parse(result);
				 opResult = Integer.parseInt(cmObj.get("result").toString());
				 if(opResult==1) {
					 Reobj.put("result","10000");
					 JOYNCar valueObj = new JOYNCar();
					 valueObj.lockState = 0;
					 joyNCarService.updateRecord(valueObj,filterObj);
				 }

			 } else if(URI.contains("lock")) {
				 operation  = "lock";
				 url = ConfigUtils.getPropValues("cloudmove.lock");
				 data = "vin="+vinNum+"&time="+timeStr;
				 result = HttpPostUtils.post(url, data);
				 JSONObject cmObj = (JSONObject)parser.parse(result);
				 opResult = Integer.parseInt(cmObj.get("result").toString());
				 if(opResult==1) {
					 Reobj.put("result","10000");
					 JOYNCar valueObj = new JOYNCar();
					 valueObj.lockState = 1;
					 joyNCarService.updateRecord(valueObj,filterObj);
				 }
			 } else if(URI.contains("blow")) {
				 operation  = "blow";
				 url = ConfigUtils.getPropValues("cloudmove.blow");
				 String duration = ConfigUtils.getPropValues("caropt.duration");
				 String interval = ConfigUtils.getPropValues("caropt.interval");
				 String times = ConfigUtils.getPropValues("caropt.times");
				 data = "vin="+vinNum+"&time="+timeStr +"&duration="+duration+"&interval="+interval+"&times="+times;
				 result = HttpPostUtils.post(url, data);
				 JSONObject cmObj = (JSONObject)parser.parse(result);
				 opResult = Integer.parseInt(cmObj.get("result").toString());
				 if(opResult==1) {
					 Reobj.put("result","10000");

				 }
			 } else if(URI.contains("vague")) {
				 operation  = "vague";
				 url = ConfigUtils.getPropValues("cloudmove.vague");
				 String duration = ConfigUtils.getPropValues("caropt.duration");
				 String interval = ConfigUtils.getPropValues("caropt.interval");
				 String times = ConfigUtils.getPropValues("caropt.times");
				 data = "vin="+vinNum+"&time=" + timeStr + "&duration="+duration+"&interval="+duration+"&times="+times;
				 result = HttpPostUtils.post(url, data);
				 JSONObject cmObj = (JSONObject)parser.parse(result);
				 opResult = Integer.parseInt(cmObj.get("result").toString());
				 if(opResult==1) {
					 Reobj.put("result","10000");

				 }
			 }



			// Reobj.put("result", "10000");
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
		JOYNCar ncar = null;
		ReentrantLock optLock = null;
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
				Reobj.put("errMsg", "之前还有未支付的订单。");
				return Reobj;
			}
			//check the order state
			JOYOrder orderFilter = new JOYOrder();
			orderFilter.mobileNo = String.valueOf(jsonObj.get("mobileNo"));
			orderFilter.delMark = JOYOrder.NON_DEL_MARK;
			List<JOYOrder> orders = joyOrderService.getNeededList(orderFilter);
			if(orders.size()>0) {
				Reobj.put("errMsg", "之前还有未支付的订单。");
				return Reobj;

			}
			//then test the car ===================================================================
			optLock = CarOpLock.getCarLock(vinNum);
			//start lock //锁锁锁  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			if(optLock.tryLock()) {
				//获取到锁之后必须重新获取一次车的状态
				car = cacheCarService.getByVinNum(vinNum);
				if (car != null && ((car.getState() == Car.state_free) || (car.getState() == Car.state_reserved && car.getOwner().equals(mobileNo)))) {
					car.setOwner(mobileNo);
					//因为有了锁，可以认为肯定成功
					cacheCarService.updateCarStateWaitSendCode(car);
					if (cacheCarService.sendAuthCode(car.getVinNum())) {
						//cloudmove 已经成功接收到下发授权码的命令
						Reobj.put("result", "10000");
					} else {
						//偷偷取消奥,cm 下发失败了
						car.setState(null);
						car.setOwner("");
						cacheCarService.updateCarStateFree(car);
					}
				} else {
					Reobj.put("errMsg", "车辆不是空闲状态");
				}
				optLock.unlock();
			}
		} catch(Exception e){
			if(optLock!=null && optLock.getHoldCount() > 0) {
				    cacheCarService.updateCarStateFree(car);
                    optLock.unlock();
			}
			logger.error(e.getStackTrace().toString());
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
				 if(car.getState() == Car.state_wait_sendcode||car.getState()==Car.state_wait_poweron) {
					 Reobj.put("result", "10005");
					 Reobj.put("errMsg", "车辆还没准备好");
				 } else if (car.getState() == Car.state_busy) {
					 // the order alreay created, now return the orderId
					 JOYOrder orderFilter = new JOYOrder();
					 orderFilter.carVinNum = car.getVinNum();
					 orderFilter.mobileNo = car.getOwner();
					 orderFilter.delMark = JOYOrder.NON_DEL_MARK;
					 List<JOYOrder> orders = joyNOrderService.getNeededList(orderFilter);
					 JOYOrder cOrder = orders.get(0);
					 Reobj.put("orderId", cOrder.id);
		    	     Reobj.put("carId", cOrder.carVinNum);
		    	     Reobj.put("startTime", cOrder.startTime.getTime());
					 Reobj.put("ifBlueTeeth",cOrder.ifBlueTeeth);
		    	     Reobj.put("authCode", "ABCDEF");
					 Reobj.put("result", "10000");
				 }
			 } else {
				 Reobj.put("errMsg", "出现内部错误");
			 }
		 } catch(Exception e){
			 logger.error(e.toString());
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}

    //暂时留着，为以前保持兼容
	@RequestMapping(value={"newcar/rentCarTerminate"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject rentCarTerminate(HttpServletRequest req){
		logger.trace("rentCarTerminate method was invoked...");
		Map<String,Object> likeCondition = new HashMap<String, Object>();
		JSONObject Reobj=new JSONObject();
		Reobj.put("result", "10001");
		ReentrantLock optLock = null;
		try {
			//check if the car in state_wait_code or state_busy, and the owner equals the mobileNo
			Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			String vinNum = (String)jsonObj.get("carId");
			String mobileNo = (String)jsonObj.get("mobileNo");
			optLock = CarOpLock.getCarLock(vinNum);
			optLock.lock(); //先 获取锁,再取得状态>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			Car car = cacheCarService.getByVinNum(vinNum);
			if(mobileNo.equals(car.getOwner()) && (car.getState()==Car.state_wait_poweron
					|| car.getState()==Car.state_wait_sendcode|| car.getState()==Car.state_busy)  ) {

				     if(car.getState()==car.state_busy) {
						 //首先停止订单
						 JOYOrder orderFilter = new JOYOrder();
						 JOYOrder orderNewValue = new JOYOrder();
						 orderFilter.carVinNum = car.getVinNum();
						 orderFilter.delMark = JOYOrder.NON_DEL_MARK;
						 orderFilter.mobileNo = mobileNo;
						 orderNewValue.state = JOYOrder.state_wait_pay;
						 orderNewValue.stopLatitude = car.getLatitude();
						 orderNewValue.stopLatitude = car.getLongitude();
						 orderNewValue.stopTime = new Date(System.currentTimeMillis());
						 joyNOrderService.updateRecord(orderNewValue,orderFilter);
					 }

				car.setState(null);
				car.setOwner("");
				//这个过程的发起在car update status 来做
				cacheCarService.updateCarStateWaitClearCode(car);
				Reobj.put("result", "10000");
			} else {
				Reobj.put("errMsg","目前无订单");
			}
			//锁锁锁
			optLock.unlock(); //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		} catch(Exception e){
			if(optLock!=null && optLock.getHoldCount()>0) {
				optLock.unlock();
			}
			logger.error(e.getStackTrace().toString());
			Reobj.put("errMsg", e.toString());
		}
		return Reobj;
	}


	
	@RequestMapping(value={"newcar/rentCarTerminateReq"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject rentCarTerminateReq(HttpServletRequest req){
		 logger.trace("rentCarTerminateReq method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		ReentrantLock optLock = null;
		 try {
			//check if the car in state_wait_code or state_busy, and the owner equals the mobileNo
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 String vinNum = (String)jsonObj.get("carId");
			 String mobileNo = (String)jsonObj.get("mobileNo");
			 optLock = CarOpLock.getCarLock(vinNum);
			 optLock.lock(); //先 获取锁,再取得状态>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			 Car car = cacheCarService.getByVinNum(vinNum);
			 if(mobileNo.equals(car.getOwner()) && (car.getState()==Car.state_wait_poweron
					 || car.getState()==Car.state_wait_sendcode|| car.getState()==Car.state_busy)  ) {
                    /*
				     if(car.getState()==car.state_busy) {
						 //首先停止订单
						 joyNOrderService.updateOrderTermiate(car);
					 }
					 */
				     car.setState(null);
				    // car.setOwner("");
				     //这个过程的发起在car update status 来做
					 cacheCarService.updateCarStateWaitClearCode(car);
					 Reobj.put("result", "10000");
			 } else if(car.getState()==Car.state_wait_lock|| car.getState()==Car.state_wait_poweroff||car.getState()==Car.state_wait_clearcode){
				 //已经处于停止租用的流程了
				 Reobj.put("result", "10000");
			 }else {
				 	Reobj.put("errMsg","目前无订单");
			 }
			 //锁锁锁
			 optLock.unlock(); //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			
		 } catch(Exception e){
			 if(optLock!=null && optLock.getHoldCount()>0) {
				 optLock.unlock();
			 }
			 logger.error(e.getStackTrace().toString());
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}

	@RequestMapping(value={"newcar/rentCarTerminateAck"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject rentCarTerminateAck(HttpServletRequest req){
		logger.trace("rentCarTerminateAck method was invoked...");
		Map<String,Object> likeCondition = new HashMap<String, Object>();
		JSONObject Reobj=new JSONObject();
		Reobj.put("result", "10001");
		try {

			Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
            JOYOrder orderFilter = new JOYOrder();
			orderFilter.mobileNo = String.valueOf(jsonObj.get("mobileNo"));
			orderFilter.delMark = JOYOrder.NON_DEL_MARK;
			List<JOYOrder> orders = joyOrderService.getNeededList(orderFilter);
			if(orders.size()>0) {
				JOYOrder order = orders.get(0);
				if(order.state!=JOYOrder.state_busy) {
					Reobj.put("result","10000");
				}
			} else {
				Reobj.put("result","10000");
			}
		} catch(Exception e){
			logger.error(e.getStackTrace().toString());
			Reobj.put("errMsg", e.toString());
		}
		return Reobj;
	}


	@RequestMapping(value={"newcar/getCarLockState"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject getCarLockState(HttpServletRequest req){
		logger.error("getCarLockState method was invoked...");
		Map<String,Object> likeCondition = new HashMap<String, Object>();
		JSONObject Reobj=new JSONObject();
		Reobj.put("result", "10001");

		try {
			Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			String vinNum = jsonObj.get("carId").toString();
			JOYNCar ncarFilter = new JOYNCar();
			ncarFilter.vinNum = vinNum;
			List<JOYNCar> cars = joyNCarService.getNeededList(ncarFilter);
			JOYNCar car  = cars.get(0);
			Reobj.put("lockState",car.lockState);
            Reobj.put("result","10000");
		} catch(Exception e){
			Reobj.put("errMsg", e.toString());
			logger.error(e.toString());
		}

		return Reobj;
	}




	
	
	@RequestMapping(value="newcar/changeBatonMode", method=RequestMethod.POST)
	public  @ResponseBody JSONObject changeBatonMode(HttpServletRequest req){
		 logger.trace("new car changeBatonMode method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			   JOYOrder orderFilter = new JOYOrder();
			   orderFilter.mobileNo = ((String)jsonObj.get("mobileNo"));
			   orderFilter.carVinNum = (jsonObj.get("carId").toString());
	    	   orderFilter.delMark = (JOYReserveOrder.NODEL_FLAG);
			   JOYOrder orderNewValue = new JOYOrder();
	    	   orderNewValue.batonMode = (((Long)jsonObj.get("batonMode")).intValue());
	    	   joyNOrderService.updateRecord(orderNewValue,orderFilter);
			   Reobj.put("result", "10000");
		 } catch(Exception e){
			 logger.error(e.getStackTrace().toString());
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	@RequestMapping(value="newcar/updateDestination", method=RequestMethod.POST)
	public  @ResponseBody JSONObject updateDestination(HttpServletRequest req){
		 logger.trace("new car updateDestination method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 JOYOrder orderFilter = new JOYOrder();
			 orderFilter.mobileNo = ((String)jsonObj.get("mobileNo"));
			 orderFilter.carVinNum = (jsonObj.get("carId").toString());
	    	 orderFilter.delMark = (JOYReserveOrder.NODEL_FLAG);
			 JOYOrder orderNewValue = new JOYOrder();
	    	 
	    	 String destination = jsonObj.get("destination").toString();
			 orderNewValue.destination = destination;
	    	 joyNOrderService.updateRecord(orderNewValue,orderFilter);
			 Reobj.put("result", "10000");
		 } catch(Exception e){
			 logger.error(e.getStackTrace().toString());
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
}
