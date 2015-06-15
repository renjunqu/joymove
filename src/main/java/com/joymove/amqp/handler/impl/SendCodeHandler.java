package com.joymove.amqp.handler.impl;

import java.util.concurrent.locks.ReentrantLock;

import com.futuremove.cacheServer.concurrent.CarOpLock;
import com.joymove.service.JOYNOrderService;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymove.amqp.handler.EventHandler;
import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component("ReportSendCodeHandler")
public class SendCodeHandler implements EventHandler {

	@Resource(name = "carService")
	private CarService      cacheCarService;
	@Resource(name = "JOYNOrderService")
	private JOYNOrderService joyNOrderService;

	public int getEventType() {
		return 4;
	}


	final static Logger logger = LoggerFactory.getLogger(SendCodeHandler.class);


	/********* busi proc *******/

	@Override
	public boolean handleData(JSONObject json) {
		boolean error=true;
		ReentrantLock opLock = null;

		try {
			logger.debug("get the send code report from clouemove");
			String vinNum = String.valueOf(json.get("vin"));
			opLock = CarOpLock.getCarLock(vinNum);
			opLock.lock();//>>============================
			Car car = new Car();
			car.setVinNum(vinNum);
			car = cacheCarService.getByVinNum(vinNum);
			Long result = Long.parseLong(String.valueOf(json.get("result")));
				if (car.getState() == Car.state_wait_sendcode) {
					if(result==1) {
						cacheCarService.updateCarStateWaitPowerOn(car);
						cacheCarService.sendPowerOn(car.getVinNum());
					} else {
						//try again
						cacheCarService.sendAuthCode(car.getVinNum());
					}
				}
				error = false;

		} catch(Exception e){
			error = true;
			logger.error(e.getStackTrace().toString());
		} finally {
			if(opLock!=null && opLock.getHoldCount()>0)
				opLock.unlock();
		}
		return error;
	}

}
