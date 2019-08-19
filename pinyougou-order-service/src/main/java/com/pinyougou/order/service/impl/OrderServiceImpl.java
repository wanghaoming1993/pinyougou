package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbPayLogMapper payLogMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {

		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		List<Long> list = new ArrayList<Long>();
		// 1.从redis中获取到购物车资料
		String cartListString = (String) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		List<Cart> cartList = JSON.parseArray(cartListString, Cart.class);
		double fee=0;
		// 2.遍历集合将每个购物里面的对象存入对应的订单对象中
		for (Cart cart : cartList) {
			// 新创建一个新的订单，不要将旧订单存入
			TbOrder tbOrder = new TbOrder();
			long orderId = idWorker.nextId();
			System.out.println(orderId);
			tbOrder.setOrderId(orderId);
			tbOrder.setPaymentType(order.getPaymentType());
			tbOrder.setStatus("1");
			tbOrder.setCreateTime(new Date());
			tbOrder.setUpdateTime(new Date());
			tbOrder.setUserId(order.getUserId());
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiver(order.getReceiver());
			tbOrder.setSourceType(order.getSourceType());
			tbOrder.setSellerId(cart.getSellerName());

			double money = 0;
			for (TbOrderItem orderItem : cart.getOrderItems()) {
				orderItem.setOrderId(orderId);
				orderItem.setId(idWorker.nextId());// 雪花算法，分布式id 即解决不同表 id相等的情况uuid redis步长， oracle序列
				money += orderItem.getTotalFee().doubleValue();
				fee+=orderItem.getTotalFee().doubleValue();
				orderItemMapper.insert(orderItem);
			}
			System.out.println(money);
			tbOrder.setPayment(new BigDecimal(money));
			// 实付金额：tbOrder.setPayment(payment);
			list.add(orderId);
			System.out.println(list);
			orderMapper.insert(tbOrder);
		}
		// 如果是微信支付的话
		if ("1".equals(order.getPaymentType())) {

			TbPayLog paylog = new TbPayLog();
			paylog.setUserId(order.getUserId());
			paylog.setOutTradeNo(idWorker.nextId() + "");// 商户订单号
			paylog.setCreateTime(new Date());
			paylog.setTradeState("0");// 交易状态 未成功
			paylog.setOrderList(list.toString().replace("[", "").replace("]", ""));// 订单列表
			paylog.setTotalFee((long) (fee*100));
			paylog.setPayType("1");
			payLogMapper.insert(paylog);
			// 将对象存入redis中
			redisTemplate.boundHashOps("payLog").put(order.getUserId(), paylog);
		}

		// 3.删除redis储存的数据
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());

	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order) {
		orderMapper.updateByPrimaryKey(order);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id) {
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			orderMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbOrderExample example = new TbOrderExample();
		Criteria criteria = example.createCriteria();

		if (order != null) {
			if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
				criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
			}
			if (order.getPostFee() != null && order.getPostFee().length() > 0) {
				criteria.andPostFeeLike("%" + order.getPostFee() + "%");
			}
			if (order.getStatus() != null && order.getStatus().length() > 0) {
				criteria.andStatusLike("%" + order.getStatus() + "%");
			}
			if (order.getShippingName() != null && order.getShippingName().length() > 0) {
				criteria.andShippingNameLike("%" + order.getShippingName() + "%");
			}
			if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
				criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
			}
			if (order.getUserId() != null && order.getUserId().length() > 0) {
				criteria.andUserIdLike("%" + order.getUserId() + "%");
			}
			if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
				criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
			}
			if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
				criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
			}
			if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
				criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
			}
			if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
				criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
			}
			if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
				criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
			}
			if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
				criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
			}
			if (order.getReceiver() != null && order.getReceiver().length() > 0) {
				criteria.andReceiverLike("%" + order.getReceiver() + "%");
			}
			if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
				criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
			}
			if (order.getSourceType() != null && order.getSourceType().length() > 0) {
				criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
			}
			if (order.getSellerId() != null && order.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + order.getSellerId() + "%");
			}

		}

		Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 从redis取payLog的方法
	 */
	@Override
	public TbPayLog getPayLogByRedis(String username) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(username);
	}

	@Override
	public void updateStatu(String out_trade_no,String transaction_id) {
		// TODO Auto-generated method stub
		//1.是修改数据库的数据，不是修改redis中的数据
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("2");
		payLog.setTransactionId(transaction_id);
		payLogMapper.updateByPrimaryKey(payLog);
		//2.获取订单数据修改状态
		String orderList = payLog.getOrderList();
		String[] orderArray = orderList.split(",");
		for(String orderId:orderArray) {
			if (orderId!=null) {
				TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
				order.setStatus("2");
				order.setPaymentTime(new Date());
				orderMapper.updateByPrimaryKey(order);
			}	
			
		}
		//3.删除redis中数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}


}
