package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {

	
	/**
	 * 传入订单号和金额，生成二维码
	 * @param out_trade_no
	 * @param total_fee
	 * @return
	 */
	public Map creatNative(String out_trade_no,String total_fee);
	
	
	/**
	 * 传入商户订单号  查询支付状态
	 * @param out_trade_no
	 * @return
	 */
	public Map queryForStatu(String out_trade_no);
	
	
	public Map closePay(String out_trade_no);
	
}
