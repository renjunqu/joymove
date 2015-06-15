package com.joymove.amqp.handler.impl;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.joymove.amqp.handler.EventHandler;
import com.futuremove.cacheServer.concurrent.CarOpLock;
import com.joymove.entity.JOYNCar;
import com.joymove.entity.JOYOrder;
import com.joymove.service.JOYNCarService;
import com.joymove.service.JOYNOrderService;
import com.joymove.service.JOYOrderService;
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

    @Resource(name = "carService")
    private CarService cacheCarService;
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
        try {
            logger.debug("get the lock report from clouemove");
            String vinNum = String.valueOf(json.get("vin"));
            opLock = CarOpLock.getCarLock(vinNum);
            opLock.lock();//>>============================
            Car car = new Car();
            car.setVinNum(vinNum);
            car = cacheCarService.getByVinNum(vinNum);
            Long result = Long.parseLong(String.valueOf(json.get("result")));
            if (car.getState() == Car.state_wait_lock) {
                logger.debug("get the lock result for state_wait_lock with "+car.getVinNum());
                if(result==1) {
                    logger.debug("lock success ");
                    //首先停止订单
                    JOYOrder orderFilter = new JOYOrder();
                    JOYOrder orderValue = new JOYOrder();

                    orderFilter.carVinNum = car.getVinNum();
                    orderFilter.mobileNo = car.getOwner();
                    orderFilter.state = JOYOrder.state_busy;


                    orderValue.state = JOYOrder.state_wait_pay;
                    orderValue.stopLongitude = car.getLongitude();
                    orderValue.stopLatitude = car.getLatitude();
                    orderValue.stopTime = new Date(System.currentTimeMillis());
                    joyNOrderService.updateRecord(orderValue,orderFilter);
                    car.setOwner("");
                    JOYNCar ncarFilter = new JOYNCar();
                    JOYNCar ncarValue = new JOYNCar();
                    ncarFilter.vinNum = vinNum;
                    ncarValue.lockState = 1;
                    joyNCarService.updateRecord(ncarValue, ncarFilter);
                    cacheCarService.updateCarStateFree(car);
                } else {
                    logger.debug("lock failed ");
                    //try again
                    cacheCarService.sendLock(car.getVinNum());
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
