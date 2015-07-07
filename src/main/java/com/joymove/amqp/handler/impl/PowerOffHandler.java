package com.joymove.amqp.handler.impl;

import com.futuremove.cacheServer.entity.CarDynProps;
import com.futuremove.cacheServer.service.CarDynPropsService;
import com.joymove.amqp.handler.EventHandler;
import com.futuremove.cacheServer.concurrent.CarOpLock;
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
@Component("PowerOffHandler")
public class PowerOffHandler implements EventHandler {

    @Resource(name = "CarDynPropsService")
    private CarDynPropsService carPropsService;

    final static Logger logger = LoggerFactory.getLogger(PowerOffHandler.class);



    public int getEventType() {
        return 11;
    }

    public boolean handleData(JSONObject json) {
        boolean error=true;
        ReentrantLock opLock = null;
        CarDynProps carPropsFilter = new CarDynProps();
        CarDynProps  carProps  = new CarDynProps();

        try {
            logger.debug("get the power off  report from clouemove");
            String vinNum = String.valueOf(json.get("vin"));
            opLock = CarOpLock.getCarLock(vinNum);
            opLock.lock();//>>============================
            carPropsFilter.vinNum = vinNum;
            carProps.fromDocument(carPropsService.find(carPropsFilter).first());
            Long result = Long.parseLong(String.valueOf(json.get("result")));
            if (carProps.state == CarDynProps.state_wait_poweroff) {
                logger.debug("get the power off result");
                if(result==1) {
                    logger.debug("power off ok");
                    carProps.clearProperties();
                    carProps.state=CarDynProps.state_wait_lock;
                    carProps.stateUpdateTime = new Date(System.currentTimeMillis());
                    carPropsService.update(carPropsFilter,carProps);
                    carPropsService.sendLock(vinNum);
                } else {
                    logger.debug("power off failed ,try again");
                    //try again
                    carPropsService.sendPowerOff(vinNum);
                }
            }else {
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
