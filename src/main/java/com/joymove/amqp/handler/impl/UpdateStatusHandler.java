package com.joymove.amqp.handler.impl;

import java.util.Map;

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
import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.futuremove.cacheServer.utils.ConfigUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("UpdateStatusHandler")
public class UpdateStatusHandler  implements EventHandler {

	final static Logger logger = LoggerFactory.getLogger(UpdateStatusHandler.class);

	@Resource(name = "carService")
	private  CarService carService;
	


	public int getEventType() {
		return 3;
	}

	


	

	public UpdateStatusHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean handleData(JSONObject json) {
		boolean error=true;
		try {
			
			logger.info("update car status  handler called !!");
			Car car = new Car();
			car.setLatitude(Double.parseDouble(String.valueOf(json.get("latitude"))));
			car.setLongitude(Double.parseDouble(String.valueOf(json.get("longitude"))));
			car.setVinNum(String.valueOf(json.get("vin")));
			logger.info("call carservice to update car loc info !!");
			carService.updateCarPosition(car);
			
			
		} catch(Exception e){
			error = true;
			logger.error(e.toString());
		}
		return error;
	}

}
