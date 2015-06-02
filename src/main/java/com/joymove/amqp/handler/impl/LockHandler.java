package com.joymove.amqp.handler.impl;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.joymove.amqp.handler.EventHandler;
import com.joymove.entity.JOYCar;
import com.joymove.entity.JOYOrder;
import com.joymove.service.JOYNOrderService;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by qurj on 15/6/2.
 */

@Component("LockHandler")
public class LockHandler implements EventHandler {

    @Resource(name = "carService")
    private CarService cacheCarService;
    @Resource(name = "JOYNOrderService")
    private JOYNOrderService joyNOrderService;


    final static Logger logger = LoggerFactory.getLogger(LockHandler.class);

    public int getEventType() {
        return 9;
    }

    public boolean handleData(JSONObject json) {
        boolean error=true;
        try {
            logger.debug("get the send code report from clouemove");

            String vinNum = String.valueOf(json.get("vin"));
            Car car = new Car();
            car.setVinNum(vinNum);
            car = cacheCarService.getByVinNum(vinNum);
            if (Integer.parseInt(String.valueOf(json.get("result"))) == 1) {
                if (car.getState() == Car.state_wait_lock) {
                    cacheCarService.updateCarStateFree(car);
                }
            } else {
                cacheCarService.sendLock(car.getVinNum());
            }
            return false;
        } catch(Exception e){
            error = true;
        }
        return error;
    }

}
