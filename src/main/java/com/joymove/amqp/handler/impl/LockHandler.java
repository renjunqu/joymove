package com.joymove.amqp.handler.impl;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.entity.CarDynProps;
import com.futuremove.cacheServer.service.CarDynPropsService;
import com.joymove.amqp.handler.EventHandler;
import com.futuremove.cacheServer.concurrent.CarOpLock;
import com.joymove.entity.JOYNCar;
import com.joymove.entity.JOYOrder;
import com.joymove.service.JOYNCarService;
import com.joymove.service.JOYNOrderService;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by qurj on 15/6/2.
 */

@Component("LockHandler")
public class LockHandler implements EventHandler {

    @Resource(name = "CarDynPropsService")
    private CarDynPropsService carPropsService;
    @Resource(name = "JOYNCarService")
    private JOYNCarService joyNCarService;
    @Resource(name = "JOYNOrderService")
    private JOYNOrderService joyNOrderService;


    final static Logger logger = LoggerFactory.getLogger(LockHandler.class);

    public int getEventType() {
        return 9;
    }

    public boolean handleData(JSONObject json) {
        boolean error=true;
        ReentrantLock opLock = null;
        CarDynProps carPropsFilter = new CarDynProps();
        CarDynProps  carProps  = new CarDynProps();

        try {
            logger.debug("get the lock report from clouemove");
            String vinNum = String.valueOf(json.get("vin"));
            opLock = CarOpLock.getCarLock(vinNum);
            opLock.lock();//>>============================
            carPropsFilter.vinNum = vinNum;
            carProps.fromDocument(carPropsService.find(carPropsFilter).first());
            Long result = Long.parseLong(String.valueOf(json.get("result")));
            if (carProps.state == Car.state_wait_lock) {
                logger.debug("get the lock result for state_wait_lock with "+vinNum);
                if(result==1) {
                    logger.debug("lock success ");
                    //首先停止订单
                    JOYOrder orderFilter = new JOYOrder();
                    JOYOrder orderValue = new JOYOrder();

                    orderFilter.carVinNum = vinNum;
                    orderFilter.mobileNo = carProps.owner;
                    orderFilter.state = JOYOrder.state_busy;
                    orderValue.state = JOYOrder.state_wait_pay;
                    orderValue.stopLongitude = carProps.location.coordinates.get(1);
                    orderValue.stopLatitude = carProps.location.coordinates.get(0);
                    orderValue.stopTime = new Date(System.currentTimeMillis());
                    joyNOrderService.updateRecord(orderValue, orderFilter);
                    JOYNCar ncarFilter = new JOYNCar();
                    JOYNCar ncarValue = new JOYNCar();
                    ncarFilter.vinNum = vinNum;
                    ncarValue.lockState = 1;
                    joyNCarService.updateRecord(ncarValue, ncarFilter);
                    carProps.clearProperties();
                    carProps.state = CarDynProps.state_free;
                    carProps.owner = "";
                    carPropsService.update(carPropsFilter,carProps);
                } else {
                    logger.debug("lock failed ");
                    //try again
                    carPropsService.sendLock(vinNum);
                }
            }else {
                logger.debug("the car in state "+ carProps.state+ " so we do not do anything");
            }
            error = false;

        } catch(Exception e){
            error = true;
            logger.error("exception: ",e);
        } finally {
            if(opLock!=null && opLock.getHoldCount()>0)
                opLock.unlock();
        }
        return error;
    }

}
