package com.joymove.amqp.handler.impl;

import com.futuremove.cacheServer.entity.CarDynProps;
import com.futuremove.cacheServer.service.CarDynPropsService;
import com.joymove.entity.JOYNCar;
import com.joymove.service.JOYNCarService;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymove.amqp.handler.EventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("ReportSendKeyHandler")
public class SendKeyHandler  implements EventHandler {

	final static Logger logger = LoggerFactory.getLogger(SendKeyHandler.class);
	public static int eventType = 2;

	public int getEventType() {
		return 2;
	}

	@Resource(name = "CarDynPropsService")
	private CarDynPropsService carPropsService;
	@Resource(name = "JOYNCarService")
	private JOYNCarService joyNCarService;




	@Override
	public boolean handleData(JSONObject json) {
		boolean error=true;
		try {
			CarDynProps carPropsFilter = new CarDynProps();
			CarDynProps  carProps  = new CarDynProps();
				
				logger.info("report send key   handler called !!");
		 		Long result = Long.parseLong(String.valueOf(json.get("result")));
		 		if(result > 0 ) {
		 			JOYNCar carFilter =new JOYNCar();
					JOYNCar carNew = new JOYNCar();
					carNew.registerState = 1;
					carFilter.vinNum = String.valueOf(json.get("vin"));
					logger.debug("update car's register state ");
					joyNCarService.updateRecord(carNew, carFilter);
					//add a new car entity to mongo
					logger.debug("now ,save the new car info into mongo");
					carPropsFilter.vinNum = carFilter.vinNum;
					Long carPropsCount = carPropsService.count(carPropsFilter);
					if(carPropsCount==0) {
						carPropsService.insert(carPropsFilter);
						logger.debug("now , register car ack ok");
					}
		 			return false;
		 		} else {
		 			logger.info("send key failed  !!");
		 		}
		} catch(Exception e){
			error = true;
			logger.info("send key exception: ",e);
		}
		return error;
	}

}
