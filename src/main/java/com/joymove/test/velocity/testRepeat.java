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

public  class testRepeat {

    public static String ttt = "#repeat( $ids $id  \",\" \" state_id IN (\" \")\" )\n" +
            "    $id\n" +
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
        Template template = (Template)VelocityFacade.compile(testRepeat.ttt,"haha_test");
        String ttt = template.toString();
        String result = VelocityFacade.apply(template,context);
        System.out.println("result is "+result);


    }

}

