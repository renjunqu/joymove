package com.joymove.view;

import java.io.IOException;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joymove.entity.JOYDynamicPws;
import com.joymove.service.JOYDynamicPwsService;
import com.joymove.util.JsonHashUtils;
import com.joymove.util.SmsUtils;


@Controller("JOYDynamicPwsController")
public class JOYDynamicPwsController {


	@Resource(name = "JOYDynamicPwsService")
	private JOYDynamicPwsService joydynamicpwsService;
	
	@RequestMapping(value="/usermgr/dynamicPwsGen", method=RequestMethod.POST)
	public @ResponseBody JSONObject addMBKRegisterCode(HttpServletRequest req) {
		
		JSONObject jsonObjArr = new JSONObject();
		jsonObjArr.put("result","10001");
		
		try {
			Hashtable<String,Object> hash = (Hashtable<String,Object>)req.getAttribute("jsonArgs");//JsonHashUtils.strToJSONHash(req.getReader());
			
				jsonObjArr=new JSONObject();
				
		    	  int number = 0;
		    	  int i =0;
		          Set set = new HashSet();
		          int[] array = new int[4];
		          while (i++ < 4) {
		              int temp = (int)(Math.random() * 10);
		              if(temp==0)
		            	  temp = 1;
		              number = number*10 + temp;
		         
		              jsonObjArr.put("result","10000");
		              jsonObjArr.put("number",number);
		          }
		          String  mobileNo = (String) hash.get("mobileNo");
		          
		          JOYDynamicPws dynamicPws = new JOYDynamicPws();
		          
		          dynamicPws.mobileNo = (mobileNo);
		          dynamicPws.code = (String.valueOf(number));
		          //dynamicPws.setCreateTime(new Date());
		          joydynamicpwsService.insertDynamicPwse(dynamicPws);
		          SmsUtils.sendRegisterCode(String.valueOf(number), String.valueOf(mobileNo));
	            //  jsonObjArr.put("result","10000");
	            //  jsonObjArr.put("number",number);
		          
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObjArr;

			}
	
	@RequestMapping(value="/usermgr/dynamicPwsVeri", method=RequestMethod.POST)
	public @ResponseBody JSONObject getMBKRegisterCode(HttpServletRequest req) {
		
		JSONObject Reobj=new JSONObject();
		Reobj.put("result",1);
		
		
		//{"number":"3754","PhoneNo":"111111"}
		//
		
		try {
			Hashtable<String,Object> hash = (Hashtable<String,Object>)req.getAttribute("jsonArgs");//JsonHashUtils.strToJSONHash(req.getReader());
				
				String number = (String)hash.get("number");
				
				String phone = (String) hash.get("phoneNo");
				Map<String,Object> likeCondition = new HashMap<String, Object>();
				likeCondition.put("mobileNo",hash.get("phoneNo"));
				List<JOYDynamicPws> pwss = joydynamicpwsService.getDynamicPws(likeCondition);
				
				
				if(pwss.size()==1) {
				JOYDynamicPws dynamicPws = pwss.get(0);
				Date time = dynamicPws.createTime;
					if(dynamicPws.code.equals(number)){
						if(time!=null && ((System.currentTimeMillis() - time.getTime())/(60000) < 5)){
							Reobj.put("result",0);
						} 
			       }		
			   }
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Reobj.put("result",1);
		}
		
		
		return Reobj;
		
	}
	
}
