package com.joymove.amqp.handler.impl;

import java.util.concurrent.locks.ReentrantLock;

import com.futuremove.cacheServer.concurrent.CarOpLock;
import com.futuremove.cacheServer.entity.CarDynProps;
import com.futuremove.cacheServer.service.CarDynPropsService;
import com.joymove.service.JOYNOrderService;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymove.amqp.handler.EventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component("SendCodeHandler")
public class SendCodeHandler implements EventHandler {

	@Resource(name = "CarDynPropsService")
	private CarDynPropsService carPropsService;
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
			CarDynProps carPropsFilter = new CarDynProps();
			CarDynProps  carProps  = new CarDynProps();

			String vinNum = String.valueOf(json.get("vin"));
			logger.debug("get the send code report from clouemove for "+vinNum);

			opLock = CarOpLock.getCarLock(vinNum);
			opLock.lock();//>>============================
			carPropsFilter.vinNum = vinNum;
			carProps.fromDocument(carPropsService.find(carPropsFilter).first());
			Long result = Long.parseLong(String.valueOf(json.get("result")));
				if (carProps.state == CarDynProps.state_wait_sendcode) {
					logger.debug("we already in wait sendcode state ");
					if(result==1) {
						logger.debug("the cloudmove tell us it is good");
						carProps.clearProperties();
						carProps.state = CarDynProps.state_wait_poweron;
						carPropsService.update(carPropsFilter,carProps);
						carPropsService.sendPowerOn(vinNum);
					} else {
						logger.debug("the cloudmove tell us it is failed");
						//try again
						carPropsService.sendAuthCode(vinNum);
					}
				} else {
					logger.debug("the car in state "+carProps.state+" so we do not do anything");
				}
				error = false;
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
