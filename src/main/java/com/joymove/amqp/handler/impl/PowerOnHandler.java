package com.joymove.amqp.handler.impl;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.joymove.amqp.handler.EventHandler;
import com.joymove.concurrent.CarOpLock;
import com.joymove.entity.JOYCar;
import com.joymove.entity.JOYOrder;
import com.joymove.service.JOYNOrderService;
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
@Component("PowerOnHandler")
public class PowerOnHandler  implements EventHandler {

    @Resource(name = "carService")
    private CarService cacheCarService;
    @Resource(name = "JOYNOrderService")
    private JOYNOrderService joyNOrderService;

    final static Logger logger = LoggerFactory.getLogger(PowerOnHandler.class);

    public int getEventType() {
        return 10;
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
            if(car.getState()==Car.state_wait_poweron) {
                if(result==1L) {
                    JOYOrder order = new JOYOrder();
                    order.mobileNo = (car.getOwner());
                    order.carVinNum = (car.getVinNum());
                    order.startLongitude = car.getLongitude();
                    order.startLatitude = car.getLatitude();
                    order.ifBlueTeeth = JOYCar.HAS_BT;
                    joyNOrderService.insertNOrder(order);
                    cacheCarService.updateCarStateBusy(car);
                } else {
                    Thread.sleep(20);
                    while (cacheCarService.sendPowerOn(car.getVinNum()) == false) {
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
