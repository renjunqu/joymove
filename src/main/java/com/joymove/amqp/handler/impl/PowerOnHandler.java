package com.joymove.amqp.handler.impl;

import com.futuremove.cacheServer.entity.Car;
import com.futuremove.cacheServer.service.CarService;
import com.joymove.amqp.handler.EventHandler;
import com.joymove.concurrent.CarOpLock;
import com.joymove.entity.JOYCar;
import com.joymove.entity.JOYNCar;
import com.joymove.entity.JOYOrder;
import com.joymove.service.JOYNCarService;
import com.joymove.service.JOYNOrderService;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
            logger.debug("get the send code report from clouemove");

            String vinNum = String.valueOf(json.get("vin"));
            opLock = CarOpLock.getCarLock(vinNum);
            Map<String,Object> likeCondition = new HashMap<String,Object>();
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
                    likeCondition.put("vinNum", car.getVinNum());
                    List<JOYNCar> ncars = joynCarService.getNeededCar(likeCondition);
                    JOYNCar ncar = ncars.get(0);
                     order.ifBlueTeeth = ncar.ifBlueTeeth;
                    joyNOrderService.insertNOrder(order);
                    cacheCarService.updateCarStateBusy(car);
                } else {
                    //try again
                    cacheCarService.sendPowerOn(car.getVinNum());
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
