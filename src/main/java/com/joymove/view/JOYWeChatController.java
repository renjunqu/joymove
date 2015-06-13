package com.joymove.view;

import com.futuremove.cacheServer.concurrent.UserOptLock;
import com.joymove.entity.JOYOrder;
import com.joymove.entity.JOYPayHistory;
import com.joymove.entity.JOYUser;
import com.joymove.entity.JOYPayReqInfo;
import com.joymove.service.JOYOrderService;
import com.joymove.service.JOYPayHistoryService;
import com.joymove.service.JOYUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.util.concurrent.locks.ReentrantLock;

import com.joymove.util.WeChatPay.WeChatPayUtil;
import com.joymove.service.JOYPayReqInfoService;
/**
 * Created by qurj on 15/5/12.
 */

@Controller("JOYWeChatController")
public class JOYWeChatController {


    final static Logger logger = LoggerFactory.getLogger(JOYWeChatController.class);

    @Resource(name = "JOYOrderService")
    private JOYOrderService joyOrderService;
    @Resource(name = "JOYUserService")
    private JOYUserService joyUserService;
    @Resource(name = "JOYPayReqInfoService")
    private JOYPayReqInfoService joyPayReqInfoService;
    @Resource(name ="JOYPayHistoryService")
    private JOYPayHistoryService joyPayHistoryService;



    @RequestMapping(value={"wechat/notify"},method= RequestMethod.POST)
    public void weChatPay(HttpServletRequest req,HttpServletResponse response){
        String succStr = "<xml>" +
                "<return_code><![CDATA[SUCCESS]]></return_code>" +
                "<return_msg><![CDATA[OK]]></return_msg>" +
                "</xml>";

        JOYPayHistory payHistoryNew = new JOYPayHistory();
        payHistoryNew.type = JOYPayHistory.weixin_type;
        ReentrantLock optLock = null;
        try {
          //  System.out.println("++++++ inside wechat controller ");
            Map<String,Object> likeCondition = new HashMap<String, Object>();

            BufferedReader reader = req.getReader();
            StringBuffer jb = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null)
                jb.append(line);
            String jstr = jb.toString();

            String weChatRet = WeChatPayUtil.getXmlElement("result_code",jstr);
            if(weChatRet.equals("SUCCESS")) {
                String fee = WeChatPayUtil.getXmlElement("total_fee",jstr);
                String trade_no = WeChatPayUtil.getXmlElement("out_trade_no", jstr);
                JOYPayReqInfo filterObj = new JOYPayReqInfo();
                filterObj.out_trade_no = trade_no;
                filterObj.payOverFlag = 0;
                filterObj.type = JOYPayReqInfo.type_wx;

                List<JOYPayReqInfo> infos = joyPayReqInfoService.getNeededList(filterObj);
                if(infos.size()==1) {
                        JOYPayReqInfo payInfo = infos.get(0);
                        //开始加锁
                        optLock = UserOptLock.getUserLock(payInfo.mobileNo);
                        optLock.lock();
                        //要重新获取一次payInfo
                          payInfo = joyPayReqInfoService.getNeededRecord(filterObj);
                        if(payInfo==null) {
                            //已经被其他线程支付过了
                        } else if (trade_no.contains("deposit")) {
                            JOYUser userFilter = new JOYUser();
                            userFilter.mobileNo = payInfo.mobileNo; //setMobileNo(payInfo.getMobileNo());
                            userFilter = joyUserService.getNeededRecord(userFilter);
                            JOYUser userNew = new JOYUser();
                            BigDecimal currDepo =userFilter.deposit;  // getDeposit();
                            System.out.println("before recharge: " + currDepo);
                            currDepo = currDepo.add(BigDecimal.valueOf(Double.valueOf(fee) / 100));
                            userNew.deposit = currDepo;  //setDeposit(currDepo);
                            userNew.mobileNo = payInfo.mobileNo;  //setMobileNo(payInfo.getMobileNo());
                            System.out.println("after recharge: " + currDepo);
                            joyUserService.updateRecord(userNew,userFilter);
                            //记录支付过程
                            payHistoryNew.balance = payInfo.totalFee;
                            payHistoryNew.target = JOYPayHistory.pay_for_deposit;
                            payHistoryNew.mobileNo = payInfo.mobileNo;
                            //记录支付历史信息
                            joyPayHistoryService.insertRecord(payHistoryNew);
                            logger.debug("recharge ok");
                        } else if (trade_no.contains("rentPay")) {

                            logger.debug("it is a rent pay info , mobileNo is " + payInfo.mobileNo);
                            JOYOrder order = new JOYOrder();
                            order.mobileNo = payInfo.mobileNo;
                            order.delMark = JOYOrder.NON_DEL_MARK;
                            order.state = JOYOrder.state_wait_pay;
                            order = joyOrderService.getNeededRecord(order);
                            //检车订单id是否是Null,否则就全部都变成支付成功了。
                            payHistoryNew.orderId = order.id;
                            JOYOrder valueObj = new JOYOrder();
                            valueObj.delMark = JOYOrder.DEL_MARK;
                            valueObj.state = JOYOrder.state_pay_over;
                            joyOrderService.updateRecord(valueObj,order);
                            logger.debug("rent pay over ");
                            //记录支付过程
                            payHistoryNew.balance = payInfo.totalFee;
                            payHistoryNew.target = JOYPayHistory.pay_for_rent;
                            payHistoryNew.mobileNo = payInfo.mobileNo;

                            //记录支付历史信息
                            joyPayHistoryService.insertRecord(payHistoryNew);
                        }
                        // mark the pay info
                        JOYPayReqInfo valueInfoObj = new JOYPayReqInfo();
                        valueInfoObj.payOverFlag = 1;
                        joyPayReqInfoService.updateRecord(valueInfoObj,payInfo); //markPayInfo(payInfo);
                        //解锁
                        optLock.unlock();
                }// size() == 1

                //给微信返回值
                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(succStr.getBytes());
                outputStream.close();

            }//equals SUCCESS
          //  System.out.println(jstr);
           // System.out.println("++++++ inside wechat controller ");
        } catch (Exception e){
           System.out.println(e.toString());
            //记得做解锁
            if(optLock!=null && optLock.getHoldCount()>0)
                optLock.unlock();
        }
    }//over notify
}
