package com.joymove.test.postgres;

import org.json.simple.JSONObject;
import org.mongodb.morphia.geo.Geometry;
import org.postgis.PGgeometry;
import org.postgis.java2d.PGShapeGeometry;
import org.postgresql.geometric.PGpoint;
import org.postgresql.jdbc4.Jdbc4Array;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by qurj on 15/7/20.
 */
public class testInsert {

    public  static void main(String[] args) {
        try {
            String url = "jdbc:postgresql://123.57.151.176:6543/joymove_qrj";
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "qrj12345");
            Connection conn = DriverManager.getConnection(url, props);
            System.out.println("get connection ok");
            System.out.println("conn is sdfdsfdsf" + conn);
            Statement st = conn.createStatement();
            StringBuffer sqlBuilder = new StringBuffer(" insert into \"JOY_PowerBar\" (extendinfo,location,\"testArr\") values(");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("q","r");
            sqlBuilder.append("\'"+jsonObject.toJSONString()+"\'::jsonb,");
            PGpoint point  = new PGpoint();
            point.x = 3.2;
            point.y = 2.4;
            sqlBuilder.append("ST_GeographyFromText(\'SRID=4326;POINT(" + point.x + " " + point.y + ")\'),");
            ArrayList<Integer> arr = new ArrayList<Integer>();
            arr.add(1);
            arr.add(2);
            String arrStr = "{";
            for(int i=0;i<arr.size();i++) {
                arrStr += i;
                if(i<arr.size()-1)
                    arrStr+=",";
            }
            arrStr+="}";
            sqlBuilder.append("\'"+arrStr+"\');");
            System.out.println("result sql is " + sqlBuilder.toString());
            st.execute(sqlBuilder.toString());



        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
