package com.pinyougou.cart.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;
import util.CookieUtil;

@RequestMapping("cart")
@RestController
public class CartController {

	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Reference
	private CartService cartService;

	@RequestMapping("addCart")
	@CrossOrigin(origins ="http://localhost:9085")
	public Result addCart(Long itemId, Integer num) {
		try {
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			System.out.println(username);
			// 查询cookies里是否有购物车
			List<Cart> cartList = findCartList();
			// 调用方法把数据重新获取购物车对象集合
			List<Cart> cartList2 = cartService.addGoodsToCartList(cartList, itemId, num);
			// 把数据重新储存到购物车中
			if (username.equals("anonymousUser")) {
				String cartListString = JSON.toJSONString(cartList2);
				CookieUtil.setCookie(request, response, "cartList", cartListString, 3600 * 24, "UTF-8");
			}else {
				cartService.SaveCartListToRedis(cartList2, username);
			}
			return new Result(true, "存入购物车成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "存入错误");
		}

	}

	@RequestMapping("findCartList")
	public List<Cart> findCartList() {

		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if (cartListString == null || cartListString.equals("")) {
			cartListString = "[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		
		if (username.equals("anonymousUser")) {
		
			System.out.println("从cookie中取");
			return cartList_cookie;

		}else {
			System.out.println("从redis中取");
			List<Cart> cartList_redis = cartService.findCartListByRedis(username);
			if (cartList_redis==null) {
				cartList_redis=new ArrayList<Cart>();
			}
			
			//存在cookie，本地
			if (cartList_cookie.size()>0) {
				cartList_redis = cartService.mergaList(cartList_cookie,cartList_redis);
				//清除本地cookie值
				CookieUtil.deleteCookie(request, response, "cartList");
				
				cartService.SaveCartListToRedis(cartList_redis, username);
				
			}
			//如果有本地的话就合并，如果没有就放回从redis中查找出的
			return cartList_redis;
		}
	}

}
