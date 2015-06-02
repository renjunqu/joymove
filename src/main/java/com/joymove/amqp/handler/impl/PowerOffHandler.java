package com.joymove.amqp.handler.impl;

import com.joymove.amqp.handler.EventHandler;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Created by qurj on 15/6/2.
 */
@Component("PowerOffHandler")
public class PowerOffHandler implements EventHandler {
    public int getEventType() {
        return 11;
    }

    public boolean handleData(JSONObject json) {
        boolean error=true;

        return error;
    }

}
