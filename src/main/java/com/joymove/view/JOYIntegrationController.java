package com.joymove.view;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joymove.entity.JOYIntegration;
import com.joymove.service.JOYIntegrationService;


@Controller("JOYIntegrationController")
public class JOYIntegrationController {
	@Resource(name = "JOYIntegrationService")
	private JOYIntegrationService joyIntegrationService;
	
	

	/*******   business proc **************/

	@RequestMapping(value="usermgr/viewJifen", method = RequestMethod.POST)
	public @ResponseBody JSONObject viewJifen(HttpServletRequest req) {
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
		try {
			Hashtable<String,Object> hash = (Hashtable<String,Object>)req.getAttribute("jsonArgs");
			JOYIntegration filterObj = new JOYIntegration();
			String mobileNo = (String) hash.get("mobileNo");
			filterObj.mobileNo = mobileNo;
			List<JOYIntegration> joyIntegrations = joyIntegrationService.getNeededList(filterObj);
			JSONArray jsonArray = new JSONArray();
			if (joyIntegrations.size() > 0 ) {
				for (JOYIntegration joyIntegration : joyIntegrations) {
					 jsonArray.add(joyIntegration.toJSON());
				}
				
				Integer sumCredits = 0;
				for(JOYIntegration credit:joyIntegrations) {
					sumCredits += credit.jifen;
					jsonObject.put("result","10000");
					jsonObject.put("Integrations",jsonArray);
					jsonObject.put("TotalPoints",sumCredits);
					
					}
			}else{
				jsonObject.put("result","10002");
				jsonObject.put("errMsg","没有这个用户的积分信息");
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return jsonObject;
	}
	
	@RequestMapping(value="usermgr/addIntegration", method = RequestMethod.POST)
	public @ResponseBody JSONObject addIntegration(HttpServletRequest req){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
			try{
				Hashtable<String,Object> hash = (Hashtable<String,Object>)req.getAttribute("jsonArgs");
				String mobileNo = (String) hash.get("mobileNo");
				String jiFen = (String) hash.get("jiFen");
				String jiFenDesc = (String) hash.get("jiFanDesc");
				String statusMark = (String) hash.get("statusMark");
				JOYIntegration joyintegration = new JOYIntegration();
				joyintegration.mobileNo = (mobileNo);
				joyintegration.jifen = (Integer.valueOf(jiFen));
				joyintegration.jifenDesc = (jiFenDesc);
				joyintegration.statusMark = (Integer.valueOf(statusMark));
				joyIntegrationService.insertRecord(joyintegration);
			}catch(Exception e){
				e.printStackTrace();
			}
		
		return jsonObject;
	}


	
	
	
	
	

}
