package com.joymove.view;

import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joymove.entity.JOYDynamicPws;
import com.joymove.service.JOYDynamicPwsService;
import com.joymove.util.SmsUtils;


@Controller("JOYDynamicPwsController")
public class JOYDynamicPwsController {


	@Resource(name = "JOYDynamicPwsService")
	private JOYDynamicPwsService joydynamicpwsService;
	
	@RequestMapping(value="/usermgr/dynamicPwsGen", method=RequestMethod.POST)
	public @ResponseBody JSONObject dynamicPwsGen(HttpServletRequest req) {
		
		JSONObject Reobj = new JSONObject();
		Reobj.put("result", "10001");
		
		try {
			Hashtable<String,Object> jsonArgs = (Hashtable<String,Object>)req.getAttribute("jsonArgs");//JsonHashUtils.strToJSONHash(req.getReader());

				  String base = "123456789";
				  Random random = new Random();
			      StringBuffer sb = new StringBuffer();
			      for (int i = 0; i < 4; i++) {
				        int number = random.nextInt(base.length());
			        	sb.append(base.charAt(number));
			      }

			      String  mobileNo = (String) jsonArgs.get("mobileNo");
		          
		          JOYDynamicPws dynamicPws = new JOYDynamicPws();
		          
		          dynamicPws.mobileNo = (mobileNo);
		          dynamicPws.code = sb.toString();
		          //dynamicPws.setCreateTime(new Date());
		          joydynamicpwsService.insertRecord(dynamicPws);
		          SmsUtils.sendRegisterCode(sb.toString(), String.valueOf(mobileNo));
			      Reobj.put("result", "10000");
			      Reobj.put("number", sb.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Reobj;

	}
	
	@RequestMapping(value="/usermgr/dynamicPwsVeri", method=RequestMethod.POST)
	public @ResponseBody JSONObject dynamicPwsVeri(HttpServletRequest req) {
		
		JSONObject Reobj=new JSONObject();
		Reobj.put("result","10001");
		
		
		//{"number":"3754","PhoneNo":"111111"}
		//
		
		try {
			Hashtable<String,Object> jsonObj = (Hashtable<String,Object>)req.getAttribute("jsonArgs");//JsonHashUtils.strToJSONHash(req.getReader());
				String number = String.valueOf(jsonObj.get("number"));
				
				String phone = String.valueOf(jsonObj.get("phoneNo"));
				JOYDynamicPws dynamicPwsFilter = new JOYDynamicPws();
			    dynamicPwsFilter.mobileNo = String.valueOf(jsonObj.get("phoneNo"));
				List<JOYDynamicPws>joyDynamicPwsess = joydynamicpwsService.getNeededList(dynamicPwsFilter,0,1,"DESC");

				if(joyDynamicPwsess.size()==1) {
				JOYDynamicPws dynamicPws = joyDynamicPwsess.get(0);
				Date time = dynamicPws.createTime;
					if(dynamicPws.code.equals(number)){
						if(time!=null && ((System.currentTimeMillis() - time.getTime())/(60000) < 5)){
							Reobj.put("result","10000");
						} 
			       }		
			   }
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Reobj;
		
	}
	
}
