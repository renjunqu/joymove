package com.joymove.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringOrInteger {
	
	public static void main(String []args){
		
		String tt ="123456";
		System.out.println(StringOrInteger.isValidateNumber(tt));
	}
	public static boolean isValidateNumber(String str) throws NumberFormatException{
        if (str==null) {
          return false;
        }
        try {
        	Pattern p = Pattern.compile("[\\d]+");
        	Matcher m = p.matcher(str);
        	return m.matches();
        } catch (Exception e){

          e.printStackTrace();
          return false;
        }
      }
}
