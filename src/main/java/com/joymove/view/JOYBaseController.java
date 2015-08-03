package com.joymove.view;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.StringWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jessie on 2015/7/21.
 */
public class JOYBaseController  {

    @ExceptionHandler(Exception.class)
    public @ResponseBody JSONObject handleException(HttpServletRequest req,HttpServletResponse res,Exception ex){
        JSONObject exJson = new JSONObject();
        exJson.put("result","10001");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        exJson.put("errMsg","发生错误，请核查您的输入");//sw.toString());
        exJson.put("errMsg.detail",sw.toString());
        return exJson;
    }


}
