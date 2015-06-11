package com.joymove.view;

import com.joymove.entity.JOYOrder;
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

            JOYSeed seedFilter = new JOYSeed();
            Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
            seedFilter.code = String.valueOf(jsonObj.get("code"));
            seedFilter.mobileNo = String.valueOf(jsonObj.get("mobileNo"));
            seedFilter.status = JOYSeed.status_seed_alive;
            seedFilter.type = JOYSeed.type_coupon_seed;
            seed = joySeedService.getNeededRecord(seed);
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
