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

import com.joymove.entity.JOYMessage;
import com.joymove.service.JOYMessageService;


@Controller("JOYMessageController")
public class JOYMessageController {

	@Resource(name = "JOYMessageService")
	private JOYMessageService joyMessageService;
	
	@RequestMapping(value="/toObtain/message",method=RequestMethod.POST)
	public @ResponseBody JSONObject getMessage(HttpServletRequest req){
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
			try{
				Hashtable<String,Object> jsonObj = (Hashtable<String, Object>) req.getAttribute("jsonArgs");
				JSONArray  jsonArray = new JSONArray();
				String  mobileNo = (String) jsonObj.get("mobileNo");
				Map<String,Object> likeCondition = new HashMap<String,Object>();
				likeCondition.put("mobileNo",mobileNo);
				List<JOYMessage> messages = joyMessageService.getJOYMessageById(likeCondition);
				/************  get message by id ***********/
				if (messages.size() > 0) {
					for (JOYMessage mess : messages) {
						jsonArray.add(mess.toJSON());
					}
						
				}
				/************  get broad cast message ***********/
				messages = joyMessageService.getJOYBroadcastMessage();
				if (messages.size() > 0) {
					for (JOYMessage mess : messages) {
						jsonArray.add(mess.toJSON());
					}
				}	
				jsonObject.put("result","10000");
				jsonObject.put("messages",jsonArray);
			}catch(Exception e){
				
				e.printStackTrace();
			}
		
		return jsonObject;
	}

}
