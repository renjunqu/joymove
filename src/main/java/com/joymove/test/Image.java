package com.joymove.test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;

import org.springframework.util.FileCopyUtils;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class Image {
	public static void main(String[] args) throws Exception {
		
		 Class.forName("com.mysql.jdbc.Driver");
		 String url="jdbc:mysql://123.56.106.52:3306/mbkdb?user=root&password=123456";
		 Connection con = (Connection) DriverManager.getConnection(url);
		 Statement stmt = (Statement) con.createStatement();
		 String query = "select * from MBK_Image where userId='2706'";
		 ResultSet rs=stmt.executeQuery(query);
//		 byte[] bytes = new byte[9999];
		 int i = 0;
		 while(rs.next())
		 {
			 
//			bytes = rs.getBytes("tupian");
			 InputStream binaryStream = rs.getBinaryStream("image");
			 OutputStream os = new FileOutputStream("C:\\Users\\Public\\tupin"+ ++i + ".png");
			 FileCopyUtils.copy(binaryStream, os);
			 os.close();
			 binaryStream.close();
		 }	
		 
		 //PrintWriter printWriter = new PrintWriter(fileOutputStream);
	}
}
































