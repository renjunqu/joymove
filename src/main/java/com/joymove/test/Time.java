package com.joymove.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Time {
	final static Logger logger = LoggerFactory.getLogger(Time.class);

	public static void main(String[] args) {
		
		 long  ss = 13345561;
		 int tt = 20;
		 Time time = new Time();
		 String time2 = time.getTime(ss);
		 logger.trace(time2);
		 Integer jinQian = time.getJinQian(time2,tt);
		 logger.trace(jinQian.toString());
	}
	
	 private  String getTime(long time) {
		  String str = "" ;
		  time = time / 1000;
		  int s = (int) (time % 60);
		  int m = (int) (time / 60 % 60);
		  int h = (int) (time / 3600);
		  str = h + "";  
		  return str ;
		 }  
	 
	 private Integer getJinQian(String time,int tt1){
		 	int ss = 0;
		 
		  if(time != null && !time.equals("") && tt1 > 0){
			  int parseInt = Integer.parseInt(time);
			  if (parseInt > 1 && parseInt < 2) {
				  ss = tt1 -1;
			  }else if(parseInt >= 2 && parseInt <=5){
				  ss = tt1 -5;
			  }else if(parseInt > 5){
				  ss = tt1 -15;
			  }else {
				  ss = tt1 -1;
			  }
		  }
		  return ss;
	 }
}
