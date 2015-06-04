package com.joymove.view;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
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

import com.joymove.entity.JOYDriverLicense;
import com.joymove.entity.JOYUser;
import com.joymove.service.JOYDriverLicenseService;
import com.joymove.service.JOYUserService;
import sun.misc.BASE64Encoder;


@Controller("JOYDriverLicenseController")
public class JOYDriverLicenseController {


	@Resource(name = "JOYDriverLicenseService")
	private JOYDriverLicenseService joyDriverLicenseService;

	@Resource(name = "JOYUserService")
	private JOYUserService   joyUserService;
	

	/********       business proc ********************/


	@RequestMapping(value="usermgr/getDriverAuthInfo",method=RequestMethod.POST)
	public @ResponseBody JSONObject getDriverAuthInfo(HttpServletRequest req,HttpServletResponse res){
			JSONObject Reobj = new JSONObject();
			Reobj.put("result", "10001");
			try{

				Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				String mobileNo = (String)jsonObj.get("mobileNo");
				 Map<String,Object> likeCondition = new HashMap<String, Object>();
				 likeCondition.put("mobileNo", mobileNo);
				 List<JOYDriverLicense> driveAuthInfos = joyDriverLicenseService.getDriverAuthInfo(likeCondition);
				 if(driveAuthInfos.size()>0) {
					 BASE64Encoder encoder = new BASE64Encoder();
					 JOYDriverLicense driverLicense = driveAuthInfos.get(0);
					 Reobj.put("driverLicenseNumber",driverLicense.driverLicenseNumber);
					 Reobj.put("expireTime",driverLicense.expireTime);
					 Reobj.put("driverAuthInfo",encoder.encode(driverLicense.driverAuthInfo));
					 Reobj.put("result", "10000");
				 } else {
					 Reobj.put("result","10002");
				 }
				
			}catch(Exception e){
				e.printStackTrace();
			}
		return Reobj;
	}
	
	@RequestMapping(value="usermgr/updateDriverAuthInfo",method=RequestMethod.POST)
	public @ResponseBody JSONObject updateDriverAuthInfo(HttpServletRequest req){
		JSONObject Reobj = new JSONObject();
		Map<String,Object> likeCondition = new HashMap<String, Object>();
		Reobj.put("result", "10001");
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 String image = (String) jsonObj.get("image");
				 String driverNumber = String.valueOf(jsonObj.get("driverNumber"));
				 Date expireTime = new Date(Long.parseLong(jsonObj.get("expireTime").toString()));
				 BASE64Decoder decode = new BASE64Decoder();
				 byte[] byteImage = decode.decodeBuffer(image);
				 likeCondition.put("mobileNo",mobileNo);
				 List<JOYDriverLicense> drivers = joyDriverLicenseService.getDriverAuthInfo(likeCondition);
				 JOYDriverLicense  driverLicense = new JOYDriverLicense();
				 driverLicense.mobileNo = (mobileNo);
				 driverLicense.driverAuthInfo = (byteImage);
				 driverLicense.driverLicenseNumber = (driverNumber);
				 driverLicense.expireTime = (expireTime);

				 if (drivers.size() == 0) {
					 joyDriverLicenseService.insertDriverAuthInfo(driverLicense);
					 Reobj.put("result", "10000");
				 } else {
					for (JOYDriverLicense driver : drivers) {
						 joyDriverLicenseService.updateJOYDriverLicense(driver);
						 Reobj.put("result", "10000");
					}
				}
				 
				 
			
			}catch(Exception e){
				e.printStackTrace();
			}
			return Reobj;
		
	}



	
}
