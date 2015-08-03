package com.joymove.test.velocity;

import com.joymove.postgres.entity.JOYPowerBar;
import org.apache.velocity.Template;
import org.mybatis.scripting.velocity.VelocityFacade;

import java.util.*;

/**
 * Created by qurj on 15/7/24.
 */
public class testComposite {
    //当为false,或者为定义的时候, if($qq) 会变成false
    public static String ttt =
            "#if ($qq && $qq.length()>0)" +
                    " its not null " +
                    " #else " +
                    " its null " +
                    " #end ";

    public static void main(String[] args) throws  Exception{

        Map<String,Object> context = new HashMap<String, Object>();
        //Map<String,Object> haha = new HashMap<String, Object>();
        JOYPowerBar pb = new JOYPowerBar();
        pb.id = 1L;
        context.put("qq",3.2);
        context.put("haha","sdfsdfdsf");
        Template template = (Template) VelocityFacade.compile(testComposite.ttt, "haha_test");
        String result = VelocityFacade.apply(template,context);
        System.out.println("result is \n"+result);
    }
}
