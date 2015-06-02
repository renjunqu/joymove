package com.joymove.amqp.handler.impl;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.joymove.concurrent.CarOpLock;
import com.joymove.entity.JOYCar;
import com.joymove.entity.JOYOrder;
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
import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.futuremove.cacheServer.utils.ConfigUtils;
import com.futuremove.cacheServer.utils.HttpPostUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
		int tryTimes = 0;
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
						while (cacheCarService.sendPowerOn(car.getVinNum()) == false) {
							Thread.sleep(tryTimes * 20);
						}
					} else {
						Thread.sleep(20);
						//重发下发授权码
						while (cacheCarService.sendAuthCode(car.getVinNum()) == false) {
							Thread.sleep(tryTimes++ * 20);
						}
					}
				}
				error = false;

		} catch(Exception e){
			error = true;
		} finally {
			if(opLock!=null && opLock.getHoldCount()>0)
				opLock.unlock();
		}
		return error;
	}

}
