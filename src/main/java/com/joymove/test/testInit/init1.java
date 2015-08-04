package com.joymove.test.testInit;
import com.joymove.test.testInit.init2;

/**
 * Created by qurj on 15/8/4.
 */
public class init1  extends  init2{
    static {

        System.out.println("hi, i am static1 initialer");

    }

    public  static void main(String [] args){
        System.out.println("main\n");
    }
}
