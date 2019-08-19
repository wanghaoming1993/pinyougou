package com.pinyougou.manager.controller;

import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

/**
 * controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Autowired
	private JmsTemplate jmsTemplate;//jms模板

	@Autowired
	private Destination queueSolrDestination;//中间件目的，点到点，更新solr库

	@Autowired
	private Destination queueSolrDeleteDestination;//中间件目的地，点到点，删除solr库的索引

	@Autowired
	private Destination topicPageDestination;//中间件目的地，发送订阅，生成静态页面
	
	@Autowired
	private Destination topicPageDeleteDestination;//发送订阅 删除静态页面的消息

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll() {
		return goodsService.findAll();
	}

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows) {
		return goodsService.findPage(page, rows);
	}

	/**
	 * 修改
	 * 
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods) {
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	/**
	 * 获取实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id) {
		return goodsService.findOne(id);
	}

	/**
	 * 批量删除
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long[] ids) {
		try {
			goodsService.delete(ids);
			if (ids != null && ids.length > 0) {
				// itemSearchService.deleteSKUByIds(ids);
				jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						// TODO Auto-generated method stub
						return session.createObjectMessage(ids);
					}
				});

			}
			if (ids != null && ids.length > 0) {
				jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						// TODO Auto-generated method stub
						return session.createObjectMessage(ids);
					}
				});
				
				
			}	
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * 
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
		return goodsService.findPage(goods, page, rows);
	}

	@RequestMapping("updateStatus")
	public Result updateStatus(Long[] selectIds, String status) {
		try {
			goodsService.updateStatus(selectIds, status);
			if (status.equals("1")) {
				List<TbItem> list = goodsService.findSKUBySPUIdAndStatus(selectIds, status);
				if (list != null && list.size() > 0) {
					
					//向中间件发送存数据到索引库solr
					// itemSearchService.saveSKU(list);
					final String jsonString = JSON.toJSONString(list);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {

						@Override
						public Message createMessage(Session session) throws JMSException {
							// TODO Auto-generated method stub
							return session.createTextMessage(jsonString);
						}
					});

				}
			}
			//通过id向中间件发送生成静态页面的消息
			for (final Long id : selectIds) {
				jmsTemplate.send(topicPageDestination, new MessageCreator() {

					@Override
					public Message createMessage(Session session) throws JMSException {
						// TODO Auto-generated method stub
						return session.createTextMessage(id + "");
					}
				});
			}

			return new Result(true, "修改成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}
	/**
	* 测试方法
	* 
	* @param goodsId
	* @RequestMapping("genItemHtml") public void genItemHtml(Long goodsId) {
	* itemPageService.genItemHtml(goodsId); }
	*/

}
