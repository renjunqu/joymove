package com.joymove.view;

import com.joymove.entity.JOYCarCluster;
import com.joymove.entity.JOYPowerBar;
import com.joymove.service.JOYCarClusterService;
import com.joymove.service.JOYPowerBarService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by qurj on 15/6/23.
 */
@Controller("JOYCarClusterController")
public class JOYCarClusterController {
    @Resource(name = "JOYCarClusterService")
    private JOYCarClusterService joyCarClusterService;


    final static Logger logger = LoggerFactory.getLogger(JOYCarClusterController.class);



    @RequestMapping(value="newcar/getNearByCluster", method= RequestMethod.POST)
    public  @ResponseBody
    JSONObject getNearByCluster(HttpServletRequest req){
        logger.trace("getNearByCluster method was invoked...");
        Map<String,Object> likeCondition = new HashMap<String, Object>();
        JSONObject Reobj=new JSONObject();
        JSONArray ccArray  = new JSONArray();

        Reobj.put("result", "10001");
        Reobj.put("clusters", ccArray);
        try {
            Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");;
            likeCondition.put("longitude", jsonObj.get("userLongitude")==null ? 0.0: jsonObj.get("userLongitude") );
            likeCondition.put("latitude", jsonObj.get("userLatitude")==null ? 0.0: jsonObj.get("userLatitude") );
            likeCondition.put("scope", jsonObj.get("scope") == null ? 10 : jsonObj.get("scope"));
            List<JOYCarCluster> ccs = joyCarClusterService.getCarClusterByScope(likeCondition);

            Iterator iter = ccs.iterator();
            while(iter.hasNext()){
                JOYCarCluster pb_item  = (JOYCarCluster)iter.next();
                ccArray.add(pb_item.toJSON());
            }
            Reobj.put("result", "10000");

        } catch(Exception e) {
            Reobj.put("result", "10001");
            logger.error(e.getStackTrace().toString());
        }
        return Reobj;
    }

}
