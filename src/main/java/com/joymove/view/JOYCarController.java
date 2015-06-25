package com.joymove.view;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import com.futuremove.cacheServer.concurrent.CarOpLock;
import com.joymove.util.WeChatPay.WeChatPayUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Resource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.joymove.service.*;
import com.joymove.util.zhifubao.ZhifubaoUtils;
import com.joymove.entity.*;


@Controller("JOYCarController")
public class JOYCarController {
	
	final static Logger logger = LoggerFactory.getLogger(JOYCarController.class);

	@Resource(name = "JOYCarService")
	private  JOYCarService joyCarService;
	@Resource(name = "JOYCouponService")
	private JOYCouponService joyCouponService;
	@Resource(name = "carService")
	private CarService cacheCarService;
	@Resource(name = "JOYOrderService")
	private JOYOrderService joyOrderService;
	@Resource(name = "JOYNOrderService")
	private JOYNOrderService joynOrderService;
	@Resource(name = "JOYReserveOrderService")
	private JOYReserveOrderService joyReserveOrderService;
	@Resource(name = "JOYInterPOIService")
	private JOYInterPOIService joyInterPOIService;
	@Resource(name = "JOYUserService")
	private JOYUserService joyUserService;
	@Resource(name = "JOYPayReqInfoService")
	private JOYPayReqInfoService  joyPayReqInfoService;




	/*********   business proc  ******************/

	@RequestMapping(value={"rent/getNearByAvailableCars","rent/getNearByBusyCars"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject getNearByAvailableCars(HttpServletRequest req){
		 logger.trace("getNearByAvailableCars method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 JSONArray  carArray  = new JSONArray();
		//       sdfdsfdsf
		 Reobj.put("result", "10001");
		 Reobj.put("cars", carArray);
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");;
			 likeCondition.put("userPositionX", jsonObj.get("userLongitude")==null ? 0.0: jsonObj.get("userLongitude") );
			 likeCondition.put("userPositionY", jsonObj.get("userLatitude")==null ? 0.0: jsonObj.get("userLatitude") );
			 likeCondition.put("scope", jsonObj.get("scope")==null ? 10 : jsonObj.get("scope") );
			 String URI = req.getRequestURI();
			 if(URI.contains("getNearByAvailableCars")) {
				 likeCondition.put("state", JOYCar.STATE_FREE);
			 } else {
				 likeCondition.put("state", JOYCar.STATE_BUSY);
			 }
			 List<JOYCar> cars = joyCarService.getCarByScope(likeCondition);

			 Iterator iter = cars.iterator();
			 while(iter.hasNext()){
				 JOYCar car_item  = (JOYCar)iter.next();
				 JSONObject car_json = new JSONObject();
				 car_json.put("carId", String.valueOf(car_item.id));
				 car_json.put("longitude",  car_item.positionX);
				 car_json.put("latitude",  car_item.positionY);
				 car_json.put("desp",  car_item.desp);
				 car_json.put("ifBlueTeeth",car_item.ifBlueTeeth);
				 car_json.put("carInfo",car_item.carInfo);
				 car_json.put("imageUrl",car_item.imageUrl);
				 car_json.put("powerType",car_item.powerType);
				 car_json.put("powerPercent",car_item.powerPercent);
				 if(URI.contains("getNearByAvailableCars")) {

				 } else {
					 long timeScope = jsonObj.get("timeScope")==null? 60000:Long.parseLong(jsonObj.get("timeScope").toString());
					 car_json.put("eta",  (int)(Math.random()*timeScope) + System.currentTimeMillis());

				 }
				 carArray.add(car_json);
			 }
			 Reobj.put("result", "10000");

		 } catch(Exception e) {
			 Reobj.put("result", "10001");
			 logger.trace(e.getStackTrace().toString());
		 }
		 return Reobj;
	}

	@RequestMapping(value={"rent/getCarDetailInfo"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject getCarDetailInfo(HttpServletRequest req){
		logger.trace("getCarDetailInfo method was invoked...");
		Map<String,Object> likeCondition = new HashMap<String, Object>();
		JSONObject Reobj=new JSONObject();
		//       sdfdsfdsf
		Reobj.put("result", "10001");

		try {
			Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");;
            JOYCar carFilter = new JOYCar();
			Integer carId = Integer.parseInt(String.valueOf(jsonObj.get("carId")));
			carFilter.id = carId;
			JOYCar car = joyCarService.getNeededRecord(carFilter);
			if(car==null) {
                 Reobj.put("result","10002");
				 Reobj.put("errMsg","车辆ID不正确");
			} else {
				Reobj.put("longitude",  car.positionX);
				Reobj.put("latitude",  car.positionY);
				Reobj.put("desp",  car.desp);
				Reobj.put("ifBlueTeeth",car.ifBlueTeeth);
				Reobj.put("carInfo",car.carInfo);
				Reobj.put("imageUrl",car.imageUrl);
				Reobj.put("powerType",car.powerType);
				Reobj.put("powerPercent",car.powerPercent);
			}
		} catch(Exception e) {
			Reobj.put("result", "10001");
			logger.trace("exception: ",e);
		}
		return Reobj;
	}

	
	

	
	@RequestMapping(value={"rent/rentCarReq","rent/pingCar"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject rentCarReq(HttpServletRequest req){
		 logger.trace("rentCarReq method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 JSONArray  carArray  = new JSONArray();
		//       sdfdsfdsf
		 try{
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 String vinNum = jsonObj.get("carId").toString();
			 String mobileNo = jsonObj.get("mobileNo").toString();
			 JOYUser user = new JOYUser();
			 user.mobileNo = mobileNo; // setMobileNo(mobileNo);
			 String authStateErrMsg = joyUserService.checkUserState(user);
			 if(authStateErrMsg!=null) {
				 Reobj.put("errMsg", authStateErrMsg);
				 return Reobj;
			 }
			 
			 
			 logger.trace("servlet path: " +req.getSession().getServletContext().getRealPath("/"));
			 //  logger.trace("servlet path: " +req.getSession().getServletContext().);
		 } catch(Exception e){
			 
		 }
		 Reobj.put("result", "10000");
		 return Reobj;
	}
	
	@RequestMapping(value="rent/rentCarAck", method=RequestMethod.POST)
	public  @ResponseBody JSONObject rentCarAck(HttpServletRequest req){
		 logger.trace("getNearByAvailableCars method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 ReentrantLock optLock = null;
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 //first check if this car reserved by me , if not ,return 
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 JOYOrder orderFilter = new JOYOrder();

			 Integer carId = Integer.parseInt(jsonObj.get("carId").toString());
			 orderFilter.carId = carId;
			 orderFilter.delMark = JOYOrder.NON_DEL_MARK;
			 orderFilter.mobileNo = String.valueOf(jsonObj.get("mobileNo"));
			 List<JOYOrder> orders = joyOrderService.getNeededList(orderFilter);
			 JOYCar carFilter = new JOYCar();
			 carFilter.id  = carId;
		      //check the state of this car, it muse be in car_free
			 optLock = CarOpLock.getCarLock(String.valueOf(carId));
				 if(optLock.tryLock()) {

					  List<JOYCar> cars = joyCarService.getNeededList(carFilter);
					  JOYCar cCar = cars.get(0);
					   if(orders.size()>0) {
						 //already have a order
						   JOYOrder cOrder = orders.get(0);
						   Reobj.put("orderId",cOrder.id);
						   Reobj.put("state",cOrder.state);
						   Reobj.put("errMsg", "您已经租用车辆");

					   } else if(cCar.state==JOYCar.STATE_FREE ) {
								  JOYCar carNewValue = new JOYCar();
								  carNewValue.mobileNo = String.valueOf(jsonObj.get("mobileNo"));
								  carNewValue.state = JOYCar.STATE_BUSY;
								  joyCarService.updateRecord(carNewValue, cCar);
								  JOYOrder order = new JOYOrder();
								  order.mobileNo = String.valueOf(jsonObj.get("mobileNo"));
								  order.carId = (carId);
								  order.ifBlueTeeth = cCar.ifBlueTeeth;
								  order.startLongitude = cCar.positionX.doubleValue();
								  order.startLatitude = cCar.positionY.doubleValue();
								  order.startTime = new Date(System.currentTimeMillis());
								  joyOrderService.insertRecord(order);
								  order.delMark = JOYOrder.NON_DEL_MARK;
								  orders = joyOrderService.getNeededList(order);
								  order = orders.get(0);
								  Reobj.put("orderId", order.id);
								  Reobj.put("carId", String.valueOf(order.carId));
								  Reobj.put("ifBlueTeeth", JOYCar.NON_BT);
								  Reobj.put("startTime", order.startTime.getTime());
								  Reobj.put("authCode", "abcdef");
								  Reobj.put("result", "10000");
					  } else {
						  Reobj.put("errMsg", "车辆不在空闲状态");
					  }
					 //unlock
					 optLock.unlock();
				 } else {
                       Reobj.put("errMsg","车辆被其他用户抢先租用.");
				}
		 } catch(Exception e){
			 if(optLock!=null && optLock.getHoldCount()>0) {
				 optLock.unlock();
			 }
			 logger.error(e.getStackTrace().toString());
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	@RequestMapping(value="rent/terminateMyOrder", method=RequestMethod.POST)
	public  @ResponseBody JSONObject terminateMyOrder(HttpServletRequest req){
		 logger.trace("getNearByAvailableCars method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 try{
			 
			 //change the order, owner by the 'mobileNo' and the 'carId' to be state 'wait for pay'
			 //then ok.
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 JOYOrder orderFilter = new  JOYOrder();
			 orderFilter.mobileNo = String.valueOf(jsonObj.get("mobileNo"));
			 orderFilter.state = JOYOrder.state_busy;
			 List<JOYOrder> orders = joyOrderService.getNeededList(orderFilter);
			JOYOrder order = orders.get(0);
			order.delMark = (JOYOrder.NON_DEL_MARK);
			order.state = (JOYOrder.state_wait_pay);
			order.stopTime = (new Date(System.currentTimeMillis()));
			order.stopLatitude = order.startLatitude;
			order.stopLongitude = order.startLongitude;
			joyOrderService.updateRecord(order,orderFilter);
			 //update car's state
			 JOYCar car = new JOYCar();
			 car.id = (order.carId);
			 car.state = (JOYCar.STATE_FREE);
			 joyCarService.setCarFree(car);
			Reobj.put("result","10000");
		 } catch(Exception e){
			 logger.error("exception:",e);
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	@RequestMapping(value={"rent/checkOrderStatus","rent/getOrderDetail"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject checkOrderStatus(HttpServletRequest req){
		 logger.trace("checkOrderStatus method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 Reobj.put("state", 0);
		 Car cacheCar = null;
		 logger.info("i am just the log4j tester");
		 
		//       sdfdsfdsf
		 try{
			 //first check if this car reserved by me , if not ,return 
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 JOYOrder orderFilter = new JOYOrder();
			 orderFilter.mobileNo = String.valueOf(jsonObj.get("mobileNo"));
			 orderFilter.delMark = JOYOrder.NON_DEL_MARK;
			 List<JOYOrder> orders = joyOrderService.getNeededList(orderFilter);
			 
			 cacheCar = new Car();
			 cacheCar.setOwner((String)jsonObj.get("mobileNo"));
			 cacheCar.setState(Car.state_free);
			 cacheCar =  cacheCarService.getByOwnerAndNotState(cacheCar);
			 
			 
		       if(orders.size()>0) {
		    	   JOYOrder cOrder = orders.get(0);
		    	 //already have a order
		    	  
		    	   Reobj.put("result", "10000");
		    	   Reobj.put("orderId", cOrder.id);
		    	   if(cOrder.carId!=null) {
		    		   // it is a old order
		    		   Reobj.put("carId", String.valueOf(cOrder.carId));
		    	   } else {
		    		   // it is a new order
		    		   Reobj.put("carId",cOrder.carVinNum);
		    	   }
		    	   Reobj.put("destination", cOrder.destination);
		    	   Reobj.put("startTime", cOrder.startTime.getTime());
		    	   Reobj.put("batonMode", cOrder.batonMode);
		    	   Reobj.put("state",cOrder.state);
		    	   Reobj.put("destination",cOrder.destination);
				   Reobj.put("authCode", "ABCDEF");
				   Reobj.put("ifBlueTeeth", cOrder.ifBlueTeeth);
		    	   if(cOrder.state == JOYOrder.state_wait_pay) {
		    		   Reobj.put("stopTime", cOrder.stopTime.getTime());
		    	   }
		    	   Reobj.put("fee", cOrder.getTotalFee());
		    	   Reobj.put("mile", cOrder.getTotalFee() *3.1415);
		      }else if (cacheCar!=null){
		    	  //if there is a ncar in wait_code state, and the owner is me
		    	  Reobj.put("state", -2);
		    	  Reobj.put("result","10000");
		    	  Reobj.put("carId", cacheCar.getVinNum());
		      } else {
		    	  Reobj.put("result", "10000");
		    	  Reobj.put("state", 0);
		    	  Reobj.put("errMsg", "您目前无订单");
		      }
		 } catch(Exception e){
			 logger.error(e.getStackTrace().toString());
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	
	
	
	@RequestMapping(value="rent/payOrderReq", method=RequestMethod.POST)
	public  @ResponseBody JSONObject payOrderReq(HttpServletRequest req){
			 logger.trace("payOrderReq method was invoked...");
			 Map<String,Object> likeCondition = new HashMap<String, Object>();
			 JSONObject Reobj=new JSONObject();
			 Reobj.put("result", "10001");
			 Long [] temp = null;
			 JOYOrder cOrder = (JOYOrder)req.getAttribute("cOrder");
			 
			 
			 if(cOrder.state==JOYOrder.state_pay_over) {
				 Reobj.put("result", "10010");//the order already pay over
				 return Reobj;
			 }
			 
			 try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");

				 
				 String mobileNo = (String)jsonObj.get("mobileNo");

				 double zhifubaoFee = Double.valueOf(String.valueOf(jsonObj.get("zhifubao")));
				 /*******************          *******           ****************/
				 JOYOrder orderFilter = new JOYOrder();
				 //first get the orders
				 orderFilter.mobileNo = mobileNo;
				 orderFilter.delMark = JOYOrder.NON_DEL_MARK;
				 orderFilter.state = JOYOrder.state_wait_pay;
				 List<JOYOrder> orders =  joyOrderService.getNeededList(orderFilter);
				 JOYOrder order = orders.get(0);
				 /*******************          *******           ****************/
				 //then get the coupon from sql
				 JSONArray couponIds = (JSONArray)jsonObj.get("coupons");
				 Long [] cIds = new Long[couponIds.size()];
				 int index = 0;
				 for(Object id:couponIds){
					 cIds[index++] = Long.valueOf(String.valueOf(id));
				 }
				 
				 List<JOYCoupon> coupons = joyCouponService.getCouponById(cIds);
				 /*******************          *******           ****************/
				 //check the coupon's num
				 JSONArray usedIds = new JSONArray();
				 List<Long> usedLongIds = new ArrayList<Long>();
				 double  orderFee = (double)(Math.floor(order.getTotalFee() * 100)/100.0);
				 double  couponFee = 0.0;
				 for(JOYCoupon coupon:coupons){
					if(coupon.delMark==JOYCoupon.NON_DELMARK) {
						 couponFee += coupon.couponNum.doubleValue();
						 usedIds.add(coupon.id.longValue());
						 usedLongIds.add(coupon.id.longValue());
						 if(couponFee >= orderFee){
							 break;
						 }
					 }
				 }
				 /*******************          *******           ****************/
				 //how many show payed by zhifubao
				 if(couponFee >= orderFee){
					 //only coupon is ok
					 
					 Reobj.put("coupons", usedIds);
					 Reobj.put("zhifubao", "0.0");
					 //change the order state
					// joyCouponService.deleteCouponById((Long [])usedIds.toArray());
					 temp = new Long[usedLongIds.size()];
					 JOYOrder orderNewValue =new JOYOrder();
					 orderNewValue.state = JOYOrder.state_pay_over;
					 orderNewValue.delMark = JOYOrder.DEL_MARK;
					 temp = new Long[usedLongIds.size()];
                     joyCouponService.deleteCouponById(usedLongIds.toArray(temp),order.id);
					 joyOrderService.updateRecord(orderNewValue,orderFilter); //deleteOrder(usedLongIds.toArray(temp),order);
					 Reobj.put("result", "10000");
					 
				 } else if (couponFee + zhifubaoFee >= orderFee) {
					 //coupon and zhifubao is ok 
					
					 Reobj.put("coupons", usedIds);
					 Reobj.put("zhifubao", String.valueOf(zhifubaoFee));
					 /***** zhifubao's code ******/
					 String currTime = System.currentTimeMillis() + "";
					 String zhifubao_code = ZhifubaoUtils.getPayInfo("rentPay", mobileNo, zhifubaoFee, currTime + String.valueOf(order.id));
					 temp = new Long[usedLongIds.size()];
					 joyCouponService.deleteCouponById(usedLongIds.toArray(temp),order.id);
					 /***** wx's code ******/
					 JOYPayReqInfo wxpayInfo = new JOYPayReqInfo();
					 JOYPayReqInfo zhifubaoInfo = new JOYPayReqInfo();
					 zhifubaoInfo.mobileNo = mobileNo;
					 wxpayInfo.mobileNo = (mobileNo);
					 zhifubaoInfo.type = JOYPayReqInfo.type_zhifubao;
					 wxpayInfo.type = JOYPayReqInfo.type_wx;

					 String wx_trade_no = "rentPay" + String.valueOf(System.currentTimeMillis());
					 zhifubaoInfo.out_trade_no = currTime + String.valueOf(order.id);
					 wxpayInfo.out_trade_no = (wx_trade_no);
					 wxpayInfo.totalFee = (Double.valueOf(zhifubaoFee));
					 zhifubaoInfo.totalFee = wxpayInfo.totalFee;
					 String wx_code = WeChatPayUtil.genePayStr(String.valueOf(Double.valueOf(zhifubaoFee * 100).longValue()), wx_trade_no);
					 joyPayReqInfoService.insertRecord(zhifubaoInfo);
					 joyPayReqInfoService.insertRecord(wxpayInfo);  //insertWXPayInfo(wxpayInfo);
					 /** generate result **/
					 Reobj.put("zhifubao_code", zhifubao_code);
					 Reobj.put("wx_code",new JSONParser().parse(wx_code));
					 Reobj.put("result", "10000");
				 } else {
					 //coupon and zhifubao is not ok
					 Reobj.put("errMsg", "支付的费用不够完成本次租车");
				 }

			 } catch(Exception e){
				 Reobj.put("errMsg", e.toString());
				 e.printStackTrace();
			 }
			 return Reobj;
	}
	
	@RequestMapping(value="rent/changeBatonMode", method=RequestMethod.POST)
	public  @ResponseBody JSONObject changeBatonMode(HttpServletRequest req){
		 logger.trace("changeBatonMode method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			   JOYOrder orderFilter = new JOYOrder();
			 JOYOrder orderNew = new JOYOrder();
			   orderFilter.mobileNo = ((String)jsonObj.get("mobileNo"));
			   orderFilter.carId = (((Long)jsonObj.get("carId")).intValue());
	    	   orderFilter.delMark  = (JOYReserveOrder.NODEL_FLAG);
	    	   orderNew.batonMode = (((Long)jsonObj.get("batonMode")).intValue());
	    	   joyOrderService.updateRecord(orderNew,orderFilter);
			   Reobj.put("result", "10000");
		 } catch(Exception e){
			 logger.error(e.getStackTrace().toString());
			 Reobj.put("errMsg",e.toString());
		 }
		 return Reobj;
	}
	
	@RequestMapping(value="rent/updateDestination", method=RequestMethod.POST)
	public  @ResponseBody JSONObject updateDestination(HttpServletRequest req){
		 logger.trace("updateDestination method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 JOYOrder orderFilter = new JOYOrder();
			 JOYOrder orderNew = new JOYOrder();
			 orderFilter.mobileNo = ((String)jsonObj.get("mobileNo"));
			 orderFilter.carId = (Integer.parseInt(jsonObj.get("carId").toString()));
	    	 orderFilter.delMark = (JOYReserveOrder.NODEL_FLAG);
	    	 
	    	 String destination = jsonObj.get("destination").toString();
	    	 orderNew.destination = (destination);
	    	 joyOrderService.updateRecord(orderNew,orderFilter);
			 Reobj.put("result", "10000");
		 } catch(Exception e){
			 logger.error(e.getStackTrace().toString());
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	/***    get interesting points ********/
	@RequestMapping(value="rent/getInterestPOI", method=RequestMethod.POST)
	public  @ResponseBody JSONObject getInterestPOI(HttpServletRequest req){
		 logger.trace("getInterestPOI method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 try{
			 JOYInterPOI poiFilter = new JOYInterPOI();
			 List<JOYInterPOI> pois = joyInterPOIService.getNeededList(poiFilter);
			 //process pois
			 for(JOYInterPOI poi:pois){
				 JSONArray poiGroup = null;
				 if(likeCondition.get(poi.title)!=null){
					 poiGroup = (JSONArray)likeCondition.get(poi.title);
				 } else {
					 poiGroup = new JSONArray();
					 likeCondition.put(poi.title,poiGroup);
				 }
				 JSONObject poiJSONObj = new JSONObject();
				 poiJSONObj.put("name", poi.name);
				 poiJSONObj.put("latitude", poi.latitude);
				 poiJSONObj.put("longitude", poi.longitude);
				 poiJSONObj.put("url", "http://www.baidu.com");
				 poiGroup.add(poiJSONObj);
			 }
			 
			 Iterator iter = likeCondition.entrySet().iterator(); 
			 JSONArray ReObj_POIs = new JSONArray();
			 while (iter.hasNext()) { 
			     Map.Entry entry = (Map.Entry) iter.next(); 
			     JSONObject ReObj_POIs_item = new JSONObject();
			     ReObj_POIs_item.put("name", (String)entry.getKey());
			     ReObj_POIs_item.put("POI", (JSONArray)entry.getValue());
			   //  ReObj_POIs_item.put("POI", (JSONArray)entry.getValue());
			     ReObj_POIs.add(ReObj_POIs_item);  
			 } 
			 Reobj.put("result", "10000");
			 Reobj.put("route", ReObj_POIs);

		 } catch(Exception e){
			 logger.error(e.getStackTrace().toString());
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}

}
