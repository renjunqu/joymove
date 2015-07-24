package com.joymove.test.velocity;

import com.joymove.postgres.entity.JOYPowerBar;
import org.apache.velocity.Template;
import org.mybatis.scripting.velocity.VelocityFacade;

import java.util.*;

/**
 * Created by qurj on 15/7/24.
 */
public class testComposite {
    public static String ttt =
            "#if(${$qq})" +
            "   its null" +
            "#else" +
            "   its not null: $qq" +
            "#end";

    public static void main(String[] args) throws  Exception{

        Map<String,Object> context = new HashMap<String, Object>();
        //Map<String,Object> haha = new HashMap<String, Object>();
        JOYPowerBar pb = new JOYPowerBar();
        pb.id = 1L;
        //haha.put("t","qq");
        context.put("haha",pb);
        Template template = (Template) VelocityFacade.compile(testComposite.ttt, "haha_test");
        String result = VelocityFacade.apply(template,context);
        System.out.println("result is \n"+result);
    }
}
