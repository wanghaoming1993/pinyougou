package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillGoodsService;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;

@RestController
@RequestMapping("pay")
public class PayController {
	@Reference(timeout = 10000)
	private WeixinPayService weixinPayService;
	@Reference
	private SeckillOrderService seckillOrderService;

	@Reference
	private SeckillGoodsService seckillGoodsService;

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
		TbSeckillOrder seckillOrder = seckillOrderService.findSeckillOrderByRedis(username);
		if (seckillOrder == null) {
			return new HashMap();
		}
		return weixinPayService.creatNative(seckillOrder.getSeckillId() + "", seckillOrder.getMoney() + "");
	}

	@RequestMapping("quertForPayStatu")
	public Result quertForPayStatu(String out_trade_no) {

		Result result = null;
		int x = 0;
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		while (true) {

			Map<String, String> map = weixinPayService.queryForStatu(out_trade_no);

			if (map == null) {
				result = new Result(false, "支付出错");
				break;
			} else {
				if (map.get("trade_state").equals("SUCCESS")) {
					result = new Result(true, "支付成功");
					seckillOrderService.saveOrder(Long.valueOf(out_trade_no), username, map.get("transaction_id"));
					break;
				} else {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					x++;
					// 五分之后就不再使用
					if (x >= 100) {

						Map closeMap = weixinPayService.closePay(out_trade_no);

						if (!"SUCCESS".equals(closeMap.get("return_code"))) {
							if ("ORDERPAID".equals(closeMap.get("err_code"))) {
								result = new Result(true, "支付成功");
								seckillOrderService.saveOrder(Long.valueOf(out_trade_no), username,
										map.get("transaction_id"));
								break;

							}

						} else {
							result = new Result(false, "支付超时");
							seckillGoodsService.deleteUserOrder(username, Long.valueOf(out_trade_no));
							break;
						}

					}

				}

			}
		}

		System.out.println(result.getMessage());
		seckillGoodsService.deleteUserOrder(username, Long.valueOf(out_trade_no));
		return result;

	}

}
