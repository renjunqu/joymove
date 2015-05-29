package com.joymove.view;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.joymove.entity.JOYWXPayInfo;
import com.joymove.service.JOYWXPayInfoService;
import com.joymove.util.WeChatPay.WeChatPayUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.json.simple.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import sun.misc.BASE64Decoder;




import com.futuremove.cacheServer.utils.ConfigUtils;
import com.joymove.entity.JOYDynamicPws;
import com.joymove.entity.JOYUser;
import com.joymove.service.JOYDynamicPwsService;
import com.joymove.service.JOYUserService;
import com.joymove.util.zhifubao.ZhifubaoUtils;
import org.json.simple.parser.JSONParser;



@Scope("prototype")
@Controller("JOYUserController")
public class JOYUserController{
	@Resource(name = "JOYUserService")
	private JOYUserService joyUserService;
	@Resource(name = "JOYDynamicPwsService")
    private JOYDynamicPwsService joyDynamicPwsService;
	@Resource(name = "JOYWXPayInfoService")
	private JOYWXPayInfoService joywxPayInfoService;
	

	
	/*********   business proc  *****************/
	/**
     * @return
	 */
	@RequestMapping(value="cachemgr/triggerUser",method=RequestMethod.GET)
	public @ResponseBody JSONObject cacheTrigger(HttpServletRequest req){
		JSONObject  jsonObject = new JSONObject();
		jsonObject.put("result","10001");
		try{
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 String mobileNo = req.getParameter("mobileNo");
			 JOYUser user = new JOYUser();
			 user.mobileNo = mobileNo; //setMobileNo(mobileNo);
			 joyUserService.triggerUserCache(user);
		}catch(Exception e){
			e.printStackTrace();
		}

		return jsonObject;
	}
	
	
	
	
	/**
     * @return
	 */
	@RequestMapping(value="usermgr/register",method=RequestMethod.POST)
	public @ResponseBody JSONObject registration(HttpServletRequest req){
		JSONObject  jsonObject = new JSONObject();
		jsonObject.put("result","10001");
		try{
			
			  Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 String code = String.valueOf(jsonObj.get("code"));
				 //String username = (String)jsonObj.get("username");
				 String password = (String)jsonObj.get("password");
				 Map<String,Object> likeCondition = new HashMap<String, Object>();
				 likeCondition.put("mobileNo",mobileNo);
				 List<JOYDynamicPws> dynamicPws = joyDynamicPwsService.getDynamicPws(likeCondition);
				 JOYUser user = new JOYUser();	
				 if (password.length() <= 5 || password.length() >= 13) {
					 jsonObject.put("errMsg","密码格式不对");
				}else{
					JOYDynamicPws joyDynamicPws = dynamicPws.get(0);
					Date time = joyDynamicPws.createTime;
					if(joyDynamicPws.code.equals(code)){
							if((System.currentTimeMillis() - time.getTime())/(60000) < 15){
									user.mobileNo = mobileNo; //setMobileNo(mobileNo);
									//user.setUserName(username);
									user.userpwd = password; //setUserpwd(password);
									joyUserService.insertJOYUser(user);
									jsonObject.put("result","10000");
							} else {
								jsonObject.put("errMsg","验证码超时");
							} 
					} else {
						jsonObject.put("errMsg","验证码错误");
					}
			}	  
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		return jsonObject;
	}
	
	
	@RequestMapping(value="usermgr/login",method=RequestMethod.POST)
	public @ResponseBody JSONObject userLogin(HttpServletRequest req) {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("result","10001");
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 String password = (String)jsonObj.get("password");
				 JOYUser user = new JOYUser();
				 user.mobileNo = mobileNo; //setMobileNo(mobileNo);
				 List<JOYUser> joyUsers = joyUserService.getNeededUser(user);
				 if (joyUsers.size() > 0) {
					for (JOYUser joyUser : joyUsers) {
						String userpwd = joyUser.userpwd; //getUserpwd();
						if (password.equals(userpwd)) {
							
							 String authToken = joyUser.mobileNo /*getMobileNo()*/+"###"+String.valueOf(UUID.randomUUID());
							 joyUser.authToken = authToken; //setAuthToken(authToken);
							 joyUser.lastActiveTime = new Date(System.currentTimeMillis()); //setLastActiveTime(new Date(System.currentTimeMillis()));
							 joyUser.mobileNo = mobileNo; //setMobileNo(mobileNo);
							 joyUserService.updateJOYUser(joyUser);
							 jsonObject.put("result", "10000");
							 jsonObject.put("authToken", authToken);
							
						}else{
							jsonObject.put("errMsg","密码错误");
						}
					}
				}else{
					jsonObject.put("errMsg","用户名不存在");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		
		return jsonObject;

	}
	
	@RequestMapping(value="usermgr/updatePwd",method=RequestMethod.POST)
	public @ResponseBody JSONObject updatePwd(HttpServletRequest req){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 String passWord = (String)jsonObj.get("password");
				 String chPwd = (String) jsonObj.get("changepassword");
				 
				
				 if (chPwd.length() <= 5 || chPwd.length() >= 13) {
					 jsonObject.put("errMsg","瀵嗙爜闀垮害蹇呴』涓�-12浣嶆暟瀛楀拰瀛楁瘝");
				}else{
				
					JOYUser joyUser = (JOYUser)req.getAttribute("cUser");	
					 if (passWord.equals(joyUser.userpwd)) {
						 joyUser.userpwd = chPwd; //setUserpwd(chPwd);
						 joyUser.mobileNo = mobileNo; //setMobileNo(mobileNo);
						 joyUserService.updateJOYUser(joyUser);
						 jsonObject.put("result","10000");
					}else{
						jsonObject.put("errMsg","瀵嗙爜閿欒");
					}
				
			}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		
		return jsonObject;
	}
	
	
	
	@RequestMapping(value="usermgr/resetPwd",method=RequestMethod.POST)
	public @ResponseBody JSONObject resetPwd(HttpServletRequest req){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 String passWord = (String)jsonObj.get("password");
				 String code = String.valueOf(jsonObj.get("code"));
				 Map<String,Object> likeCondition = new HashMap<String, Object>();
				 likeCondition.put("mobileNo",mobileNo);
				 List<JOYDynamicPws> dynamicPws = joyDynamicPwsService.getDynamicPws(likeCondition);
				 JOYDynamicPws joyDynamicPws = dynamicPws.get(0);
				 
				
				if(joyDynamicPws.code.equals(code) && ((System.currentTimeMillis() - joyDynamicPws.createTime.getTime())/(60000) < 15)){
				
					
					 if (passWord.length() <= 5 || passWord.length() >= 13) {
						 jsonObject.put("errMsg","验证码格式错误");
					}else{
					
							 JOYUser joyUser = (JOYUser)req.getAttribute("cUser");	
							 joyUser.userpwd = passWord; //setUserpwd(passWord);
							 joyUser.mobileNo = mobileNo; //setMobileNo(mobileNo);
							 joyUserService.updateJOYUser(joyUser);
							 jsonObject.put("result","10000");	
					}
				} else {
					jsonObject.put("errMsg","验证码错误");
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		return jsonObject;
	}
	
	
	@RequestMapping(value={"usermgr/viewBaseInfo","usermgr/getCommonDestination","usermgr/checkUserState","usermgr/getBioLogicalInfo"},method=RequestMethod.POST)
	public @ResponseBody JSONObject userInfo(HttpServletRequest req){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 JOYUser joyUser = (JOYUser)req.getAttribute("cUser");	
				 String URI = req.getRequestURI();
				 if(URI.contains("viewBaseInfo")) {
					 jsonObject.put("username",joyUser.username);
					 jsonObject.put("gender",joyUser.gender);
					 jsonObject.put("mobileNo",joyUser.mobileNo);
					 jsonObject.put("driverlicenseCertification", joyUser.authenticateDriver);
					 jsonObject.put("deposit",joyUser.deposit);
					 jsonObject.put("result","10000");
				 } else if (URI.contains("getCommonDestination")){
					 JSONObject homeAddr = new JSONObject();
					 JSONObject corpAddr = new JSONObject();
					 
					 homeAddr.put("name", joyUser.homeAddr);
					 homeAddr.put("latitude", joyUser.homeLatitude);
					 homeAddr.put("longitude", joyUser.homeLongitude);
					 corpAddr.put("name", joyUser.corpAddr);
					 corpAddr.put("latitude", joyUser.corpLatitude);
					 corpAddr.put("longitude", joyUser.corpLongitude);
					 jsonObject.put("home", homeAddr);
					 jsonObject.put("corp", corpAddr);
					 jsonObject.put("result","10000");
				 } else if (URI.contains("checkUserState")){
				
					 jsonObject.put("mobileAuthState",1);
					 jsonObject.put("id5AuthState",joyUser.authenticateId);
					 jsonObject.put("driverLicAuthState",joyUser.authenticateDriver);
					 BigDecimal deposit = joyUser.deposit;
					 if(deposit!=null && deposit.compareTo(new BigDecimal(0.01))>=0) {
						 jsonObject.put("depositState",1);
					 } else {
						 jsonObject.put("depositState",0);
					 }
					 jsonObject.put("result","10000");
			     } else if(URI.contains("getBioLogicalInfo")) {
                     jsonObject.put("face_info",joyUser.face_info);
					 jsonObject.put("voice_info",joyUser.voice_info);
					 jsonObject.put("result","10000");
				 }
			}catch(Exception e){
				e.printStackTrace();
			}
		
		return jsonObject;
	}
	
	
	@RequestMapping(value={"usermgr/updateInfo","usermgr/updateBiologicalInfo"}, method=RequestMethod.POST)
	public @ResponseBody JSONObject updateInfo(HttpServletRequest req){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				String URI = req.getRequestURI();

				if(URI.contains("updateBiologicalInfo")) {
					String face_info = (String) jsonObj.get("face_info");
					String voice_info = (String) jsonObj.get("voice_info");
					JOYUser joyUser = (JOYUser) req.getAttribute("cUser");
					joyUser.face_info = face_info;
					joyUser.voice_info = voice_info;
					joyUser.mobileNo = mobileNo;
					joyUserService.updateJOYUser(joyUser);
					jsonObject.put("result", "10000");
				} else if (URI.contains("updateInfo")) {
						String username = (String) jsonObj.get("username");
						String gender = (String) jsonObj.get("gender");
						JOYUser joyUser = (JOYUser) req.getAttribute("cUser");
						String[] settingProps = {"username", "gender"};
					    if(username!=null)
							joyUser.username = username;
					    if(gender!=null)
					    	joyUser.gender = gender;
						joyUser.mobileNo = mobileNo;
						joyUserService.updateJOYUser(joyUser);
						jsonObject.put("result", "10000");
				}

			}catch(Exception e){
				e.printStackTrace();
			}
		
		return jsonObject;
	}
	public static void main(String[] args){
		JOYUser user = new JOYUser();
		try {
			BeanUtils.setProperty(user, "gender", "11111111");
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(user.gender);
	}

	@RequestMapping(value="usermgr/depositRecharge",method=RequestMethod.POST)
	public @ResponseBody JSONObject depositRecharge(HttpServletRequest req){
		JSONObject Reobj = new JSONObject();
		Reobj.put("result","10001");
			try{
				Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 Double balance = Double.valueOf(jsonObj.get("balance").toString());
				 String currTimeStr = System.currentTimeMillis()+"";
				 /* generate zhifubao's code **/
				 String zhifubao_code = ZhifubaoUtils.getPayInfo("depositRecharge", mobileNo, balance, currTimeStr+balance.toString().replace(".", "-"));
				 /** generate wx's code */
				JOYWXPayInfo wxpayInfo = new JOYWXPayInfo();
				 wxpayInfo.mobileNo = (mobileNo);
				 String wx_trade_no = "depositRecharge" + mobileNo + String.valueOf(System.currentTimeMillis()).substring(8,12);
				 wxpayInfo.out_trade_no = (wx_trade_no);
				 wxpayInfo.totalFee = (Double.valueOf(balance));
				 String wx_code = WeChatPayUtil.genePayStr(String.valueOf(Double.valueOf(balance*100).longValue()),wx_trade_no);
				 joywxPayInfoService.insertWXPayInfo(wxpayInfo);
				/**generate result **/
				 Reobj.put("result", "10000");
				 Reobj.put("zhifubao_code", zhifubao_code);
				Reobj.put("wx_code",new JSONParser().parse(wx_code));
			}catch(Exception e){
				e.printStackTrace();
			}
		
		return Reobj;
	}
	
	
	
	
	@RequestMapping(value="usermgr/updateIma",method=RequestMethod.POST)
	public @ResponseBody JSONObject userIma(HttpServletRequest req){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result","10001");
			try{
				Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 String image = (String) jsonObj.get("image");
				 BASE64Decoder decode = new BASE64Decoder();
				 String imagePath = ConfigUtils.getPropValues("upload.images")+"/"+mobileNo+".jpg";
				 byte[] byteImage = decode.decodeBuffer(image);
				 OutputStream  output = new BufferedOutputStream(new FileOutputStream(imagePath));
			     output.write(byteImage);
				jsonObject.put("result","10000");
				
			}catch(Exception e){
				e.printStackTrace();
			}
		
		return jsonObject;
	}
	
	@RequestMapping(value="userImage/getIma",method=RequestMethod.GET)
	public String getIma(HttpServletRequest req,HttpServletResponse res){
			
		try{
			String  mobileNo = (String) req.getParameter("mobileNo");
			byte[] bt = null;
			String imagePath = ConfigUtils.getPropValues("upload.images")+"/"+mobileNo+".jpg";
			 File file = new File(imagePath);
			 
			InputStream inputStream = new FileInputStream(file);
			bt = FileCopyUtils.copyToByteArray(inputStream);
			res.setContentType("image/jpg");
			OutputStream os = res.getOutputStream();
			os.write(bt);
			os.flush();
			os.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	@RequestMapping(value="usermgr/checkUserMobileNo",method=RequestMethod.POST)
	public @ResponseBody JSONObject checkUserMobileNo(HttpServletRequest req){
		JSONObject  jsonObject = new JSONObject();
		//if the user exists , it will be filtered by the interceptor 
		jsonObject.put("result","10000");
		return jsonObject;
	}
	
	
	@RequestMapping(value="usermgr/updateCommonDestination",method=RequestMethod.POST)
	public @ResponseBody JSONObject updateCommonDestination(HttpServletRequest req){
		JSONObject  Reobj = new JSONObject();
		Reobj.put("result","10001");
			try{
				
				Hashtable<String,Object> jsonObj = (Hashtable<String, Object>) req.getAttribute("jsonArgs");
				JOYUser user = (JOYUser)req.getAttribute("cUser");
				JSONObject homeAddr = (JSONObject)jsonObj.get("home");
				JSONObject corpAddr = (JSONObject)jsonObj.get("corp");
				if(homeAddr!=null){
					user.homeAddr = (String)homeAddr.get("name");
					user.homeLatitude = BigDecimal.valueOf(Double.valueOf(String.valueOf(homeAddr.get("latitude"))));
					user.homeLongitude = (BigDecimal.valueOf(Double.valueOf(String.valueOf(homeAddr.get("longitude")))));
				}
				if(corpAddr!=null) {
					user.corpAddr = ((String)corpAddr.get("name"));
					user.corpLatitude = (BigDecimal.valueOf(Double.valueOf(String.valueOf(corpAddr.get("latitude")))));
					user.corpLongitude = (BigDecimal.valueOf(Double.valueOf(String.valueOf(corpAddr.get("longitude")))));
					
				}
				
				joyUserService.updateJOYUser(user);
				Reobj.put("result","10000");
			}catch(Exception e){
				e.printStackTrace();
			}
		
		
		return Reobj;
	}
	
	
	
}
