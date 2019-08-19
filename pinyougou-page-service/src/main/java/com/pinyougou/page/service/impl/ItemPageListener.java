package com.pinyougou.page.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;
@Component
public class ItemPageListener implements MessageListener {

	@Autowired
	private ItemPageService itemPageService;
	
	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		TextMessage textMessage=(TextMessage)message;
		try {
			Long id=Long.parseLong(textMessage.getText());
			System.out.println("id的值为"+id);
			boolean b = itemPageService.genItemHtml(id);
			System.out.println("生成文件是"+b);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
