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


@Scope("prototype")
@Controller("JOYDriverLicenseController")
public class JOYDriverLicenseController {


	@Resource(name = "JOYDriverLicenseService")
	private JOYDriverLicenseService joyDriverLicenseService;

	@Resource(name = "JOYUserService")
	private JOYUserService   joyUserService;
	

	/********       business proc ********************/
	
	
	@RequestMapping(value="userImage/getDriverAuthInfo",method=RequestMethod.GET)
	public void getDriverAuthInfo(HttpServletRequest req,HttpServletResponse res){
	
			try{
				 
				 String mobileNo = req.getParameter("moblieNo");
				 Map<String,Object> likeCondition = new HashMap<String, Object>();
				 likeCondition.put("mobileNo",mobileNo);
				 List<JOYDriverLicense> driAuthInfos = joyDriverLicenseService.getDriverAuthInfo(likeCondition);
				 byte[] bt = null;
				 if (driAuthInfos.size() > 0) {
					for (JOYDriverLicense driAuthInfo : driAuthInfos) {
						InputStream inputStream = new  ByteArrayInputStream(driAuthInfo.driverAuthInfo);
						bt = FileCopyUtils.copyToByteArray(inputStream);
						res.setContentType("image/jpg");
						OutputStream os = res.getOutputStream();
						os.write(bt);
						os.flush();
						os.close();
						
						}
					}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		
	}
	
	@RequestMapping(value="usermgr/updateDriverAuthInfo",method=RequestMethod.POST)
	public @ResponseBody JSONObject updateDriverAuthInfo(HttpServletRequest req){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 String image = (String) jsonObj.get("image");
				 String driverNumber = (String) jsonObj.get("driverNumber");
				 Date expireTime = new Date(Long.parseLong(jsonObj.get("expireTime").toString()));
				 BASE64Decoder decode = new BASE64Decoder();
				 byte[] byteImage = decode.decodeBuffer(image);
				 Map<String,Object> likeCondition = new HashMap<String, Object>();
				 likeCondition.put("mobileNo",mobileNo);
				 List<JOYDriverLicense> drivers = joyDriverLicenseService.getDriverAuthInfo(likeCondition);
				 JOYDriverLicense  driverLicense = new JOYDriverLicense();
				 JOYUser  joyUser = new JOYUser();
				 if (drivers.size() == 0) {
					 driverLicense.mobileNo = (mobileNo);
					 driverLicense.driverAuthInfo = (byteImage);
					 driverLicense.driverLicenseNumber = (driverNumber);
					 driverLicense.expireTime = (expireTime);
					 joyDriverLicenseService.insertDriverAuthInfo(driverLicense);
					 jsonObject.put("result","10000");
				}else{
					for (JOYDriverLicense driver : drivers) {
						 driver.driverAuthInfo = (byteImage);
						 driver.mobileNo = (mobileNo);
						 driver.driverLicenseNumber = (driverNumber);
						 joyDriverLicenseService.updateJOYDriverLicense(driver);
						 jsonObject.put("result","10000");
					}
				}
				 
				 
			
			}catch(Exception e){
				e.printStackTrace();
			}
			return jsonObject;
		
	}



	
}
