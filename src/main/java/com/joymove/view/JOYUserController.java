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

import com.futuremove.cacheServer.utils.Id5Utils;
import com.joymove.entity.JOYPayReqInfo;
import com.joymove.service.JOYPayReqInfoService;
import com.joymove.util.WeChatPay.WeChatPayUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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




@Controller("JOYUserController")
public class JOYUserController{
	@Resource(name = "JOYUserService")
	private JOYUserService joyUserService;
	@Resource(name = "JOYDynamicPwsService")
    private JOYDynamicPwsService joyDynamicPwsService;
	@Resource(name = "JOYPayReqInfoService")
	private JOYPayReqInfoService  joyPayReqInfoService;


	final static Logger logger = LoggerFactory.getLogger(JOYUserController.class);








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
				 JOYDynamicPws dynamicPwsFilter = new JOYDynamicPws();
			     dynamicPwsFilter.mobileNo = mobileNo;
				 List<JOYDynamicPws> dynamicPws = joyDynamicPwsService.getNeededList(dynamicPwsFilter,0,1,"DESC");
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
									joyUserService.insertRecord(user);
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
				 List<JOYUser> joyUsers = joyUserService.getNeededList(user);
				 if (joyUsers.size() > 0) {
					for (JOYUser joyUser : joyUsers) {
						String userpwd = joyUser.userpwd; //getUserpwd();
						if (password.equals(userpwd)) {
							
							 String authToken = joyUser.mobileNo /*getMobileNo()*/+"###"+String.valueOf(UUID.randomUUID());
							 joyUser.authToken = authToken; //setAuthToken(authToken);
							 joyUser.lastActiveTime = new Date(System.currentTimeMillis()); //setLastActiveTime(new Date(System.currentTimeMillis()));
							 joyUser.mobileNo = mobileNo; //setMobileNo(mobileNo);
							 joyUserService.updateRecord(joyUser, user);
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
					 jsonObject.put("errMsg","密码长度不能小于5位");
				}else{
				
					JOYUser joyUser = (JOYUser)req.getAttribute("cUser");	
					 if (passWord.equals(joyUser.userpwd)) {
						 JOYUser userNew = new JOYUser();
						 userNew.userpwd = chPwd; //setUserpwd(chPwd);
						 joyUserService.updateRecord(userNew, joyUser);
						 jsonObject.put("result","10000");
					}else{
						jsonObject.put("errMsg","旧密码输入错误");
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
				JOYDynamicPws dynamicPwsFilter = new JOYDynamicPws();
				dynamicPwsFilter.mobileNo = mobileNo;
				List<JOYDynamicPws> dynamicPws = joyDynamicPwsService.getNeededList(dynamicPwsFilter,0,1,"DESC");
				 JOYDynamicPws joyDynamicPws = dynamicPws.get(0);
				 
				
				if(joyDynamicPws.code.equals(code) && ((System.currentTimeMillis() - joyDynamicPws.createTime.getTime())/(60000) < 15)){
				
					
					 if (passWord.length() <= 5 || passWord.length() >= 13) {
						 jsonObject.put("errMsg","验证码格式错误");
					}else{
					
							 JOYUser joyUser = (JOYUser)req.getAttribute("cUser");
						 	 JOYUser userNew = new JOYUser();
							 userNew.userpwd = passWord; //setUserpwd(passWord);
							 joyUser.mobileNo = mobileNo; //setMobileNo(mobileNo);
							 joyUserService.updateRecord(userNew, joyUser);
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
	
	
	@RequestMapping(value={"usermgr/viewBaseInfo"
			,"usermgr/getCommonDestination"
			,"usermgr/checkUserState"
			,"usermgr/getBioLogicalInfo"
			,"usermgr/updateUserIdInfo"},method=RequestMethod.POST)
	public @ResponseBody JSONObject userInfo(HttpServletRequest req){
		JSONObject Reobj = new JSONObject();
		Reobj.put("result", "10001");
			try{
				 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
				 String mobileNo = (String)jsonObj.get("mobileNo");
				 JOYUser joyUser = (JOYUser)req.getAttribute("cUser");	
				 String URI = req.getRequestURI();
				 if(URI.contains("viewBaseInfo")) {
					 Reobj.put("username", joyUser.username);
					 Reobj.put("gender", joyUser.gender);
					 Reobj.put("mobileNo", joyUser.mobileNo);
					 Reobj.put("driverlicenseCertification", joyUser.authenticateDriver);
					 Reobj.put("deposit", joyUser.deposit);
					 Reobj.put("idNo",joyUser.idNo==null?"":joyUser.idNo);
					 Reobj.put("idName",joyUser.idName==null?"":joyUser.idName);
					 Reobj.put("result", "10000");
				 } else if (URI.contains("getCommonDestination")){
					 JSONObject homeAddr = new JSONObject();
					 JSONObject corpAddr = new JSONObject();
					 
					 homeAddr.put("name", joyUser.homeAddr);
					 homeAddr.put("latitude", joyUser.homeLatitude);
					 homeAddr.put("longitude", joyUser.homeLongitude);
					 corpAddr.put("name", joyUser.corpAddr);
					 corpAddr.put("latitude", joyUser.corpLatitude);
					 corpAddr.put("longitude", joyUser.corpLongitude);
					 Reobj.put("home", homeAddr);
					 Reobj.put("corp", corpAddr);
					 Reobj.put("result", "10000");
				 } else if (URI.contains("checkUserState")){
				
					 Reobj.put("mobileAuthState", 1);
					 Reobj.put("id5PassFlag",joyUser.id5PassFlag);
					 Reobj.put("id5AuthState", joyUser.authenticateId);
					 Reobj.put("driverLicAuthState", joyUser.authenticateDriver);
					 BigDecimal deposit = joyUser.deposit;
					 if(deposit!=null && deposit.doubleValue() >=0.01) {
						 Reobj.put("depositState", 1);
					 } else {
						 Reobj.put("depositState", 0);
					 }
					 Reobj.put("result", "10000");
			     } else if(URI.contains("getBioLogicalInfo")) {
                     Reobj.put("face_info", joyUser.face_info);
					 Reobj.put("voice_info", joyUser.voice_info);
					 Reobj.put("result", "10000");
				 } else if(URI.contains("updateUserIdInfo")) {
					 String idNo = String.valueOf(jsonObj.get("idNo"));
					 String idName = String.valueOf(jsonObj.get("idName"));
					 if(idNo!=null && idName!=null) {
						 Map<String,String> id5Info = Id5Utils.Id5Check(idName,idNo);
						 JOYUser toModUser = new JOYUser();
						 toModUser.mobileNo = mobileNo;
						 if(id5Info!=null) {
							 toModUser.idNo = idNo;
							 toModUser.idName = idName;
							 toModUser.gender = String.valueOf(id5Info.get("sex"));
							 toModUser.id5PassFlag = JOYUser.auth_state_ok;
							 Reobj.put("result","10000");
						 } else {
							 toModUser.id5PassFlag = JOYUser.auth_state_pending;
						 }
						 joyUserService.updateRecord(toModUser, joyUser);
					 }
				 }
			}catch(Exception e){
				e.printStackTrace();
				Reobj.put("result","10001");
				Reobj.put("errMsg","发生未知错误");
			}
		
		return Reobj;
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
					JOYUser userNew = new JOYUser();
					userNew.face_info = face_info;
					userNew.voice_info = voice_info;
					joyUser.mobileNo = mobileNo;
					joyUserService.updateRecord(userNew, joyUser);
					jsonObject.put("result", "10000");
				} else if (URI.contains("updateInfo")) {
						String username = (String) jsonObj.get("username");
						String gender = (String) jsonObj.get("gender");
						JOYUser joyUser = (JOYUser) req.getAttribute("cUser");
					    JOYUser userNew = new JOYUser();
						String[] settingProps = {"username", "gender"};
					    if(username!=null)
							userNew.username = username;
					    if(gender!=null)
							userNew.gender = gender;

						joyUserService.updateRecord(userNew, joyUser);
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
		logger.trace(user.gender);
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
				 JOYPayReqInfo wxpayInfo = new JOYPayReqInfo();
				JOYPayReqInfo zhifubaoPayInfo = new JOYPayReqInfo();
				 wxpayInfo.mobileNo = (mobileNo);
				wxpayInfo.type = JOYPayReqInfo.type_wx;
				zhifubaoPayInfo.type = JOYPayReqInfo.type_zhifubao;
				zhifubaoPayInfo.mobileNo = mobileNo;
				 String wx_trade_no = "depositRecharge" + String.valueOf(System.currentTimeMillis());
				 wxpayInfo.out_trade_no = (wx_trade_no);
				zhifubaoPayInfo.out_trade_no = currTimeStr+balance.toString().replace(".", "-");
				 wxpayInfo.totalFee = (Double.valueOf(balance));
				zhifubaoPayInfo.totalFee = wxpayInfo.totalFee;
				 String wx_code = WeChatPayUtil.genePayStr(String.valueOf(Double.valueOf(balance*100).longValue()),wx_trade_no);
				 joyPayReqInfoService.insertRecord(wxpayInfo);
				 joyPayReqInfoService.insertRecord(zhifubaoPayInfo);
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
				JOYUser userNew = new JOYUser();
				JSONObject homeAddr = (JSONObject)jsonObj.get("home");
				JSONObject corpAddr = (JSONObject)jsonObj.get("corp");
				if(homeAddr!=null){
					userNew.homeAddr = (String)homeAddr.get("name");
					userNew.homeLatitude = BigDecimal.valueOf(Double.valueOf(String.valueOf(homeAddr.get("latitude"))));
					userNew.homeLongitude = (BigDecimal.valueOf(Double.valueOf(String.valueOf(homeAddr.get("longitude")))));
				}
				if(corpAddr!=null) {
					userNew.corpAddr = ((String)corpAddr.get("name"));
					userNew.corpLatitude = (BigDecimal.valueOf(Double.valueOf(String.valueOf(corpAddr.get("latitude")))));
					userNew.corpLongitude = (BigDecimal.valueOf(Double.valueOf(String.valueOf(corpAddr.get("longitude")))));
					
				}
				
				joyUserService.updateRecord(userNew,user);
				Reobj.put("result","10000");
			}catch(Exception e){
				e.printStackTrace();
			}
		
		
		return Reobj;
	}
	
	
	
}
