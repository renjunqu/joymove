package com.joymove.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.*;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SmsUtils {

	final static Logger logger = LoggerFactory.getLogger(SmsUtils.class);

	public static String sendRegisterCode(String code,String mobileNo) {

		Map<String, String> para = new HashMap<String, String>();

		para.put("mob", mobileNo);

		para.put("uid", "3U63Wg4Eb4lK");

		para.put("pas", "42v7cag2");

		para.put("type", "json");

		para.put("cid", "b7EDnQU8L951");
		para.put("p1", code);
		SimpleDateFormat   dateFormatter   =   new   SimpleDateFormat   ("yyyy-MM-dd   HH:mm:ss     ");   
		String currDate = dateFormatter.format(new   Date(System.currentTimeMillis()));
        para.put("p2",currDate);

		try {
			logger.error("start to call wei mi api");
			InputStream is = HttpClientHelper.get("http://api.weimi.cc/2/sms/send.html",
					para);
			String wemiRe =  HttpClientHelper.convertStreamToString(
					is, "UTF-8");
			logger.error("call  weimi result is "+wemiRe);
			return wemiRe;
		} catch (Exception e) {
			logger.error("call wemi result exception");
			e.printStackTrace();
		}
		return "{\"error\":1}";
	}
	/*
	public static void sendRegisterCode2(String code,String mobileNo) {

		String nr = "您的短信验证码为 :" +String.valueOf(code)+"请在1分钟之内完成注册。 ";
		SimpleDateFormat   dateFormatter   =   new   SimpleDateFormat   ("yyyy-MM-dd   HH:mm:ss     ");   
		String currDate = dateFormatter.format(new   Date(System.currentTimeMillis()));
        nr += currDate;

		try {
			
			sendBikeCmd(nr,mobileNo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void  sendBikeCmd(String nr,String number){
		String postUrl = "http://www.810086.com.cn/jk.aspx";
		HttpPost post = new HttpPost(postUrl);
		post.getParams().setParameter("http.protocol.content-charset",HTTP.UTF_8); 
		//post  
       
		String content = "zh=carlfan&mm=Passw0rd&hm="+number+"&nr="+nr+"&sms_type=46";
		System.out.println(content);
		StringEntity postingString;
		try {
			
		        
			postingString = new StringEntity(content,"UTF-8");
		//	postingString.setContentEncoding("utf8");
			post.setEntity(postingString);
			post.setHeader("Content-type", "application/x-www-form-urlencoded");
			
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpResponse  response = httpclient.execute(post);
			System.out.println(response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	*/
	
	public static void main(String[] args) {
		System.out.println(SmsUtils.sendRegisterCode("1112", "18500217642"));
	/*	
		for(int i =45;i<50;i++) {
			SmsUtils.sendBikeCmd("zh=carlfan&mm=Passw0rd&hm=18500217642&nr=qurenjunqurenjun&sms_type="+i);	
		}
		*/
		

	}
}
