package com.joymove.test;
import javapns.Push;
import javapns.devices.Device;
import javapns.notification.PushedNotification;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushNotificationManager;
import javapns.notification.AppleNotificationServerBasicImpl;
import org.json.simple.*;
import org.json.simple.parser.*;

public class pushTester {

	 private static String deviceId = "ff023e3f479113feee62e6bd564038d3b5b6db4dfe36fe65a4de1b82bb358a7d";
	
	 public static void main(String[] args) {
	     try {
	    	 JSONObject test = new JSONObject();
	    	 test.put("result", 0);
	    	 test.put("aps",new JSONObject());
	    	 
	    	 PushNotificationManager pushManager = new PushNotificationManager();
	    	 PushNotificationPayload payLoad = PushNotificationPayload.fromJSON(test.toJSONString());
	    	 payLoad.addBadge(2);
	    	 payLoad.addSound("test");
	    	 AppleNotificationServerBasicImpl app =  new AppleNotificationServerBasicImpl("aps_development.p12", "314159", false);
	    	// logger.trace(app.getNotificationServerHost());
	    	// logger.trace(app.getNotificationServerPort());
	    	 pushManager.initializeConnection(app);
	    	 pushManager.addDevice("iPhone", deviceId);
	    	 Device client =  pushManager.getDevice("iPhone");
             PushedNotification notifition = pushManager.sendNotification(client, payLoad);  
             boolean result = notifition.isSuccessful();
             //logger.trace(result);
	     } catch (Exception e) {
	    	// logger.trace(e);
	     }
	}
}
