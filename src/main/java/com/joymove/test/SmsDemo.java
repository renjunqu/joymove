package com.joymove.test;

import java.util.HashMap;

import java.util.Map;

import com.joymove.util.HttpClientHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.joymove.util.*;
public class SmsDemo {

	
	public static void sms_api1() {

		Map<String, String> para = new HashMap<String, String>();

		
		para.put("mob", "18500217642");

		
		para.put("uid", "3U63Wg4Eb4lK");

		
		para.put("pas", "42v7cag2");

		para.put("type", "json");

		String msg = "$$03315914935261ULK00000000000000";
		para.put("con", msg);
		
		try {
			System.out.println(HttpClientHelper.convertStreamToString(
					HttpClientHelper.post(
							"http://api.weimi.cc/2/sms/send.html", para),
					"UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sms_api2() {

		Map<String, String> para = new HashMap<String, String>();

		
		para.put("mob", "18500217642");

		
		para.put("uid", "3U63Wg4Eb4lK");

		
		para.put("pas", "42v7cag2");

		
		para.put("type", "json");

		
		para.put("cid", "b7EDnQU8L951");
		

	
		para.put("p1", "610912");
		SimpleDateFormat   dateFormatter   =   new   SimpleDateFormat   ("yyyy-MM-dd   HH:mm:ss     ");   
		String currDate = dateFormatter.format(new   Date(System.currentTimeMillis()));
        para.put("p2",currDate);

		try {
			System.out.println(HttpClientHelper.convertStreamToString(
					HttpClientHelper.get("http://api.weimi.cc/2/sms/send.html",
							para), "UTF-8"));

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] a) {

		
		sms_api1();
		

	}

}
