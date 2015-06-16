package com.joymove.test;

import com.joymove.util.zhifubao.ZhifubaoUtils;

public class testDeposit {
   public static void main(String [] args){
	   String zhifubao_code = ZhifubaoUtils.getPayInfo("depositRecharge", "15701282153", 0.01, "order0-1");
	  // logger.trace(zhifubao_code);
   }
	
}
