package com.joymove.amqp.handler.impl;

import java.util.concurrent.locks.ReentrantLock;

import com.futuremove.cacheServer.concurrent.CarOpLock;
import com.futuremove.cacheServer.entity.CarDynProps;
import com.futuremove.cacheServer.entity.CarLocation;
import com.futuremove.cacheServer.service.CarDynPropsService;
import com.futuremove.cacheServer.utils.CoordinatesUtil;
import com.futuremove.cacheServer.utils.Gps;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymove.amqp.handler.EventHandler;
import com.futuremove.cacheServer.entity.Car;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("UpdateStatusHandler")
public class UpdateStatusHandler  implements EventHandler {

	final static Logger logger = LoggerFactory.getLogger(UpdateStatusHandler.class);

	@Resource(name = "CarDynPropsService")
	private CarDynPropsService carPropsService;
	


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
		ReentrantLock opLock = null;

		try {
			logger.info("update car status  handler called !!");
			CarDynProps carPropsFilter = new CarDynProps();
			CarDynProps  carProps  = new CarDynProps();

			carPropsFilter.vinNum = String.valueOf(json.get("vin"));
			opLock = CarOpLock.getCarLock(carPropsFilter.vinNum);
			opLock.lock();//>>============================
			carProps.fromDocument(carPropsService.find(carPropsFilter).first());
			logger.info("call carservice to update car loc info !!");

			if(carProps.state==Car.state_wait_clearcode) {
				logger.info("car inside wait clear code state, send the cmd again");
                carPropsService.sendClearCode(carPropsFilter.vinNum);
			} else if(carProps.state==Car.state_wait_poweroff) {
				logger.info("car inside wait power off code state, send the cmd again");
				carPropsService.sendPowerOff(carPropsFilter.vinNum);
			} else if(carProps.state==Car.state_wait_lock) {
				logger.info("car inside wait Lock state, send the cmd again");
				carPropsService.sendLock(carPropsFilter.vinNum);
			}
			carProps.location = new CarLocation();
			Gps gps = CoordinatesUtil.gcj02_To_Gps84(
					Double.parseDouble(String.valueOf(json.get("latitude"))),
					Double.parseDouble(String.valueOf(json.get("longitude")))
			);

			carProps.location.coordinates.set(1,gps.getWgLat());
			carProps.location.coordinates.set(0,gps.getWgLon());
			carPropsService.update(carPropsFilter,carProps);
		} catch(Exception e){
			error = true;
			logger.error("exception:",e);
		} finally {
			if(opLock!=null && opLock.getHoldCount()>0)
				opLock.unlock();
		}
		return error;
	}

}
