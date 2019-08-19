package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {
	@Value("${appid}")
	private String appid;
	@Value("${partner}")
	private String partner;
	@Value("${partnerkey}")
	private String partnerkey;
	@Value("${notifyurl}")
	private String notifyurl;

	/**
	 * 生成二维码
	 */
	@Override
	public Map creatNative(String out_trade_no, String total_fee) {

		// 1.准备参数
		Map map = new HashMap();
		map.put("appid", appid);
		map.put("mch_id", partner);
		map.put("nonce_str", WXPayUtil.generateNonceStr());
		map.put("body", "品优购");
		System.out.println("金额为："+total_fee);
		map.put("out_trade_no", out_trade_no);
		map.put("total_fee", total_fee);
		map.put("spbill_create_ip", "127.0.0.1");
		map.put("notify_url", notifyurl);
		map.put("trade_type", "NATIVE");
		try {
			String wxPayStringXml = WXPayUtil.generateSignedXml(map, partnerkey);
			// 2.提交参数
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			httpClient.setXmlParam(wxPayStringXml);
			httpClient.setHttps(true);
			httpClient.post();
			String returnMapXml = httpClient.getContent();
			System.out.println("返回的内容：" + returnMapXml);

			// 3.返回值
			Map<String, String> returnMap = WXPayUtil.xmlToMap(returnMapXml);

			Map map2 = new HashMap();
			map2.put("out_trade_no", out_trade_no);
			map2.put("total_fee", total_fee);
			map2.put("code_url", returnMap.get("code_url"));
			return map2;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 调用微信支付api,查询支付状态,返回结果，出错返回null
	 */
	@Override
	public Map queryForStatu(String out_trade_no) {
		Map map = new HashMap();
		// 1.设置参数
		map.put("appid",appid );
		map.put("mch_id", partner);
		map.put("out_trade_no",out_trade_no );
		map.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			String paramStringXml = WXPayUtil.generateSignedXml(map, partnerkey);
			// 2.传递参数
			HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			httpClient.setHttps(true);
			httpClient.setXmlParam(paramStringXml);
			httpClient.post();
			String resultXmlString = httpClient.getContent();
			System.out.println("获取的内容是："+resultXmlString);
			// 3.返回内容
			Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXmlString);
			return resultMap;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
/**
 * 关闭订单的方法
 */
	@Override
	public Map closePay(String out_trade_no) {
		Map map = new HashMap();
		// 1.设置参数
		map.put("appid",appid );
		map.put("mch_id", partner);
		map.put("out_trade_no",out_trade_no );
		map.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			String paramStringXml = WXPayUtil.generateSignedXml(map, partnerkey);
			// 2.传递参数
			HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(paramStringXml);
			httpClient.post();
			String resultXmlString = httpClient.getContent();
			System.out.println("获取的内容是："+resultXmlString);
			// 3.返回内容
			Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXmlString);
			return resultMap;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
