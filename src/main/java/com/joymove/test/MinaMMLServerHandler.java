package com.joymove.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MinaMMLServerHandler extends IoHandlerAdapter {

	@Override
	public void sessionCreated(IoSession session) {
    System.out.println(session.getRemoteAddress().toString());
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		String str = message.toString();
		if (str.trim().equalsIgnoreCase("quit")) {
			session.close(true);
			return;
		} else if (str.trim().startsWith("1")) {
			sendPost("http://123.56.106.52:8001/joymove/bikecomm/saveInfo.c", "comm="+str.trim().substring(1));
		} else if (str.trim().startsWith("0")){ 
			session.write(str.trim().substring(1));
		}
		
	}
	
	public String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + param;
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
           
            e.printStackTrace();
        }
       
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
	
	public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            
            URLConnection conn = realUrl.openConnection();
            
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            
            conn.setDoOutput(true);
            conn.setDoInput(true);
           
            out = new PrintWriter(conn.getOutputStream());
           
            System.out.println(param);
            out.print(param);
            
            out.flush();
            out.close();
           
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
           
            e.printStackTrace();
        }
       
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }    
}