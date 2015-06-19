package com.joymove.amqp.handler.impl;

import java.util.concurrent.locks.ReentrantLock;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.futuremove.cacheServer.concurrent.CarOpLock;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymove.amqp.handler.EventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("ReportClearCodeHandler")
public class ClearCodeHandler  implements EventHandler {

	@Resource(name = "carService")
	private CarService cacheCarService;
	final static Logger logger = LoggerFactory.getLogger(ClearCodeHandler.class);


	public int getEventType() {
		return 5;
	}

	@Override
	public boolean handleData(JSONObject json) {
		boolean error=true;
		ReentrantLock opLock = null;
		try {
			logger.debug("get the clear  code report from clouemove");
			String vinNum = String.valueOf(json.get("vin"));
			opLock = CarOpLock.getCarLock(vinNum);
			opLock.lock();//>>============================
			Car car = new Car();
			car.setVinNum(vinNum);
			car = cacheCarService.getByVinNum(vinNum);
			Long result = Long.parseLong(String.valueOf(json.get("result")));
			if (car.getState() == Car.state_wait_clearcode) {
				logger.debug("get the clear code result ");
				if(result==1) {
					logger.debug("clear code  success ");
					cacheCarService.updateCarStateWaitPowerOff(car);
					cacheCarService.sendPowerOff(car.getVinNum());
				} else {
					logger.debug("clear code failed ");
					//try again
					cacheCarService.sendClearCode(car.getVinNum());
				}
			}else {
				logger.debug("the car in state "+car.getState()+" so we do not do anything");
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
