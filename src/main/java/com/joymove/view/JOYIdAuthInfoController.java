package com.joymove.view;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import sun.misc.BASE64Decoder;

import com.joymove.entity.JOYIdAuthInfo;
import com.joymove.entity.JOYUser;
import com.joymove.service.JOYIdAuthInfoService;
import java.io.ByteArrayInputStream;


@Scope("prototype")
@Controller("JOYIdAuthInfoController")
public class JOYIdAuthInfoController {

	@Resource(name = "JOYIdAuthInfoService")
	private JOYIdAuthInfoService joyIdAuthInfoService;

	/****** business proc ***********/

	@RequestMapping(value="usermgr/updateIdAuthInfo",method=RequestMethod.POST)
	public @ResponseBody JSONObject updateIdAuthInfo(HttpServletRequest req){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
		JOYIdAuthInfo authInfo = new JOYIdAuthInfo();;
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 Map<String,Object> likeCondition = new HashMap<String, Object>();
				 likeCondition.put("mobileNo",mobileNo);
				 String idNo = (String) jsonObj.get("id_no");
				 String idName = (String) jsonObj.get("id_name");
				 String idCard = (String) jsonObj.get("id_card");
				 String idCard_back = (String) jsonObj.get("id_card_back");
				 BASE64Decoder decode = new BASE64Decoder();
				 authInfo.mobileNo = (mobileNo);
				 authInfo.idName = (idName);
				 authInfo.idNo = (idNo);
				 authInfo.idAuthInfo =  (decode.decodeBuffer(idCard));
				 authInfo.idAuthInfo_back = (decode.decodeBuffer(idCard_back));
				 
				 
				 List<JOYIdAuthInfo> infos = joyIdAuthInfoService.getNeededIdAuthInfo(likeCondition);
				 if(infos.size()>0) {
					 //update 
					 joyIdAuthInfoService.updateIdAuthInfo(authInfo);
					 
				 } else {
					 //insert 
					 joyIdAuthInfoService.insertIdAuthInfo(authInfo);
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
				 jsonObject.put("result","10000");
			}catch(Exception e){
				e.printStackTrace();
			}
			return jsonObject;
		
	}

	


	
	
}
