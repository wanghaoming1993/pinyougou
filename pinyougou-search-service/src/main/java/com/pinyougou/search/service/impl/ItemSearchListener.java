package com.pinyougou.search.service.impl;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Component
public class ItemSearchListener implements MessageListener {
	@Autowired
	private ItemSearchService ItemSearchService;
	

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		TextMessage textMessage=(TextMessage)message;
		
		try {
			String text = textMessage.getText();
			List<TbItem> list = JSON.parseArray(text,TbItem.class);
			System.out.println("获取的内容:"+list);
			ItemSearchService.saveSKU(list);
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

}
