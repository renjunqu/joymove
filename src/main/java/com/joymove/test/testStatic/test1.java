package com.joymove.test.testStatic;
import com.joymove.test.testStatic.test2;
/**
 * Created by qurj on 15/7/24.
 */
public class test1 {
    static {
        System.out.println("test1 static init");
    }
    public static void main(String[] args){
          test2.test();
    }
    public static void test(){
        System.out.println("test1 test func");
    }
}
