package com.joymove.amqp.handler.impl;

import java.util.concurrent.locks.ReentrantLock;

import com.futuremove.cacheServer.concurrent.CarOpLock;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymove.amqp.handler.EventHandler;
import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
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
		ReentrantLock opLock = null;

		try {
			logger.info("update car status  handler called !!");
			Car car = new Car();
			car.setLatitude(Double.parseDouble(String.valueOf(json.get("latitude"))));
			car.setLongitude(Double.parseDouble(String.valueOf(json.get("longitude"))));
			car.setVinNum(String.valueOf(json.get("vin")));
			opLock = CarOpLock.getCarLock(car.getVinNum());
			opLock.lock();//>>============================
			Car cacheCar = carService.getByVinNum(car.getVinNum());
			logger.info("call carservice to update car loc info !!");

			if(cacheCar.getState()==Car.state_wait_clearcode) {
				logger.info("car inside wait cCCCCClear code state, send the cmd again");
                carService.sendClearCode(car.getVinNum());
			} else if(cacheCar.getState()==Car.state_wait_poweroff) {
				logger.info("car inside wait pPPPPPower off code state, send the cmd again");
				carService.sendPowerOff(car.getVinNum());
			} else if(cacheCar.getState()==Car.state_wait_lock) {
				logger.info("car inside wait LLLLock state, send the cmd again");
				carService.sendLock(car.getVinNum());
			}
			carService.updateCarPosition(car);
		} catch(Exception e){
			error = true;
			logger.error(e.toString());
			logger.error(e.getStackTrace().toString());
		} finally {
			if(opLock!=null && opLock.getHoldCount()>0)
				opLock.unlock();
		}
		return error;
	}

}
