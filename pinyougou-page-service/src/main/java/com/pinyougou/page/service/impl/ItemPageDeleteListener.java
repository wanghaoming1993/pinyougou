package com.pinyougou.page.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;

@Component
public class ItemPageDeleteListener implements MessageListener {
	
	@Autowired
	private ItemPageService itemPageService;

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		ObjectMessage objectMessage=(ObjectMessage)message;
		try {
			Long[] ids=(Long[]) objectMessage.getObject();
			System.out.println("生产者发来的消息"+ids);
			boolean b = itemPageService.deletePage(ids);
			System.out.println("删除页面成功与否"+b);
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
