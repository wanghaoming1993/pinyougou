package com.pinyougou.search.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.search.service.ItemSearchService;


/**
 * 删除的消息监听
 * @author Administrator
 *
 */
@Component
public class ItemSearchDeleteListener implements MessageListener {
	
	@Autowired
	private ItemSearchService itemSearchService;

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		ObjectMessage objectMessage=(ObjectMessage)message;
		
		try {
			Long[] goodsid=(Long[]) objectMessage.getObject();
			System.out.println("获取到的id为"+goodsid);
			itemSearchService.deleteSKUByIds(goodsid);
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
