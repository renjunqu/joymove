package com.joymove.view;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import com.joymove.service.JOYSeedService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import com.joymove.entity.JOYSeed;

/**
 * Created by figoxu on 15/5/5.
 */


@Controller("JOYSeedController")
public class JOYSeedController {
    @Resource(name = "JOYSeedService")
    private JOYSeedService joySeedService;


    /******  business proc ********/
    @RequestMapping(value="seed/exchangeCoupon", method= RequestMethod.POST)
    public  @ResponseBody
    JSONObject exchangeCoupon(HttpServletRequest req){
        JSONObject Reobj  = new JSONObject();
        Map<String,Object> likeCondition = new HashMap<String, Object>();
        Reobj.put("result","10001");
        JOYSeed seed = null;
        try {

            Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
            likeCondition.put("code", jsonObj.get("code"));
            likeCondition.put("mobileNo", jsonObj.get("mobileNo"));
            likeCondition.put("status", JOYSeed.status_seed_alive);
            likeCondition.put("type", JOYSeed.type_coupon_seed);

            seed = joySeedService.getNeededSeed(likeCondition);
            if((seed != null) && seed.mobileNo.equals(jsonObj.get("mobileNo"))) {
                 joySeedService.exchangeCoupon(seed);
                Reobj.put("result","10000");
            } else {
                Reobj.put("errMsg", "输入的兑换码有错误");
            }
        } catch(Exception e){
             Reobj.put("errMsg",e.toString());
        }
        return Reobj;
    }


}
