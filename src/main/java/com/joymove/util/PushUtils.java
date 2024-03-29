package com.joymove.util;
import javapns.devices.Device;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.Push;
import javapns.devices.Device;
import javapns.notification.PushedNotification;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushNotificationManager;
import javapns.notification.AppleNotificationServerBasicImpl;


import org.json.simple.*;
import org.json.simple.parser.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushUtils {

	final static Logger logger = LoggerFactory.getLogger(PushUtils.class);

	public static boolean iPhonePush(String token,JSONObject args){
		try {
	    	 args.put("aps",new JSONObject());
	    	 PushNotificationManager pushManager = new PushNotificationManager();
	    	 PushNotificationPayload payLoad = PushNotificationPayload.fromJSON(args.toJSONString());
	    	 AppleNotificationServerBasicImpl app =  new AppleNotificationServerBasicImpl("aps_development.p12", "314159", false);
	    	 logger.trace(String.valueOf(app.getNotificationServerHost()));
	    	 logger.trace(String.valueOf(app.getNotificationServerPort()));
	    	 logger.trace("token is "+token);
	    	 pushManager.initializeConnection(app);
	    	 pushManager.addDevice("iPhone", token);
	    	 Device client =  pushManager.getDevice("iPhone");
	    	 //payLoad.addBadge(3);
	    	 //payLoad.addSound("test");
             PushedNotification notifition = pushManager.sendNotification(client, payLoad);
             return notifition.isSuccessful();
	     } catch (Exception e) {
	    	 logger.error(e.getStackTrace().toString());
	    	 return false;
	     }
	  }

}
