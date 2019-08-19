package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojogroup.Cart;

public interface CartService {

	/**
	 * 查看详细页中 传过来itemid,和商品购物车列表，返回一个新的购物车列表
	 * 未登录的情况下
	 * @param cartList
	 * @param id
	 * @param num
	 * @return
	 */
	public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);
	
	public List<Cart> findCartListByRedis(String username);
	
	public void SaveCartListToRedis(List<Cart> cartList,String username);
	
	public List<Cart> mergaList(List<Cart> list1,List<Cart> list2);
	
}
