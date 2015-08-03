package com.joymove.view;


import com.joymove.postgres.entity.JOYUser;
import com.joymove.postgres.entity.JOYRowMapper;
import com.joymove.postgres.entity.JOYDynamicPws;
import org.apache.velocity.Template;
import org.json.simple.JSONObject;
import org.mybatis.scripting.velocity.VelocityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by qurj on 15/8/3.
 */

@Controller("JOYPUserController")
public class JOYPUserController extends JOYBaseController {


    public static String addNewUser = " insert into \"JOY_User\" " +
            " (\"mobileNo\",userpwd) values ('${mobileNo}','${userpwd}') ";

    public static String getUserByProps = " select * from \"JOY_User\" " +
            " #where() " +
            " #if(${mobileNo}) and \"mobileNo\" = '${mobileNo}' #end " +
            " #end ";

    public static String updateUserProps = " update \"JOY_User\" " +
            " #mset() " +
            " #if(${authToken}) \"authToken\" = '${authToken}', #end " +
            " #if(${lastActiveTime}) \"lastActiveTime\" = to_timestamp(${lastActiveTime.getTime()}/1000), #end " +
            " #if(${userpwd}) userpwd = '${userpwd}', #end " +
            " #end " +
            " where id=${id} ";


    public static Template addNewUserTemplate;
    public static Template getUserByPropsTemplate;
    public static Template updateUserPropsTemplate;


    {
        JOYPUserController.addNewUserTemplate = (Template)VelocityFacade.compile(JOYPUserController.addNewUser, "addNewUser");

        JOYPUserController.getUserByPropsTemplate = (Template)
                VelocityFacade.compile(JOYPUserController.getUserByProps,"getUserByProps");
        JOYPUserController.updateUserPropsTemplate =
                (Template)VelocityFacade.compile(JOYPUserController.updateUserProps,"updateUserProps");
    }





    @Resource(name="jdbcTemplate")
    private JdbcTemplate jdbcTemplate;


    final static Logger logger = LoggerFactory.getLogger(JOYUserController.class);
    /**
     * @return
     */

    @Transactional(propagation= Propagation.REQUIRED,rollbackFor = Exception.class,isolation = Isolation.REPEATABLE_READ) // 增加这个事务是为了防止两次读取不一致
    @RequestMapping(value="/pg/usermgr/register",method= RequestMethod.POST)
    public @ResponseBody
    JSONObject registration(HttpServletRequest req){
        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("result","10001");


        Hashtable<String, Object> jsonObj = (Hashtable<String, Object>) req.getAttribute("jsonArgs");
        String mobileNo = (String)jsonObj.get("mobileNo");
        String code = String.valueOf(jsonObj.get("code"));
        //String username = (String)jsonObj.get("username");
        String password = (String)jsonObj.get("password");

        //开始构建SQL
        Map<String,Object> sqlContext = new HashMap<String,Object>();
        sqlContext.put("mobileNo",mobileNo);
        String getCodeSQL = VelocityFacade.apply(JOYPDynamicPwsController.getPwsTemplate,sqlContext);
        List codeItems = jdbcTemplate.query(getCodeSQL,
                new JOYRowMapper(com.joymove.postgres.entity.JOYDynamicPws.class));


        if (password.length() <= 5 || password.length() >= 13) {
            jsonObject.put("errMsg","密码格式不对");
        }else{
            JOYDynamicPws joyDynamicPws = (JOYDynamicPws)codeItems.get(0);
            Date time = joyDynamicPws.createTime;
            if(joyDynamicPws.code.equals(code)){
                if((System.currentTimeMillis() - time.getTime())/(60000) < 15){
                    sqlContext.clear();
                    sqlContext.put("mobileNo", mobileNo);
                    sqlContext.put("userpwd",password);
                    String addUserSQL = VelocityFacade.apply(addNewUserTemplate, sqlContext);
                    jdbcTemplate.execute(addUserSQL);
                    jsonObject.put("result","10000");
                } else {
                    jsonObject.put("errMsg","验证码超时");
                }
            } else {
                jsonObject.put("errMsg","验证码错误");
            }
        }
        return jsonObject;
    }

    @Transactional(propagation= Propagation.REQUIRED,rollbackFor = Exception.class,isolation = Isolation.REPEATABLE_READ) // 增加这个事务是为了防止两次读取不一致
    @RequestMapping(value="/pg/usermgr/login",method=RequestMethod.POST)
    public @ResponseBody JSONObject userLogin(HttpServletRequest req) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("result","10001");

            Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
            String mobileNo = (String)jsonObj.get("mobileNo");
            String password = (String)jsonObj.get("password");
            //开始构建SQL
            Map<String,Object> sqlContext = new HashMap<String,Object>();
            sqlContext.put("mobileNo",mobileNo);
            String userGetSQL = VelocityFacade.apply(getUserByPropsTemplate, sqlContext);
            List userItems = jdbcTemplate.query(userGetSQL,
                    new JOYRowMapper(JOYUser.class));
            if (userItems.size() > 0) {
                    JOYUser joyUser = (JOYUser)userItems.get(0);
                    String userpwd = joyUser.userpwd; //getUserpwd();
                    if (password.equals(userpwd)) {
                        //生成sql语句
                        String authToken =  joyUser.mobileNo + "###" + String.valueOf(UUID.randomUUID());
                        sqlContext.clear();
                        sqlContext.put("authToken", authToken);
                        sqlContext.put("lastActiveTime",new Date(System.currentTimeMillis()));
                        System.out.println("date is "+new Date(System.currentTimeMillis()));
                        System.out.println("time is "+System.currentTimeMillis());
                        sqlContext.put("id", joyUser.id);
                        String updateSQL = VelocityFacade.apply(JOYPUserController.updateUserPropsTemplate,sqlContext);
                        jdbcTemplate.execute(updateSQL);
                        jsonObject.put("result", "10000");
                        jsonObject.put("authToken", authToken);

                    }else{
                        jsonObject.put("errMsg","密码错误");
                    }
            }else{
                jsonObject.put("errMsg","用户名不存在");
            }

        return jsonObject;

    }


    @Transactional(propagation= Propagation.REQUIRED,rollbackFor = Exception.class,isolation = Isolation.REPEATABLE_READ) // 增加这个事务是为了防止两次读取不一致
    @RequestMapping(value="/pg/usermgr/updatePwd",method=RequestMethod.POST)
    public @ResponseBody JSONObject updatePwd(HttpServletRequest req){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result","10001");

        Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
        String mobileNo = (String)jsonObj.get("mobileNo");
        String passWord = (String)jsonObj.get("password");
        String chPwd = (String) jsonObj.get("changepassword");


        if (chPwd.length() <= 5 || chPwd.length() >= 13) {
            jsonObject.put("errMsg","密码长度不能小于5位");
        }else{

            //开始构建SQL
            Map<String,Object> sqlContext = new HashMap<String,Object>();
            sqlContext.put("mobileNo",mobileNo);
            String userGetSQL = VelocityFacade.apply(getUserByPropsTemplate, sqlContext);
            List userItems = jdbcTemplate.query(userGetSQL,
                    new JOYRowMapper(JOYUser.class));
            if(userItems.size()==0) {
                jsonObject.put("errMsg","输入的手机号有问题");
            } else {
                JOYUser user = (JOYUser)userItems.get(0);
                if (passWord.equals(user.userpwd)) {
                    sqlContext.clear();
                    sqlContext.put("id",user.id);
                    sqlContext.put("userpwd",chPwd);
                    String updateSQL = VelocityFacade.apply(updateUserPropsTemplate,sqlContext);
                    jdbcTemplate.execute(updateSQL);
                    jsonObject.put("result","10000");
                }else{
                    jsonObject.put("errMsg","旧密码输入错误");
                }
            }
        }



        return jsonObject;
    }



    @Transactional(propagation= Propagation.REQUIRED,rollbackFor = Exception.class,isolation = Isolation.REPEATABLE_READ) // 增加这个事务是为了防止两次读取不一致
    @RequestMapping(value="/pg/usermgr/resetPwd",method=RequestMethod.POST)
    public @ResponseBody JSONObject resetPwd(HttpServletRequest req){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result","10001");

        Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
        String mobileNo = (String)jsonObj.get("mobileNo");
        String password = (String)jsonObj.get("password");
        String code = String.valueOf(jsonObj.get("code"));

        //开始构建SQL,获取验证码
        Map<String,Object> sqlContext = new HashMap<String,Object>();
        sqlContext.put("mobileNo",mobileNo);
        String getCodeSQL = VelocityFacade.apply(JOYPDynamicPwsController.getPwsTemplate, sqlContext);
        List codeItems = jdbcTemplate.query(getCodeSQL,
                new JOYRowMapper(com.joymove.postgres.entity.JOYDynamicPws.class));

        JOYDynamicPws dynamicPws = (JOYDynamicPws)codeItems.get(0);

        if(dynamicPws.code.equals(code) && ((System.currentTimeMillis() - dynamicPws.createTime.getTime())/(60000) < 15)){
            if (password.length() <= 5 || password.length() >= 13) {
                jsonObject.put("errMsg","验证码格式错误");
            }else{
                sqlContext.clear();
                //先尝试获取用户
                sqlContext.put("mobileNo",mobileNo);
                String userGetSQL = VelocityFacade.apply(getUserByPropsTemplate, sqlContext);
                List userItems = jdbcTemplate.query(userGetSQL,
                        new JOYRowMapper(JOYUser.class));
                if(userItems.size()<=0) {
                    jsonObject.put("errMsg","手机号输入错误");
                } else {
                    sqlContext.clear();
                    JOYUser user = (JOYUser)userItems.get(0);
                    sqlContext.put("id",user.id);
                    sqlContext.put("userpwd",password);
                    String updateSQL = VelocityFacade.apply(updateUserPropsTemplate,sqlContext);
                    jdbcTemplate.execute(updateSQL);
                    jsonObject.put("result","10000");
                }
            }
        } else {
            jsonObject.put("errMsg","验证码错误");
        }

        return jsonObject;
    }



    @RequestMapping(value={"usermgr/updateInfo","usermgr/updateBiologicalInfo"}, method=RequestMethod.POST)
    public @ResponseBody JSONObject updateInfo(HttpServletRequest req){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result","10001");

        Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
        String mobileNo = (String)jsonObj.get("mobileNo");
        String URI = req.getRequestURI();
        JOYUser cUser = null;

        //开始构建SQL,获取验证码
        Map<String,Object> sqlContext = new HashMap<String,Object>();
        sqlContext.put("mobileNo",mobileNo);
        String userGetSQL = VelocityFacade.apply(getUserByPropsTemplate, sqlContext);
        List userItems = jdbcTemplate.query(userGetSQL,
                new JOYRowMapper(JOYUser.class));
        if(userItems.size()<=0) {
            jsonObject.put("errMsg","手机号输入错误");
        } else {
            sqlContext.clear();
            cUser = (JOYUser)userItems.get(0);
            if(URI.contains("updateBiologicalInfo")) {
                String face_info = (String) jsonObj.get("face_info");
                String voice_info = (String) jsonObj.get("voice_info");
                com.joymove.entity.JOYUser joyUser = (com.joymove.entity.JOYUser) req.getAttribute("cUser");
                com.joymove.entity.JOYUser userNew = new com.joymove.entity.JOYUser();
                userNew.face_info = face_info;
                userNew.voice_info = voice_info;
                joyUser.mobileNo = mobileNo;
                joyUserService.updateRecord(userNew, joyUser);
                jsonObject.put("result", "10000");
            } else if (URI.contains("updateInfo")) {
                String username = (String) jsonObj.get("username");
                String gender = (String) jsonObj.get("gender");
                com.joymove.entity.JOYUser joyUser = (com.joymove.entity.JOYUser) req.getAttribute("cUser");
                com.joymove.entity.JOYUser userNew = new com.joymove.entity.JOYUser();
                String[] settingProps = {"username", "gender"};
                if(username!=null)
                    userNew.username = username;
                if(gender!=null)
                    userNew.gender = gender;

                joyUserService.updateRecord(userNew, joyUser);
                jsonObject.put("result", "10000");
            }
        }
        return jsonObject;
    }








}
