package com.joymove.view;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.joymove.entity.JOYUser;
import com.joymove.service.JOYUserService;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import sun.misc.BASE64Decoder;

import com.joymove.entity.JOYIdAuthInfo;
import com.joymove.service.JOYIdAuthInfoService;
import sun.misc.BASE64Encoder;


@Controller("JOYIdAuthInfoController")
public class JOYIdAuthInfoController {

	@Resource(name = "JOYIdAuthInfoService")
	private JOYIdAuthInfoService joyIdAuthInfoService;

	@Resource(name = "JOYUserService")
	private JOYUserService joyUserService;


	/****** business proc ***********/

	@RequestMapping(value="usermgr/updateIdAuthInfo",method=RequestMethod.POST)
	public @ResponseBody JSONObject updateIdAuthInfo(HttpServletRequest req){
		JSONObject Reobj = new JSONObject();
		Reobj.put("result", "10001");
		JOYIdAuthInfo authInfo = new JOYIdAuthInfo();
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 JOYIdAuthInfo authInfoFilter = new JOYIdAuthInfo();
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 authInfoFilter.mobileNo = mobileNo;
				 String idNo = (String) jsonObj.get("id_no");
				 String idName = (String) jsonObj.get("id_name");
				 String idCard = (String) jsonObj.get("id_card");
				 String idCard_back = (String) jsonObj.get("id_card_back");
				 authInfo.mobileNo = (mobileNo);
				 authInfo.idName = (idName);
				 authInfo.idNo = (idNo);
				 authInfo.idAuthInfo =  idCard;
				 authInfo.idAuthInfo_back = idCard_back;
				 
				 
				 List<JOYIdAuthInfo> infos = joyIdAuthInfoService.getNeededList(authInfoFilter);
				JOYUser userFilter = new JOYUser();
				JOYUser userValue = new JOYUser();
				userFilter.mobileNo = mobileNo;
				userValue.authenticateId = JOYUser.auth_state_ing;

				 if(infos.size()==0) {
					 //update 
					 joyIdAuthInfoService.insertRecord(authInfo);
					 joyUserService.updateRecord(userValue,userFilter);
					 
				 } else {
					 //insert 
					 joyIdAuthInfoService.updateRecord(authInfo, authInfoFilter);
					 joyUserService.updateRecord(userValue,userFilter);
				 }
				/*
				 BASE64Decoder decode = new BASE64Decoder();
					byte[] byteImage = decode.decodeBuffer(image);
					JOYIdAuthInfo joyIdAuthInfo = new JOYIdAuthInfo();
					joyIdAuthInfo.setMobileNo(phoneNo);
					joyIdAuthInfo.setIdAuthInfo(byteImage);
			
					joyIdAuthInfoService.insertIdAuthInfo(joyIdAuthInfo);
						
					jsonObject.put("result",0);
				*/
				 Reobj.put("result", "10000");
			}catch(Exception e){
				e.printStackTrace();
			}
			return Reobj;
		
	}

	@RequestMapping(value="usermgr/getIdAuthInfo",method=RequestMethod.POST)
	public @ResponseBody JSONObject getIdAuthInfo(HttpServletRequest req){
		JSONObject Reobj = new JSONObject();
		Reobj.put("result", "10001");

		try{
			Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			JOYIdAuthInfo authInfoFilter = new JOYIdAuthInfo();

			String mobileNo = (String)jsonObj.get("mobileNo");
			if(mobileNo!=null) {
					authInfoFilter.mobileNo = mobileNo;
					List<JOYIdAuthInfo> infos = joyIdAuthInfoService.getNeededList(authInfoFilter);
					if (infos.size() == 0) {
						Reobj.put("result", "10002");
					} else {
						JOYIdAuthInfo authInfo = infos.get(0);
						BASE64Encoder encoder = new BASE64Encoder();
						Reobj.put("idName", authInfo.idAuthInfo);
						Reobj.put("idNo", authInfo.idNo);
						Reobj.put("idAuthInfo", authInfo.idAuthInfo);
						Reobj.put("idAuthInfo_back", authInfo.idAuthInfo_back);
						Reobj.put("result", "10000");
					}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return Reobj;

	}


	


	
	
}
