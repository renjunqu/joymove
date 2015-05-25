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

import com.joymove.service.JOYParkService;
import com.joymove.entity.JOYPark;


@Scope("prototype")
@Controller("JOYParkController")
public class JOYParkController {
	@Resource(name = "JOYParkService")
	private  JOYParkService joyParkService;



	@RequestMapping(value="rent/getNearByParks", method=RequestMethod.POST)
	public  @ResponseBody JSONObject getNearByParks(HttpServletRequest req){
		 System.out.println("getNearByParks method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 JSONArray  parkArray  = new JSONArray();
		
		 Reobj.put("result", "10001");
		 Reobj.put("parks", parkArray);
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 likeCondition.put("userPositionX", jsonObj.get("userLongitude")==null ? 0.0: jsonObj.get("userLongitude") );
			 likeCondition.put("userPositionY", jsonObj.get("userLatitude")==null ? 0.0: jsonObj.get("userLatitude") );
			 likeCondition.put("scope", jsonObj.get("scope")==null ? 10 : jsonObj.get("scope") );
			 List<JOYPark> parks = joyParkService.getParkByScope(likeCondition);
			 
			 Iterator iter = parks.iterator();
			 while(iter.hasNext()){
				 JOYPark park_item  = (JOYPark)iter.next();
				 JSONObject park_json = new JSONObject();
				 park_json.put("parkId", park_item.id);
				 park_json.put("longitude",  park_item.positionX);
				 park_json.put("latitude",  park_item.positionY);
				 park_json.put("desp",  park_item.desp);
				
				 parkArray.add(park_json);
			 }
			 Reobj.put("result", "10000");
			 
		 } catch(Exception e) {
			 Reobj.put("result", "10001");
			 System.out.println(e);
		 }
		 return Reobj;
	}

}
