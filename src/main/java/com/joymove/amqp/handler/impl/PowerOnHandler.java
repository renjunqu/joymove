package com.joymove.amqp.handler.impl;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by qurj on 15/6/2.
 */
@Component("PowerOnHandler")
public class PowerOnHandler  implements EventHandler {

    @Resource(name = "carService")
    private CarService cacheCarService;
    @Resource(name = "JOYNOrderService")
    private JOYNOrderService joyNOrderService;
    @Resource(name = "JOYNCarService")
    private JOYNCarService joynCarService;
    final static Logger logger = LoggerFactory.getLogger(PowerOnHandler.class);

    public int getEventType() {
        return 10;
    }

    public boolean handleData(JSONObject json) {
        boolean error=true;
        ReentrantLock opLock = null;
        try {
            logger.debug("get the send power on report from clouemove");

            String vinNum = String.valueOf(json.get("vin"));
            opLock = CarOpLock.getCarLock(vinNum);
            Map<String,Object> likeCondition = new HashMap<String,Object>();
            opLock.lock();//>>============================
            Car car = new Car();
            car.setVinNum(vinNum);
            car = cacheCarService.getByVinNum(vinNum);
            Long result = Long.parseLong(String.valueOf(json.get("result")));
            if(car.getState()==Car.state_wait_poweron) {
                logger.error("now we at state_wait_power on, for "+car.getVinNum());
                if(result==1L) {
                    logger.error("the cloumove tell us it is ok");
                    JOYNCar ncarFilter = new JOYNCar();
                    JOYOrder order = new JOYOrder();
                    order.mobileNo = (car.getOwner());
                    order.carVinNum = (car.getVinNum());
                    order.startLongitude = car.getLongitude();
                    order.startLatitude = car.getLatitude();
                    ncarFilter.vinNum = car.getVinNum();
                    List<JOYNCar> ncars = joynCarService.getNeededList(ncarFilter);
                    JOYNCar ncar = ncars.get(0);
                     order.ifBlueTeeth = ncar.ifBlueTeeth;
                    joyNOrderService.insertRecord(order);
                    cacheCarService.updateCarStateBusy(car);

                } else {
                    logger.error("the cloudmove tell us it it failed");
                    //try again
                    cacheCarService.sendPowerOn(car.getVinNum());
                }
            } else {
                logger.debug("the car in state "+car.getState()+" so we do not do anything");
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
