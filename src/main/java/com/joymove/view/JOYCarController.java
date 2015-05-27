package com.joymove.view;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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

@Scope("prototype")
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
	@Resource(name = "JOYReserveOrderService")
	private JOYReserveOrderService joyReserveOrderService;
	@Resource(name = "JOYInterPOIService")
	private JOYInterPOIService joyInterPOIService;
	@Resource(name = "JOYUserService")
	private JOYUserService joyUserService;
	@Resource(name = "JOYWXPayInfoService")
	private JOYWXPayInfoService joywxPayInfoService;



	
	public JOYCarController(JOYCarService joyCarService,
			JOYCouponService joyCouponService, JOYOrderService joyOrderService,
			JOYReserveOrderService joyReserveOrderService) {
		super();
		this.joyCarService = joyCarService;
		this.joyCouponService = joyCouponService;
		this.joyOrderService = joyOrderService;
		this.joyReserveOrderService = joyReserveOrderService;
	}

	public JOYCarController() {
		super();
		// TODO Auto-generated constructor stub
	}

	/*********   business proc  ******************/

	@RequestMapping(value={"rent/getNearByAvailableCars","rent/getNearByBusyCars"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject getNearByAvailableCars(HttpServletRequest req){
		 System.out.println("getNearByAvailableCars method was invoked...");
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
				 car_json.put("carId", car_item.id);
				 car_json.put("longitude",  car_item.positionX);
				 car_json.put("latitude",  car_item.positionY);
				 car_json.put("desp",  car_item.desp);
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
			 System.out.println(e);
		 }
		 return Reobj;
	}
	
	@RequestMapping(value="rent/getMyReservedCar", method=RequestMethod.POST)
	public  @ResponseBody JSONObject getMyReservedCar(HttpServletRequest req){
		 System.out.println("getNearByAvailableCars method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		 try {  
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 likeCondition.put("mobileNo", jsonObj.get("mobileNo"));
			 likeCondition.put("delFlag", JOYReserveOrder.NODEL_FLAG);
			 likeCondition.put("expSeconds", JOYReserveOrder.EXPIRE_SECONDS);
			 List<JOYReserveOrder> reOrders = joyReserveOrderService.getNeededReserveOrder(likeCondition);
			 Reobj.put("result","10002" );
			 Reobj.put("errMsg","No data");
			 if(reOrders.size()==1) {
				 JOYReserveOrder cOrder = reOrders.get(0);
				 if(cOrder.carId==null) {
					 //it is a new style reserve order
					 String vinNum = cOrder.carVinNum;
					 Car cacheCar = cacheCarService.getByVinNum(vinNum);
					 if(cacheCar.getState()==Car.state_reserved) {
						 Reobj.put("result", "10000");
						 JSONObject carJson = new JSONObject();
						 Reobj.put("cars", carJson);
						 carJson.put("carId", vinNum);
						 carJson.put("longitude", cacheCar.getLongitude());
						 carJson.put("latitude", cacheCar.getLatitude());
						 carJson.put("desp","dsfdsf");
						 carJson.put("startTime", cOrder.startTime);
					 }
				 } else {
					 // it is a old style reserve order
					 likeCondition.put("id", cOrder.carId);
					 List<JOYCar> cars = joyCarService.getCarById(likeCondition);
					 if(cars.size() ==1) {
						 JOYCar car = cars.get(0);
						 Reobj.put("result", "10000");
						 JSONObject carJson = new JSONObject();
						 Reobj.put("cars", carJson);
						 
						 carJson.put("carId", cOrder.carId);
						 carJson.put("longitude", car.positionX);
						 carJson.put("latitude", car.positionY);
						 carJson.put("desp",car.desp);
						 carJson.put("startTime", cOrder.startTime);
					 } 
				 }
			 } 
		 } catch(Exception e) {
			 Reobj.put("result", "10001");
			 System.out.println(e);
		 }
		 return Reobj;
	}
	
	

	
	@RequestMapping(value={"rent/rentCarReq","rent/pingCar"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject rentCarReq(HttpServletRequest req){
		 System.out.println("rentCarReq method was invoked...");
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
			 
			 
			 System.out.println("servlet path: " +req.getSession().getServletContext().getRealPath("/"));
			 //  System.out.println("servlet path: " +req.getSession().getServletContext().);
		 } catch(Exception e){
			 
		 }
		 Reobj.put("result", "10000");
		 return Reobj;
	}
	
	@RequestMapping(value="rent/rentCarAck", method=RequestMethod.POST)
	public  @ResponseBody JSONObject rentCarAck(HttpServletRequest req){
		 System.out.println("getNearByAvailableCars method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 //first check if this car reserved by me , if not ,return 
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 Integer carId = Integer.parseInt(jsonObj.get("carId").toString()); 
			 likeCondition.put("carId", carId);
			 likeCondition.put("delFlag", JOYReserveOrder.NODEL_FLAG);
			 likeCondition.put("delMark", JOYReserveOrder.NODEL_FLAG);
			 likeCondition.put("expSeconds", JOYReserveOrder.EXPIRE_SECONDS);
			 
			 List<JOYReserveOrder> reOrders = joyReserveOrderService.getNeededReserveOrder(likeCondition);
			 likeCondition.remove("carId");
			 likeCondition.put("mobileNo", jsonObj.get("mobileNo"));
			 List<JOYOrder> orders = joyOrderService.getNeededOrder(likeCondition);
			 
			 JOYReserveOrder cReOrder = reOrders.size()>0?reOrders.get(0):null;
			 likeCondition.put("id", carId);
		      //check the state of this car, it muse be in car_free
		      List<JOYCar> cars = joyCarService.getCarById(likeCondition);
		      JOYCar cCar = cars.get(0);
		       if(orders.size()>0) {
		    	 //already have a order
		    	   JOYOrder cOrder = orders.get(0);
		    	   Reobj.put("orderId",cOrder.id);
		    	   Reobj.put("state",cOrder.state);
		    	   Reobj.put("errMsg", "already ordered + "+cOrder.id);
		    	   
		       } else if(cCar.state==JOYCar.STATE_FREE || (cCar.state==JOYCar.STATE_RESERVE &&
		    		  cReOrder.mobileNo.equals((String)jsonObj.get("mobileNo")))) {
		    	      JOYOrder order = new JOYOrder();
		    	      order.mobileNo = ((String)jsonObj.get("mobileNo"));
		    	      order.carId = (carId);
		    	      joyOrderService.insertOrder(order);
		    	      orders = joyOrderService.getNeededOrder(likeCondition);
		    	      order = orders.get(0);
		    	      Reobj.put("result", "10000");
		    	      Reobj.put("orderId", order.id);
		    	      Reobj.put("carId", order.carId);
		    	      Reobj.put("startTime", order.startTime.getTime());
		    	      Reobj.put("authCode", "123456");
		    	      
		      } else {
		    	  Reobj.put("errMsg", "car not in free state");
		      }
		 } catch(Exception e){
			
			
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	@RequestMapping(value="rent/terminateMyOrder", method=RequestMethod.POST)
	public  @ResponseBody JSONObject terminateMyOrder(HttpServletRequest req){
		 System.out.println("getNearByAvailableCars method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 
			 //change the order, owner by the 'mobileNo' and the 'carId' to be state 'wait for pay'
			 //then ok.
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 likeCondition.put("mobileNo", jsonObj.get("mobileNo"));
			 likeCondition.put("state", JOYOrder.state_busy);
			 List<JOYOrder> orders = joyOrderService.getNeededOrder(likeCondition);
			JOYOrder order = orders.get(0);
			order.delMark = (JOYOrder.NON_DEL_MARK);
			order.state = (JOYOrder.state_wait_pay);
			order.stopTime = (new Date(System.currentTimeMillis()));
			joyOrderService.updateOrderStop(order);
			Reobj.put("result","10000");
		 } catch(Exception e){
			 e.printStackTrace();
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	@RequestMapping(value={"rent/checkOrderStatus","rent/getOrderDetail"}, method=RequestMethod.POST)
	public  @ResponseBody JSONObject checkOrderStatus(HttpServletRequest req){
		 System.out.println("checkOrderStatus method was invoked...");
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
			
			 likeCondition.put("mobileNo", jsonObj.get("mobileNo"));
			 likeCondition.put("delMark", JOYReserveOrder.NODEL_FLAG);
			 List<JOYOrder> orders = joyOrderService.getNeededOrder(likeCondition);
			 
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
		    		   Reobj.put("carId", cOrder.carId);
		    	   } else {
		    		   // it is a new order
		    		   Reobj.put("carId",cOrder.carVinNum);
		    	   }
		    	   Reobj.put("destination", cOrder.destination);
		    	   Reobj.put("startTime", cOrder.startTime.getTime());
		    	   Reobj.put("batonMode", cOrder.batonMode);
		    	   Reobj.put("state",cOrder.state);
		    	   Reobj.put("destination",cOrder.destination);
				   Reobj.put("authCode", "123456");
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
		    	  Reobj.put("errMsg", "not ordered");
		      }
		 } catch(Exception e){
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	
	
	
	    //payReq 
		@RequestMapping(value="rent/payOrderReq", method=RequestMethod.POST)
		public  @ResponseBody JSONObject payOrderReq(HttpServletRequest req){
			 System.out.println("payOrderReq method was invoked...");
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

				 double zhifubaoFee = Double.valueOf((String)jsonObj.get("zhifubao"));
				 /*******************          *******           ****************/
				 //first get the orders
				 likeCondition.put("mobileNo",mobileNo);
				 likeCondition.put("delMark",JOYOrder.NON_DEL_MARK);
				 likeCondition.put("state",JOYOrder.state_wait_pay);
				 List<JOYOrder> orders =  joyOrderService.getNeededOrder(likeCondition);
				 JOYOrder order = orders.get(0);
				 /*******************          *******           ****************/
				 //then get the coupon from sql
				 JSONArray couponIds = (JSONArray)jsonObj.get("coupons");
				 Long [] cIds = new Long[couponIds.size()];
				 int index = 0;
				 for(Object id:couponIds){
					 cIds[index++] = Long.valueOf((String)id);
				 }
				 
				 List<JOYCoupon> coupons = joyCouponService.getCouponById(cIds);
				 /*******************          *******           ****************/
				 //check the coupon's num
				 JSONArray usedIds = new JSONArray();
				 List<Long> usedLongIds = new ArrayList<Long>();
				 double  orderFee = (double)(Math.round(order.getTotalFee()*100)/100.0);
				 double  couponFee = 0.0;
				 for(JOYCoupon coupon:coupons){
					if(coupon.getDelMark()==JOYCoupon.NON_DELMARK) {
						 couponFee += coupon.couponNum.doubleValue();
						 usedIds.add(coupon.couponId.longValue());
						 usedLongIds.add(coupon.couponId.longValue());
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
					 order.state = (JOYOrder.state_pay_over);
					 order.delMark = (JOYOrder.DEL_MARK);
					 joyOrderService.deleteOrder(usedLongIds.toArray(temp),order);
					 Reobj.put("result", "10000");
					 
				 } else if (couponFee + zhifubaoFee >= orderFee) {
					 //coupon and zhifubao is ok 
					
					 Reobj.put("coupons", usedIds);
					 Reobj.put("zhifubao", String.valueOf(zhifubaoFee));
					 /***** zhifubao's code ******/
					 String currTime = System.currentTimeMillis() + "";
					 String zhifubao_code = ZhifubaoUtils.getPayInfo("rentPay", mobileNo, zhifubaoFee, currTime + String.valueOf(order.id));
					 temp = new Long[usedLongIds.size()];
					 joyCouponService.deleteCouponById(usedLongIds.toArray(temp));
					 /***** wx's code ******/
					 JOYWXPayInfo wxpayInfo = new JOYWXPayInfo();
					 wxpayInfo.mobileNo = (mobileNo);
					 String wx_trade_no = "rentPay" + mobileNo + String.valueOf(System.currentTimeMillis()).substring(8,12);
					 wxpayInfo.out_trade_no = (wx_trade_no);
					 wxpayInfo.totalFee = (Double.valueOf(zhifubaoFee));
					 String wx_code = WeChatPayUtil.genePayStr(String.valueOf(Double.valueOf(zhifubaoFee * 100).longValue()), wx_trade_no);
					 joywxPayInfoService.insertWXPayInfo(wxpayInfo);
					 /** generate result **/
					 Reobj.put("zhifubao_code", zhifubao_code);
					 Reobj.put("wx_code",new JSONParser().parse(wx_code));
					 Reobj.put("result", "10000");
				 } else {
					 //coupon and zhifubao is not ok
					 Reobj.put("errMsg", "fee not enough");
				 }

			 } catch(Exception e){
				 Reobj.put("errMsg", e.toString());
				 e.printStackTrace();
			 }
			 return Reobj;
	}
	
	@RequestMapping(value="rent/changeBatonMode", method=RequestMethod.POST)
	public  @ResponseBody JSONObject changeBatonMode(HttpServletRequest req){
		 System.out.println("changeBatonMode method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			   JOYOrder cOrder = new JOYOrder();
			   cOrder.mobileNo = ((String)jsonObj.get("mobileNo"));
			   cOrder.carId = (((Long)jsonObj.get("carId")).intValue());
	    	   cOrder.delMark  = (JOYReserveOrder.NODEL_FLAG);
	    	   cOrder.batonMode = (((Long)jsonObj.get("batonMode")).intValue());
	    	   joyOrderService.changeBatonMode(cOrder);			 
			   Reobj.put("result", "10000");
		 } catch(Exception e){
			
			
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	@RequestMapping(value="rent/updateDestination", method=RequestMethod.POST)
	public  @ResponseBody JSONObject updateDestination(HttpServletRequest req){
		 System.out.println("updateDestination method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 
		//       sdfdsfdsf
		 try{
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 JOYOrder cOrder = new JOYOrder();
			 cOrder.mobileNo = ((String)jsonObj.get("mobileNo"));
			 cOrder.carId = (Integer.parseInt(jsonObj.get("carId").toString()));
	    	 cOrder.delMark = (JOYReserveOrder.NODEL_FLAG);
	    	 
	    	 String destination = jsonObj.get("destination").toString();
	    	 cOrder.destination = (destination);
	    	 joyOrderService.updateDestination(cOrder);	 
			 Reobj.put("result", "10000");
		 } catch(Exception e){
			
			
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	
	/***    get interesting points ********/
	@RequestMapping(value="rent/getInterestPOI", method=RequestMethod.POST)
	public  @ResponseBody JSONObject getInterestPOI(HttpServletRequest req){
		 System.out.println("getInterestPOI method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 Reobj.put("result", "10001");
		 try{
			 List<JOYInterPOI> pois = joyInterPOIService.getAllPOI();
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
			 Reobj.put("errMsg", e.toString());
		 }
		 return Reobj;
	}
	

	
	
	
	
	
	
	
	

	
	
    
}
