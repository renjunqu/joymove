package com.joymove.view;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.futuremove.cacheServer.concurrent.UserOptLock;
import com.joymove.entity.JOYPayHistory;
import com.joymove.entity.JOYPayReqInfo;
import com.joymove.service.JOYPayHistoryService;
import com.joymove.service.JOYPayReqInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.joymove.entity.JOYOrder;
import com.joymove.entity.JOYUser;
import com.joymove.service.JOYOrderService;
import com.joymove.service.JOYUserService;





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
	@Resource(name ="JOYPayHistoryService")
	private JOYPayHistoryService  joyPayHistoryService;
	@Resource(name = "JOYPayReqInfoService")
	private JOYPayReqInfoService joyPayReqInfoService;

	
		/************  business proc   *****************/
	
	
	//same pay, the zhifubao may be notify multi times
	//the tow out_trade_no could not be same
	@RequestMapping(value={"zhifubao/notify"},method=RequestMethod.POST)
	public void  resetPwd(HttpServletRequest req,HttpServletResponse response){
		
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		JOYOrder orderFilter = new JOYOrder();
		JOYOrder orderNew = new JOYOrder();
		JOYPayHistory payHistoryNew = new JOYPayHistory();
		payHistoryNew.type = JOYPayHistory.zhifubao_type;
		ReentrantLock optLock = null;

		try {
			String subject = req.getParameter("subject");
			String body    = req.getParameter("body");
			String out_trade_no = req.getParameter("out_trade_no");
			String mobileNo = body;
			JOYPayReqInfo filterObj = new JOYPayReqInfo();
			filterObj.out_trade_no = out_trade_no;
			filterObj.payOverFlag = 0;
			filterObj.type = JOYPayReqInfo.type_zhifubao;

			List<JOYPayReqInfo> infos = joyPayReqInfoService.getNeededList(filterObj);

            if(infos.size()==1) {
				 JOYPayReqInfo payInfo = infos.get(0);
				 //开始加锁，用户信息的操作锁
				 optLock = UserOptLock.getUserLock(payInfo.mobileNo);
				 optLock.lock();
				 //要重新获取一次payInfo
				 payInfo = joyPayReqInfoService.getNeededRecord(filterObj);
				//first get the orders
				String currTimeStr = System.currentTimeMillis() + "";
				logger.debug("get zhifubao info");
				if (subject.equals("rentPay")) {
					logger.debug("it is a rent pay info , mobileNo is " + mobileNo);
					orderFilter.mobileNo = mobileNo;
					orderFilter.delMark = JOYOrder.NON_DEL_MARK;
					orderFilter.state = JOYOrder.state_wait_pay;
					orderFilter = joyOrderService.getNeededRecord(orderFilter);
					//检查orderFilter是否是null,否则就全部都变成支付成功了。
					payHistoryNew.orderId = orderFilter.id;
					orderNew.delMark = JOYOrder.DEL_MARK;
					orderNew.state = JOYOrder.state_pay_over;
					joyOrderService.updateRecord(orderNew, orderFilter);
					payHistoryNew.balance = payInfo.totalFee;
					payHistoryNew.target = JOYPayHistory.pay_for_rent;
					payHistoryNew.mobileNo = mobileNo;
					//记录支付历史信息
					joyPayHistoryService.insertRecord(payHistoryNew);
					logger.debug("rent pay over ");

				} else if (subject.equals("depositRecharge")) {

					logger.trace("mobile No is " + payInfo.mobileNo);
					logger.trace("deposit is " + payInfo.totalFee);
					JOYUser userFilter = new JOYUser();
					userFilter.mobileNo = mobileNo;  //setMobileNo(mobileNo);
					JOYUser userValue = joyUserService.getNeededRecord(userFilter);
					BigDecimal currDepo = userValue.deposit; //.getDeposit();
					logger.trace("before recharge: " + currDepo);
					currDepo = currDepo.add(BigDecimal.valueOf(payInfo.totalFee));
					userValue.deposit = currDepo; //setDeposit(currDepo);
					logger.trace("after recharge: " + currDepo);
					joyUserService.updateRecord(userValue, userFilter);
					//record this pay
					//记录支付过程
					payHistoryNew.balance = payInfo.totalFee;
					payHistoryNew.target = JOYPayHistory.pay_for_deposit;
					payHistoryNew.mobileNo = mobileNo;
					//记录支付历史信息
					joyPayHistoryService.insertRecord(payHistoryNew);
					logger.debug("recharge ok");
				}
				// mark the pay info
				JOYPayReqInfo valueInfoObj = new JOYPayReqInfo();
				valueInfoObj.payOverFlag = 1;
				joyPayReqInfoService.updateRecord(valueInfoObj,payInfo); //markPayInfo(payInfo);
			}//if info.size == 1
			//给微信返回值
			ServletOutputStream outputStream = response.getOutputStream();
			outputStream.write("success".getBytes());
			outputStream.close();
			optLock.unlock();

		} catch (Exception e){
			logger.info(e.toString());
			if(optLock!=null && optLock.getHoldCount()>0)
				optLock.unlock();
		}
	  //  return "success";
	}
    public static void main(String [] args){
    	String t = "order112345";
    	logger.trace(t.substring(6).replace("1", "a"));
    }
	





}
