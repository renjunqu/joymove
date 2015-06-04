package com.joymove.view;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
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

import com.joymove.service.*;
import com.joymove.entity.*;
import com.joymove.service.JOYPowerBarService;



@Controller("JOYPowerBarController")
public class JOYPowerBarController {
	@Resource(name = "JOYPowerBarService")
	private  JOYPowerBarService joyPowerBarService;


	@RequestMapping(value="rent/getNearByPowerBars", method=RequestMethod.POST)
	public  @ResponseBody JSONObject getNearByPowerBars(HttpServletRequest req){
		 System.out.println("getNearByPowerBars method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 JSONArray  pbArray  = new JSONArray();
		
		 Reobj.put("result", "10001");
		 Reobj.put("powerbars", pbArray);
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");;
			 likeCondition.put("userPositionX", jsonObj.get("userLongitude")==null ? 0.0: jsonObj.get("userLongitude") );
			 likeCondition.put("userPositionY", jsonObj.get("userLatitude")==null ? 0.0: jsonObj.get("userLatitude") );
			 likeCondition.put("scope", jsonObj.get("scope")==null ? 10 : jsonObj.get("scope") );
			 List<JOYPowerBar> pbs = joyPowerBarService.getPowerBarByScope(likeCondition);
			 
			 Iterator iter = pbs.iterator();
			 while(iter.hasNext()){
				 JOYPowerBar pb_item  = (JOYPowerBar)iter.next();
				 JSONObject pb_json = new JSONObject();
				 pb_json.put("powerbarId", pb_item.id);
				 pb_json.put("longitude",  pb_item.positionX);
				 pb_json.put("latitude",  pb_item.positionY);
				 pb_json.put("desp",  pb_item.desp);
				 pbArray.add(pb_json);
			 }
			 Reobj.put("result", "10000");
			 
		 } catch(Exception e) {
			 Reobj.put("result", "10001");
			 System.out.println(e);
		 }
		 return Reobj;
	}

}
