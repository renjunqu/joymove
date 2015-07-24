package com.joymove.test.testStatic;

/**
 * Created by qurj on 15/7/24.
 */
public class test2 {
    static {
        System.out.println("test2 static init");
    }
    public static void test(){
         System.out.println("inside test2 test func");
    }
    public static void main(String [] args){
        test1.test();
    }
}
