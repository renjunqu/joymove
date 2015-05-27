package com.joymove.view;

import com.joymove.entity.JOYOrder;
import com.joymove.entity.JOYUser;
import com.joymove.entity.JOYWXPayInfo;
import com.joymove.service.JOYOrderService;
import com.joymove.service.JOYUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import com.joymove.util.WeChatPay.WeChatPayUtil;
import com.joymove.service.JOYWXPayInfoService;
/**
 * Created by qurj on 15/5/12.
 */
@Scope("prototype")
@Controller("JOYWeChatController")
public class JOYWeChatController {


    final static Logger logger = LoggerFactory.getLogger(JOYWeChatController.class);

    @Resource(name = "JOYOrderService")
    private JOYOrderService joyOrderService;
    @Resource(name = "JOYUserService")
    private JOYUserService joyUserService;
    @Resource(name = "JOYWXPayInfoService")
    private JOYWXPayInfoService joywxPayInfoService;


    @RequestMapping(value={"wechat/notify"},method= RequestMethod.POST)
    public @ResponseBody String weChatPay(HttpServletRequest req){
        String succStr = "<xml>\n<return_code><![CDATA[SUCCESS]]></return_code>\n<return_msg><![CDATA[OK]]></return_msg>\n</xml>";
        String failStr = "<xml>\n<return_code><![CDATA[FAIL]]></return_code>\n<return_msg><![CDATA[OK]]></return_msg>\n</xml>";

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
                likeCondition.put("out_trade_no", trade_no);
                likeCondition.put("payOverFlag",0);
                List<JOYWXPayInfo> infos = joywxPayInfoService.getNeededPayInfo(likeCondition);
                if(infos.size()==1) {
                    JOYWXPayInfo payInfo = infos.get(0);

                    if (trade_no.contains("deposit")) {

                        JOYUser user = new JOYUser();
                        user.mobileNo = payInfo.mobileNo; //setMobileNo(payInfo.getMobileNo());
                        List<JOYUser> users = joyUserService.getNeededUser(user);

                        if (users.size() == 1) {
                            BigDecimal currDepo = users.get(0).deposit;  // getDeposit();
                            System.out.println("before recharge: " + currDepo);
                            currDepo = currDepo.add(BigDecimal.valueOf(Double.valueOf(fee) / 100));
                            user.deposit = currDepo;  //setDeposit(currDepo);
                            user.mobileNo = payInfo.mobileNo;  //setMobileNo(payInfo.getMobileNo());
                            System.out.println("after recharge: " + currDepo);
                            joyUserService.updateJOYUser(user);
                            logger.debug("recharge ok");
                        }
                    } else if (trade_no.contains("rentPay")) {

                        logger.debug("it is a rent pay info , mobileNo is " + payInfo.mobileNo);
                        likeCondition.put("mobileNo", payInfo.mobileNo);
                        likeCondition.put("delMark", JOYOrder.NON_DEL_MARK);
                        likeCondition.put("state", JOYOrder.state_wait_pay);
                        List<JOYOrder> orders = joyOrderService.getNeededOrder(likeCondition);
                        JOYOrder order = orders.get(0);
                        order.delMark = (JOYOrder.DEL_MARK);
                        joyOrderService.deleteOrder(order);
                        logger.debug("rent pay over ");
                    }
                    // mark the pay info
                    joywxPayInfoService.markPayInfo(payInfo);
                } else {
                    // no no pay trade
                    return succStr;
                }
            }
          //  System.out.println(jstr);
           // System.out.println("++++++ inside wechat controller ");
        } catch (Exception e){
           System.out.println(e.toString());
            return failStr;
        }
        return succStr;
    }



}
