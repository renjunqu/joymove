package com.joymove.amqp.handler.impl;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by qurj on 15/6/2.
 */
@Component("PowerOnHandler")
public class PowerOnHandler  implements EventHandler {

    @Resource(name = "CarDynPropsService")
    private CarDynPropsService carPropsService;
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
            CarDynProps carPropsFilter = new CarDynProps();
            CarDynProps  carProps  = new CarDynProps();

            String vinNum = String.valueOf(json.get("vin"));
            opLock = CarOpLock.getCarLock(vinNum);
            Map<String,Object> likeCondition = new HashMap<String,Object>();
            opLock.lock();//>>============================
            carPropsFilter.vinNum = vinNum;
            carProps.fromDocument(carPropsService.find(carPropsFilter).first());
            Long result = Long.parseLong(String.valueOf(json.get("result")));
            if(carProps.state==CarDynProps.state_wait_poweron) {
                logger.error("now we at state_wait_power on, for "+carProps.vinNum);
                if(result==1L) {
                    logger.error("the cloumove tell us it is ok");
                    JOYNCar ncarFilter = new JOYNCar();
                    JOYOrder order = new JOYOrder();
                    order.mobileNo = carProps.owner;
                    order.carVinNum = carProps.vinNum;
                    order.startLongitude = carProps.location.coordinates.get(1);
                    order.startLatitude = carProps.location.coordinates.get(0);
                    ncarFilter.vinNum = carProps.vinNum;
                    List<JOYNCar> ncars = joynCarService.getNeededList(ncarFilter);
                    JOYNCar ncar = ncars.get(0);
                    logger.error("get ncar info ok");
                     order.ifBlueTeeth = ncar.ifBlueTeeth;
                    order.carLicenseNum = ncar.licensenum;
                    //要生成一个唯一的uuid
                    joyNOrderService.createNewOrder(order);
                    logger.error("start to update order status to busy");
                    carProps.clearProperties();
                    carProps.state = CarDynProps.state_busy;
                    carProps.stateUpdateTime = new Date(System.currentTimeMillis());
                    carPropsService.update(carPropsFilter,carProps);
                    logger.error("power on process ok");

                } else {
                    logger.error("the cloudmove tell us it it failed");
                    //try again
                    carPropsService.sendPowerOn(vinNum);
                }
            } else {
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
