package com.joymove.test.velocity;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.mybatis.scripting.velocity.VelocityFacade;

public  class test1 {

    public static String ttt = "#repeat( $ids $id \",\" \" state_id IN (\" \")\" )\n" +
            "    @{id}\n" +
            "  #end";
    public static void main(String[] args) throws  Exception{

        //this could be used as a singleton
        Map<String, Object> context = new Hashtable<String, Object>();
        Map<String, Object> _pmc = new Hashtable<String, Object>();
        List<Integer> haha = new ArrayList<Integer>();

        haha.add(1);
        _pmc.put("ids",haha);
        context.put("_pmc",_pmc);
        Template template = (Template)VelocityFacade.compile(test1.ttt,"haha_test");
        String result = VelocityFacade.apply(template,context);
        System.out.println("result is "+result);


    }

}

