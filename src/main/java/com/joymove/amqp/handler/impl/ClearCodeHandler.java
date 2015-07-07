package com.joymove.amqp.handler.impl;

import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.entity.CarDynProps;
import com.futuremove.cacheServer.service.CarDynPropsService;
import com.futuremove.cacheServer.concurrent.CarOpLock;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymove.amqp.handler.EventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("ReportClearCodeHandler")
public class ClearCodeHandler  implements EventHandler {

	@Resource(name = "CarDynPropsService")
	private CarDynPropsService carPropsService;
	final static Logger logger = LoggerFactory.getLogger(ClearCodeHandler.class);


	public int getEventType() {
		return 5;
	}

	@Override
	public boolean handleData(JSONObject json) {
		boolean error=true;
		ReentrantLock opLock = null;
		CarDynProps carPropsFilter = new CarDynProps();
		CarDynProps  carProps  = new CarDynProps();
		try {
			logger.debug("get the clear  code report from clouemove");
			String vinNum = String.valueOf(json.get("vin"));
			opLock = CarOpLock.getCarLock(vinNum);
			opLock.lock();//>>============================
			carPropsFilter.vinNum = vinNum;
			carProps.fromDocument(carPropsService.find(carPropsFilter).first());
			Long result = Long.parseLong(String.valueOf(json.get("result")));
			if (carProps.state == Car.state_wait_clearcode) {
				logger.debug("get the clear code result ");
				if(result==1) {
					logger.debug("clear code  success ");
					carProps.clearProperties();
					carProps.state = CarDynProps.state_wait_poweroff;
					carProps.stateUpdateTime = new Date(System.currentTimeMillis());
					carPropsService.update(carPropsFilter, carProps);
					carPropsService.sendPowerOff(vinNum);
				} else {
					logger.debug("clear code failed ");
					//try again
					carPropsService.sendPowerOff(vinNum);
				}
			}else {
				logger.debug("the car in state "+carProps.state+" so we do not do anything");
			}
			error = false;

		} catch(Exception e){
			error = true;
			logger.error("excpetion: ",e);
		} finally {
			if(opLock!=null && opLock.getHoldCount()>0)
				opLock.unlock();
		}
		return error;
	}

}
