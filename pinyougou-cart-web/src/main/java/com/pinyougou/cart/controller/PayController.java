package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;

@RestController
@RequestMapping("pay")
public class PayController {
	@Reference
	private WeixinPayService weixinPayService;

	@Reference
	private OrderService orderService;

	/**
	 * 创建二维码，参数从payLog中获取
	 * 
	 * @param out_trade_no
	 * @param total_fee
	 * @return
	 */
	@RequestMapping("createNative")
	public Map createNative() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		TbPayLog payLog = orderService.getPayLogByRedis(username);
		if (payLog == null) {
			return new HashMap();
		}
		return weixinPayService.creatNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
	}

	@RequestMapping("quertForPayStatu")
	public Result quertForPayStatu(String out_trade_no) {

		Result result = null;
		int x = 0;
		while (true) {
			Map<String, String> map = weixinPayService.queryForStatu(out_trade_no);
			if (map == null) {
				result = new Result(false, "支付出错");
				break;
			} else {
				if (map.get("trade_state").equals("SUCCESS")) {
					result = new Result(true, "支付成功");
					orderService.updateStatu(out_trade_no, map.get("transaction_id"));
					break;
				}else {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					x++;
					//五分之后就不再使用
					if (x >= 100) {
						result = new Result(false, "支付超时");
						break;
					}
					
				}
			}

			// 线程睡眠

		}
		System.out.println(result.getMessage());
		return result;

	}

}
