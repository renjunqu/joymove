package com.joymove.test.velocity;

import org.apache.velocity.Template;
import org.mybatis.scripting.velocity.VelocityFacade;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by qurj on 15/7/24.
 */
public class testTrim {
    public static String ttt1 = "#trim( \"where\"  \"AND|OR\" \"haha\"  \",\" )" +
            "   and    sdfdsf ," +
            "  #end";

    public static void main(String[] args) throws  Exception{

        //this could be used as a singleton
        Map<String, Object> context = new Hashtable<String, Object>();
        Map<String, Object> _pmc = new Hashtable<String, Object>();
        List<Integer> haha = new ArrayList<Integer>();

        haha.add(1);
        haha.add(2);
        haha.add(3);
        //_pmc.put("ids",haha);

        context.put("ids",haha);
        Template template = (Template) VelocityFacade.compile(testTrim.ttt1, "haha_test");
        String ttt = template.toString();
        String result = VelocityFacade.apply(template,context);
        System.out.println("result is "+result);
    }


}
