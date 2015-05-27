package com.joymove.view;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.futuremove.cacheServer.service.impl.CarServiceImpl;
import com.joymove.entity.JOYDynamicPws;
import com.joymove.entity.JOYOrder;
import com.joymove.entity.JOYUser;
import com.joymove.service.JOYOrderService;
import com.joymove.service.JOYUserService;




@Scope("prototype")
@Controller("JOYZhifubaoController")
public class JOYZhifubaoController {
	public static JedisPool pool = new JedisPool(new JedisPoolConfig(), "123.57.151.176");
	/*
	 *  zhifubao info:
	 *  
	 *  discount=0.00&payment_type=1&subject=dsfdsf&trade_no=2015040200001000670047119807&buyer_email=xinghongz%40sina.com&gmt_create=2015-04-02+20%3A14%3A56&notify_type=trade_status_sync&quantity=1&out_trade_no=dsfsdf&seller_id=2088911128852234&notify_time=2015-04-02+20%3A14%3A57&body=dsfdsf&trade_status=TRADE_SUCCESS&is_total_fee_adjust=N&total_fee=0.01&gmt_payment=2015-04-02+20%3A14%3A57&seller_email=wxmp%40futuremove.cn&price=0.01&buyer_id=2088002144215676&notify_id=492e4d81ab09f96ee13ed05a0c75ae2b5q&use_coupon=N&sign_type=RSA&sign=elct6eg8mM2dN%2F%2FYxhV4B333tO8iKtUKELCynqGdLQIEjV6olpKu71k%2FqmAiCriRuHjYuUxLsu%2BMGGDHiluGt9p48lCdMR2Y3abDyXB486F%2F3YxSTpvyWpM6bCPYKZhm2UZLsH8t%2Bc2ZrbOzBtxVt5My4CMCw9Y8B7KeM%2BEb84Y%3D
	 *  subject & body & out_trade_no
	 * *
	 */
	
	final static Logger logger = LoggerFactory.getLogger(JOYZhifubaoController.class);

	@Resource(name = "JOYOrderService")
	private JOYOrderService joyOrderService;
	@Resource(name = "JOYUserService")
	private JOYUserService joyUserService;
	
		/************  business proc   *****************/
	
	
	//same pay, the zhifubao may be notify multi times
	//the tow out_trade_no could not be same
	@RequestMapping(value={"zhifubao/notify"},method=RequestMethod.POST)
	public @ResponseBody String resetPwd(HttpServletRequest req){
		
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		try {
			String subject = req.getParameter("subject");
			String body    = req.getParameter("body");
			String out_trade_no = req.getParameter("out_trade_no");
			 //first get the orders
			String currTimeStr = System.currentTimeMillis()+"";
			logger.debug("get zhifubao info");
			if(subject.equals("rentPay")) {
				
				 String mobileNo = body;
				 logger.debug("it is a rent pay info , mobileNo is "+mobileNo);
				 likeCondition.put("mobileNo",mobileNo);
				 likeCondition.put("delMark",JOYOrder.NON_DEL_MARK);
				 likeCondition.put("state",JOYOrder.state_wait_pay);
				 List<JOYOrder> orders =  joyOrderService.getNeededOrder(likeCondition);
				 JOYOrder order = orders.get(0);
				 order.delMark = (JOYOrder.DEL_MARK);
				 joyOrderService.deleteOrder(order);
				 logger.debug("rent pay over ");
				 
			} else if (subject.equals("depositRecharge")){
				
				Jedis jedis =  pool.getResource();
				
				logger.debug("get user deposit charging message");
				String mobileNo = body;
				String jedisKey = mobileNo+out_trade_no;
				
				Double deposit = Double.valueOf(out_trade_no.substring(currTimeStr.length()).replace("-", "."));
				String payed = jedis.get(jedisKey);
				if(payed!=null && payed.length()>10) {
				    //alreayed payed.
					return "success";
				} else {
					jedis.set(jedisKey, "this is just uesd to prevent zhifubao for multi calling");
				}
				
				System.out.println("mobile No is "+mobileNo);
				System.out.println("deposit is "+deposit);
				JOYUser user = new JOYUser();
				user.mobileNo = mobileNo;  //setMobileNo(mobileNo);
				List<JOYUser> users = joyUserService.getNeededUser(user);
				 
			    if(users.size()==1) {
			          BigDecimal currDepo = users.get(0).deposit; //.getDeposit();
			          System.out.println("before recharge: "+currDepo);
			          currDepo = currDepo.add(BigDecimal.valueOf(deposit));
			          user.deposit = currDepo; //setDeposit(currDepo);
			          user.mobileNo = mobileNo; //setMobileNo(mobileNo);
			          System.out.println("after recharge: "+currDepo);
			          joyUserService.updateJOYUser(user);
			          //record this pay
			          
			          logger.debug("recharge ok");
			    }
			}
		} catch (Exception e){
			logger.info(e.toString());
		}
	    return "success";
	}


    public static void main(String [] args){
    	String t = "order112345";
    	System.out.println(t.substring(6).replace("1", "a"));
    }
	





}
