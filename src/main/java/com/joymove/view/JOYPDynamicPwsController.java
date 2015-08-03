package com.joymove.view;

import com.joymove.postgres.entity.JOYDynamicPws;
import com.joymove.postgres.entity.JOYPowerBar;
import com.joymove.postgres.entity.JOYRowMapper;
import com.joymove.util.SmsUtils;
import org.apache.velocity.Template;
import org.json.simple.JSONObject;
import org.mybatis.scripting.velocity.VelocityFacade;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by qurj on 15/8/3.
 */
@Controller("JOYPDynamicPwsController")
public class JOYPDynamicPwsController extends  JOYBaseController  {

    public static String insertPws = "insert into \"JOY_DynamicPws\" " +
            "(\"mobileNo\",code) values ('${mobileNo}','${code}')";
    public static String getPws = "SELECT * from \"JOY_DynamicPws\" " +
            "where \"mobileNo\" = '${mobileNo}' order by id desc limit 1;";



    public static Template insertPwsTemplate;
    public static Template getPwsTemplate;

    {
        JOYPDynamicPwsController.insertPwsTemplate = (Template) VelocityFacade.compile(JOYPDynamicPwsController.insertPws, "insertPws");
        JOYPDynamicPwsController.getPwsTemplate = (Template) VelocityFacade.compile(JOYPDynamicPwsController.getPws, "getPws");
    }



    @Resource(name="jdbcTemplate")
    private JdbcTemplate jdbcTemplate;



    @RequestMapping(value="/pg/usermgr/dynamicPwsGen", method= RequestMethod.POST)
    public @ResponseBody
    JSONObject dynamicPwsGen(HttpServletRequest req) {

        JSONObject Reobj = new JSONObject();
        Reobj.put("result", "10001");


            Hashtable<String,Object> jsonArgs = (Hashtable<String,Object>)req.getAttribute("jsonArgs");//JsonHashUtils.strToJSONHash(req.getReader());

            String base = "123456789";
            Random random = new Random();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 4; i++) {
                int number = random.nextInt(base.length());
                sb.append(base.charAt(number));
            }

            String  mobileNo = (String) jsonArgs.get("mobileNo");
            //开始构建SQL
            Map<String,Object> sqlContext = new HashMap<String,Object>();
            sqlContext.put("mobileNo",mobileNo);
            sqlContext.put("code", sb.toString());
            String insertSQL = VelocityFacade.apply(insertPwsTemplate, sqlContext);
            System.out.println("insertSQL is "+insertSQL);
            jdbcTemplate.execute(insertSQL);
            SmsUtils.sendRegisterCode(sb.toString(), String.valueOf(mobileNo));
            Reobj.put("result", "10000");
            Reobj.put("number", sb.toString());

            return Reobj;

    }

    @RequestMapping(value="/usermgr/pg/dynamicPwsVeri", method=RequestMethod.POST)
    public @ResponseBody JSONObject dynamicPwsVeri(HttpServletRequest req) {

        JSONObject Reobj=new JSONObject();
        Reobj.put("result","10001");


        Hashtable<String,Object> jsonObj = (Hashtable<String,Object>)req.getAttribute("jsonArgs");//JsonHashUtils.strToJSONHash(req.getReader());
        String number = String.valueOf(jsonObj.get("number"));

        String mobileNo = String.valueOf(jsonObj.get("phoneNo"));
        //开始构建SQL
        Map<String,Object> sqlContext = new HashMap<String,Object>();
        sqlContext.put("mobileNo",mobileNo);
        String getSQL = VelocityFacade.apply(getPwsTemplate, sqlContext);

        List items = jdbcTemplate.query(getSQL,
                new JOYRowMapper(JOYDynamicPws.class));




        if(items.size()==1) {
            JOYDynamicPws dynamicPws = (JOYDynamicPws)items.get(0);
            Date time = dynamicPws.createTime;
            if(dynamicPws.code.equals(number)){
                if(time!=null && ((System.currentTimeMillis() - time.getTime())/(60000) < 5)){
                    Reobj.put("result","10000");
                }
            }
        }


        return Reobj;

    }





}
