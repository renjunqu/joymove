package com.joymove.view;


import com.futuremove.cacheServer.utils.ConfigUtils;
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
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import com.joymove.postgres.dao.*;
import sun.misc.BASE64Decoder;


/**
 * Created by qurj on 15/8/3.
 */

@Controller("JOYPUserController")
public class JOYPUserController extends JOYBaseController {







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
        String getCodeSQL = VelocityFacade.apply(JOYDynamicPwsDao.getPwsTemplate,sqlContext);
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
                    String addUserSQL = VelocityFacade.apply(JOYUserDao.addNewUserTemplate, sqlContext);
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
            String userGetSQL = VelocityFacade.apply(JOYUserDao.getUserByPropsTemplate, sqlContext);
            List userItems = jdbcTemplate.query(userGetSQL,
                    new JOYRowMapper(JOYUser.class));
            if (userItems.size()==1) {
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
                        String updateSQL = VelocityFacade.apply(JOYUserDao.updateUserPropsTemplate,sqlContext);
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
            String userGetSQL = VelocityFacade.apply(JOYUserDao.getUserByPropsTemplate, sqlContext);
            List userItems = jdbcTemplate.query(userGetSQL,
                    new JOYRowMapper(JOYUser.class));
            if(userItems.size()!=1) {
                jsonObject.put("errMsg","输入的手机号有问题");
            } else {
                JOYUser user = (JOYUser)userItems.get(0);
                if (passWord.equals(user.userpwd)) {
                    sqlContext.clear();
                    sqlContext.put("id",user.id);
                    sqlContext.put("userpwd",chPwd);
                    String updateSQL = VelocityFacade.apply(JOYUserDao.updateUserPropsTemplate,sqlContext);
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
        String getCodeSQL = VelocityFacade.apply(JOYDynamicPwsDao.getPwsTemplate, sqlContext);
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
                String userGetSQL = VelocityFacade.apply(JOYUserDao.getUserByPropsTemplate, sqlContext);
                List userItems = jdbcTemplate.query(userGetSQL,
                        new JOYRowMapper(JOYUser.class));
                if(userItems.size()!=1) {
                    jsonObject.put("errMsg","手机号输入错误");
                } else {
                    sqlContext.clear();
                    JOYUser user = (JOYUser)userItems.get(0);
                    sqlContext.put("id",user.id);
                    sqlContext.put("userpwd",password);
                    String updateSQL = VelocityFacade.apply(JOYUserDao.updateUserPropsTemplate,sqlContext);
                    jdbcTemplate.execute(updateSQL);
                    jsonObject.put("result","10000");
                }
            }
        } else {
            jsonObject.put("errMsg","验证码错误");
        }

        return jsonObject;
    }




    @RequestMapping(value={"/pg/usermgr/updateInfo","/pg/usermgr/updateBiologicalInfo"}, method=RequestMethod.POST)
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
        String userGetSQL = VelocityFacade.apply(JOYUserDao.getUserByPropsTemplate, sqlContext);
        List userItems = jdbcTemplate.query(userGetSQL,
                new JOYRowMapper(JOYUser.class));
        if(userItems.size()!=0) {
            jsonObject.put("errMsg","手机号输入错误");
        } else {
            sqlContext.clear();
            cUser = (JOYUser)userItems.get(0);
            JSONObject extendInfo = new JSONObject();
            if(jsonObj.get("face_info")!=null)
                extendInfo.put("face_info",jsonObj.get("face_info"));
            if(jsonObj.get("voice_info")!=null)
                extendInfo.put("voice_info",jsonObj.get("voice_info"));


            sqlContext.put("id",cUser.id);
            sqlContext.put("extendInfo",extendInfo.toJSONString());
            if(jsonObj.get("username")!=null)
                sqlContext.put("username",jsonObj.get("username"));
            if(jsonObj.get("gender")!=null)
                sqlContext.put("gender",jsonObj.get("gender"));
            String updateSQL = VelocityFacade.apply(JOYUserDao.updateUserPropsTemplate, sqlContext);
            jdbcTemplate.execute(updateSQL);
            jsonObject.put("result", "10000");
        }
        return jsonObject;
    }

    @RequestMapping(value="/pg/usermgr/updateIma",method=RequestMethod.POST)
    public @ResponseBody JSONObject userIma(HttpServletRequest req) throws  Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result","10001");
        Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
        String mobileNo = (String)jsonObj.get("mobileNo");
        String image = (String) jsonObj.get("image");
        BASE64Decoder decode = new BASE64Decoder();
        String imagePath = ConfigUtils.getPropValues("upload.images")+"/"+mobileNo+".jpg";
        byte[] byteImage = decode.decodeBuffer(image);
        OutputStream output = new BufferedOutputStream(new FileOutputStream(imagePath));
        output.write(byteImage);
        jsonObject.put("result","10000");
        return jsonObject;
    }

    @RequestMapping(value="/pg/userImage/getIma",method=RequestMethod.GET)
    public String getIma(HttpServletRequest req,HttpServletResponse res) throws  Exception {
        String  mobileNo = (String) req.getParameter("mobileNo");
        byte[] bt = null;
        String imagePath = ConfigUtils.getPropValues("upload.images")+"/"+mobileNo+".jpg";
        File file = new File(imagePath);
        InputStream inputStream = new FileInputStream(file);
        bt = FileCopyUtils.copyToByteArray(inputStream);
        res.setContentType("image/jpg");
        OutputStream os = res.getOutputStream();
        os.write(bt);
        os.flush();
        os.close();

        return null;
    }

    @RequestMapping(value="/pg/usermgr/checkUserMobileNo",method=RequestMethod.POST)
    public @ResponseBody JSONObject checkUserMobileNo(HttpServletRequest req){
        JSONObject  jsonObject = new JSONObject();
        //if the user exists , it will be filtered by the interceptor
        Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
        String mobileNo = (String)jsonObj.get("mobileNo");
        //开始构建SQL,获取验证码
        Map<String,Object> sqlContext = new HashMap<String,Object>();
        sqlContext.put("mobileNo", mobileNo);
        String userGetSQL = VelocityFacade.apply(JOYUserDao.getUserByPropsTemplate, sqlContext);
        List userItems = jdbcTemplate.query(userGetSQL,
                new JOYRowMapper(JOYUser.class));
        if(userItems.size()==1)
            jsonObject.put("result", "10000");
        else
           jsonObject.put("result","10001");
        return jsonObject;
    }


    @RequestMapping(value="/pg/usermgr/updateCommonDestination",method=RequestMethod.POST)
    public @ResponseBody JSONObject updateCommonDestination(HttpServletRequest req){
        JSONObject  Reobj = new JSONObject();
        Reobj.put("result", "10001");


        Hashtable<String,Object> jsonObj = (Hashtable<String, Object>) req.getAttribute("jsonArgs");

        String mobileNo = (String)jsonObj.get("mobileNo");
        //开始构建SQL,获取验证码
        Map<String,Object> sqlContext = new HashMap<String,Object>();
        sqlContext.put("mobileNo", mobileNo);
        String userGetSQL = VelocityFacade.apply(JOYUserDao.getUserByPropsTemplate, sqlContext);
        List userItems = jdbcTemplate.query(userGetSQL,
                new JOYRowMapper(JOYUser.class));
        if(userItems.size()==1) {
            JSONObject addrJSON = new JSONObject();
            if(jsonObj.get("home")!=null)addrJSON.put("home",jsonObj.get("home"));
            if(jsonObj.get("corp")!=null)addrJSON.put("corp",jsonObj.get("corp"));
            sqlContext.put("addresses", addrJSON.toJSONString());
            String updateSQL = VelocityFacade.apply(JOYUserDao.updateUserPropsTemplate, sqlContext);
            jdbcTemplate.execute(updateSQL);
            Reobj.put("result", "10000");
        }
        else {
            Reobj.put("result", "10001");
            Reobj.put("errMsg","用户手机号输入错误");
        }


        Reobj.put("result","10000");
        return Reobj;
    }







}
