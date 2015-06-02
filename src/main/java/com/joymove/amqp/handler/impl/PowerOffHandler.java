package com.joymove.amqp.handler.impl;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.joymove.amqp.handler.EventHandler;
import com.joymove.concurrent.CarOpLock;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by qurj on 15/6/2.
 */
@Component("PowerOffHandler")
public class PowerOffHandler implements EventHandler {

    @Resource(name = "carService")
    private CarService cacheCarService;

    final static Logger logger = LoggerFactory.getLogger(PowerOffHandler.class);



    public int getEventType() {
        return 11;
    }

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
            if (car.getState() == Car.state_wait_poweroff) {
                if(result==1) {
                    cacheCarService.updateCarStateWaitLock(car);
                    while (cacheCarService.sendLock(car.getVinNum()) == false) {
                        Thread.sleep(tryTimes * 20);
                    }
                } else {
                    //重发下发关火命令
                    Thread.sleep(20);
                    while (cacheCarService.sendPowerOff(car.getVinNum()) == false) {
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
