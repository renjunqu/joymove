package com.joymove.test;

import java.util.HashMap;
import java.util.Map;

import com.joymove.util.HttpClientHelper;

public class AccountDemo {

	
	public static void account_api() {

		Map<String, String> para = new HashMap<String, String>();

		
		para.put("uid", "<enter your UID>");

		para.put("pas", "<enter your UID Pass>");

		
		para.put("type", "json");

		try {
			System.out
					.println(HttpClientHelper.convertStreamToString(
							HttpClientHelper
									.get("http://api.weimi.cc/2/account/balance.html",
											para), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] a) {

		
		account_api();

	}
}
