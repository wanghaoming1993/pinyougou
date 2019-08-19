package com.pinyougou.cart.service.impl;

import static org.hamcrest.CoreMatchers.nullValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	// 重体现重要的业务流程，不重要的用方法抽出
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

		// 1.通过skuid找出对应的sku列表（判断sku列表是否不存在，判断sku的状态是否合法，里面存在延时，可能添加购物车过程中，商品的状态被改成不合法）
		TbItem item = itemMapper.selectByPrimaryKey(itemId);

		if (item == null) {
			throw new RuntimeException("该商品不存在");
		}
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("商品不合法");
		}
		// 2.获取商家id
		String sellerId = item.getSellerId();
		// 3.通过商家id在集合查询cartList的是否存在商家
		
		Cart cart = searchCartBySellerId(cartList,sellerId);
		//4.1判断是否为空，该商品在购物车中没有对应的商家
		if (cart==null) {
			Cart cart2 = createCart( item, itemId,num);
			cartList.add(cart2);
		}else {
		//4.2使用itemId查询是否可以使用itemId能在该商家查找出商品
			TbOrderItem orderItem = searchOrderItemByItem(cart,itemId);
			if (orderItem==null) {
		//5.1没有找出来,新创建，然后用原来的集合加入
				TbOrderItem tbOrderItem = creatTbOrderItem(item,itemId,num);
				cart.getOrderItems().add(tbOrderItem);
			}else {
		//5.2如果找出来，在原基础进行修改数量和总价
				num=orderItem.getNum()+num;
				orderItem.setNum(num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*num));
				if (num<=0) {
					cart.getOrderItems().remove(orderItem);
				}
				
				if (cart.getOrderItems().size()<=0) {
					cartList.remove(cart);
				}
				
			}
			
		}
		
		return cartList;	
	}
/**
 * 使用sellerId在购物车中查找是否有符合的商家，有就返回Cart,没有就返回空null
 * @param cartList
 * @param sellerId
 * @return
 */
	public Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;

	}
	
	public Cart createCart(TbItem item,Long itemId,Integer num) {
		Cart cart = new Cart();
		cart.setSellerId(item.getSellerId());
		cart.setSellerName(item.getSeller());
		TbOrderItem orderItem=new TbOrderItem();
		TbOrderItem tbOrderItem = creatTbOrderItem(item,itemId,num);
		List<TbOrderItem> orderItemList=new ArrayList<TbOrderItem>();
		orderItemList.add(tbOrderItem);
		cart.setOrderItems(orderItemList);
		return cart;
	}
	/**
	 * 使用itemId在集合查找是否有符合的TbOrderItem，有就返回,没有就返回空
	 * @param cart
	 * @param itemId
	 * @return
	 */
	public TbOrderItem searchOrderItemByItem(Cart cart,Long itemId) {
		for(TbOrderItem orderItem:cart.getOrderItems()) {
			if (orderItem.getItemId().longValue()==itemId) {
				return orderItem;
			}
		}
		return null;
		
		
	}

	/**
	 * 创建新的TbOrderItem;
	 * 
	 * @param item
	 * @param num
	 * @return
	 */
	public TbOrderItem creatTbOrderItem(TbItem item,Long itemId ,Integer num) {
		TbOrderItem tbOrderItem = new TbOrderItem();
		tbOrderItem.setGoodsId(item.getGoodsId());
		tbOrderItem.setItemId(itemId);
		tbOrderItem.setTitle(item.getTitle());
		tbOrderItem.setNum(num);
		tbOrderItem.setPrice(item.getPrice());
		tbOrderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
		tbOrderItem.setSellerId(item.getSellerId());
		tbOrderItem.setPicPath(item.getImage());
		return tbOrderItem;
	}

	@Override
	public List<Cart> findCartListByRedis(String username) {
		String cartListString = (String) redisTemplate.boundHashOps("cartList").get(username);
		List<Cart> list = JSON.parseArray(cartListString, Cart.class);
		return list;
	}

	@Override
	public void SaveCartListToRedis(List<Cart> cartList, String username) {
		String cartListString = JSON.toJSONString(cartList);
		redisTemplate.boundHashOps("cartList").put(username, cartListString);

	}

	/**
	 * 合并两个集合，原理上，将内部的itemId和一个集合合并，可以直接调用上面的方法
	 */
	@Override
	public List<Cart> mergaList(List<Cart> list1, List<Cart> list2) {
		List<Cart> cartList = null;
		for (Cart cart : list1) {
			for (TbOrderItem orderItem : cart.getOrderItems()) {
				cartList = addGoodsToCartList(list2, orderItem.getItemId(), orderItem.getNum());
			}
		}
		return cartList;
	}

}
