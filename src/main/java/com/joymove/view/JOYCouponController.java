package com.joymove.view;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joymove.entity.JOYCoupon;
import com.joymove.service.JOYCouponService;



@Controller("JOYCouponController")
public class JOYCouponController {


	@Resource(name = "JOYCouponService")
	private JOYCouponService joycouponService;
	
	
	@RequestMapping(value="usermgr/viewCoupon1",method=RequestMethod.POST)
	public @ResponseBody JSONObject veiwConpon1(HttpServletRequest req){
		JSONObject jsonObject = new JSONObject(); 
		jsonObject.put("result","10001");
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String) jsonObj.get("mobileNo");
				 Map<String,Object> likeCondition = new HashMap<String, Object>();
				 likeCondition.put("mobileNo",mobileNo);
				 likeCondition.put("delMark", JOYCoupon.NON_DELMARK);
				 List<JOYCoupon> joyCoupons = joycouponService.getJOYCoupon(likeCondition);
				 JSONArray jsonArray = new JSONArray();
				 if (joyCoupons.size() > 0) {
					 for (JOYCoupon joyCoupon : joyCoupons) {
						 	jsonArray.add(joyCoupon.toJSON());	
					}
					 	jsonObject.put("result","10000");
						jsonObject.put("Coupons",jsonArray);
				}else{
					jsonObject.put("result","10002");
					jsonObject.put("errMsg","没有这个用户的优惠券信息");
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		
		
		return jsonObject;
	}
	
	
	@RequestMapping(value="usermgr/addConpon",method=RequestMethod.POST)
	public @ResponseBody  JSONObject addConpon(HttpServletRequest  req){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String) jsonObj.get("mobileNo");
				 String  couponNum = (String) jsonObj.get("couponNum");
				 BigDecimal bigDecimal = BigDecimal.valueOf(Double.parseDouble(couponNum));
				 JOYCoupon  joyCoupon = new  JOYCoupon();
				 joyCoupon.mobileNo = (mobileNo);
				 //joyCoupon.setCouponNum(bigDecimal);
				 joycouponService.insertJOYCoupon(joyCoupon);
				 jsonObject.put("result","10000");
			}catch(Exception e){
				e.printStackTrace();
			}
		
		return  jsonObject;
	}
	
}
