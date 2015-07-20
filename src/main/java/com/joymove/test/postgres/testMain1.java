package com.joymove.test.postgres;

import java.sql.*;
import java.sql.Connection;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.postgresql.*;

/**
 * Created by qurj on 15/7/20.
 */
public class testMain1 {
    public  static void main(String[] args) {
        try {
            String url = "jdbc:postgresql://123.57.151.176:6543/joymove_qrj";
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "qrj12345");
            Connection conn = DriverManager.getConnection(url, props);
            System.out.println("get connection ok");
            System.out.println("conn is "+conn);
            //do a simple query
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM \"JOY_PowerBar\"");
            while(rs.next()) {
                //rs.get 是从1开始的
                /* 获取 id */
                Object id  = rs.getObject("id");
                System.out.println("id is "+id.toString());
                System.out.println("id type is "+id.getClass());
                System.out.println("***************************");
                /* 获取 extend_info */

                Object extendObj  = rs.getObject("extendinfo");
                if(extendObj!=null) {
                    System.out.println("extend info is " + extendObj.toString());
                    System.out.println("extend info type is " + extendObj.getClass());
                    System.out.println("***************************");
                } else {
                    System.out.println("extendObj is null");
                }
                /* 获取 location  */
                Object location  = rs.getObject("location");
                if(location!=null) {
                    System.out.println("location is " + location.toString());
                    System.out.println("location type is " + location.getClass());
                    System.out.println("***************************");
                } else {
                    System.out.println("location is null");

                }
                /* 获取 testArr  */
                Object testArr  = rs.getObject("testArr");
                if(testArr!=null) {
                    System.out.println("testArr is " + testArr.toString());
                    System.out.println("testArr type is " + testArr.getClass());
                } else {
                    System.out.println("testArr is null");
                }
                System.out.println("####################################");



            }
        } catch (SQLException e) {
            System.out.println("exception: "+e.toString());
        }
    }
}
