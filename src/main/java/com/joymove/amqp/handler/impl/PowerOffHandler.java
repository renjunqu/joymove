package com.joymove.amqp.handler.impl;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.joymove.amqp.handler.EventHandler;
import com.futuremove.cacheServer.concurrent.CarOpLock;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        try {
            logger.debug("get the power off  report from clouemove");
            String vinNum = String.valueOf(json.get("vin"));
            opLock = CarOpLock.getCarLock(vinNum);
            opLock.lock();//>>============================
            Car car = new Car();
            car.setVinNum(vinNum);
            car = cacheCarService.getByVinNum(vinNum);
            Long result = Long.parseLong(String.valueOf(json.get("result")));
            if (car.getState() == Car.state_wait_poweroff) {
                logger.debug("get the power off result");
                if(result==1) {
                         logger.debug("power off ok");
                        cacheCarService.updateCarStateWaitLock(car);
                        cacheCarService.sendLock(car.getVinNum());
                    } else {
                    logger.debug("power off failed ,try again");
                    //try again
                    cacheCarService.sendPowerOff(car.getVinNum());
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
