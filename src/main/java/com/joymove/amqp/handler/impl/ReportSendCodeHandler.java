package com.joymove.amqp.handler.impl;

import java.util.Hashtable;
import java.util.Map;

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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component("ReportSendCodeHandler")
public class ReportSendCodeHandler implements EventHandler {

	@Resource(name = "carService")
	private CarService      cacheCarService;
	@Resource(name = "JOYNOrderService")
	private JOYNOrderService joyNOrderService;

	public int getEventType() {
		return 4;
	}


	final static Logger logger = LoggerFactory.getLogger(ReportSendCodeHandler.class);


	/********* busi proc *******/

	@Override
	public boolean handleData(JSONObject json) {
		boolean error=true;
		try {
			logger.debug("get the send code report from clouemove");

			String vinNum = String.valueOf(json.get("vin"));
			Car car = new Car();
			car.setVinNum(vinNum);
			car = cacheCarService.getByVinNum(vinNum);
			if(car.getState()==Car.state_wait_code) {

				JOYOrder order = new JOYOrder();
				order.mobileNo = (car.getOwner());
				order.carVinNum = (car.getVinNum());
				order.startLongitude = car.getLatitude();
				order.startLatitude = car.getLongitude();
				order.ifBlueTeeth = JOYCar.HAS_BT;
				joyNOrderService.insertNOrder(order);
				cacheCarService.updateCarStateBusy(car);
			}
			return false;
		} catch(Exception e){
			error = true;
		}
		return error;
	}

}
