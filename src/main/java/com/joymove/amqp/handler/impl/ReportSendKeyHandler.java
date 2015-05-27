package com.joymove.amqp.handler.impl;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.joymove.entity.JOYNCar;
import com.joymove.service.JOYNCarService;
import com.joymove.service.JOYNOrderService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymove.amqp.handler.EventHandler;
import com.futuremove.cacheServer.utils.ConfigUtils;
import com.futuremove.cacheServer.utils.HttpPostUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("ReportSendKeyHandler")
public class ReportSendKeyHandler  implements EventHandler {

	final static Logger logger = LoggerFactory.getLogger(ReportSendKeyHandler.class);
	public static int eventType = 2;

	public int getEventType() {
		return 2;
	}

	@Resource(name = "carService")
	private CarService cacheCarService;
	@Resource(name = "JOYNCarService")
	private JOYNCarService joyNCarService;



	@Override
	public boolean handleData(JSONObject json) {
		boolean error=true;
		try {
				
				logger.info("report send key   handler called !!");
		 		Long result = Long.parseLong(String.valueOf(json.get("result")));
		 		if(result > 0 ) {
		 			JOYNCar car =new JOYNCar();
					car.registerState = (1);
					car.vinNum = String.valueOf(json.get("vin"));
					logger.debug("update car's register state ");
					joyNCarService.updateCarRegisterState(car);
					//add a new car entity to mongo
					logger.debug("now ,save the new car info into mongo");
					Car cacheCar = cacheCarService.getByVinNum(car.vinNum);
					if(cacheCar==null) {
						cacheCar = new Car();
						cacheCar.setVinNum(car.vinNum);
						cacheCar.setLongitude(0.0);
						cacheCar.setLatitude(0.0);
						cacheCar.setState(Car.state_free);
						cacheCarService.save(cacheCar);
						logger.debug("now , register car ack ok");
					}
		 			return false;
		 		} else {
		 			logger.info("send key failed  !!");
		 		}
		} catch(Exception e){
			error = true;
			logger.info("send key exception: "+e.toString());
		}
		return error;
	}

}
