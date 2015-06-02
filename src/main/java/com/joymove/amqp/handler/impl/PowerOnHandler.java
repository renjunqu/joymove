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
        try {
            logger.debug("get the send power on  report from clouemove");

            String vinNum = String.valueOf(json.get("vin"));
            Car car = new Car();
            car.setVinNum(vinNum);
            car = cacheCarService.getByVinNum(vinNum);
            if(car.getState()==Car.state_wait_power) {
                JOYOrder order = new JOYOrder();
                order.mobileNo = (car.getOwner());
                order.carVinNum = (car.getVinNum());
                order.startLongitude = car.getLongitude();
                order.startLatitude = car.getLatitude();
                order.ifBlueTeeth = JOYCar.NON_BT;
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
