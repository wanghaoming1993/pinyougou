package com.pinyougou.user.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;

import entity.PageResult;
import entity.Result;
import util.PhoneFormatCheckUtils;

/**
 * controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll() {
		return userService.findAll();
	}

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows) {
		return userService.findPage(page, rows);
	}

	/**
	 * 增加
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user, String code) {
		if (code == null) {
			return new Result(false, "请输入验证码");
		}

		boolean b = userService.checkSmsCode(user.getPhone(), code);

		if (b) {

			try {
				userService.add(user);
				return new Result(true, "增加成功");
			} catch (Exception e) {
				e.printStackTrace();
				return new Result(false, "增加失败");
			}
		}else {
			return new Result(false,"验证码错误，请重新输入或者获取");
		}

	}

	/**
	 * 修改
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user) {
		try {
			userService.update(user);
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
	public TbUser findOne(Long id) {
		return userService.findOne(id);
	}

	/**
	 * 批量删除
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			userService.delete(ids);
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
	public PageResult search(@RequestBody TbUser user, int page, int rows) {
		return userService.findPage(user, page, rows);
	}

	@RequestMapping("sendSms")
	public Result sendSms(String phone) {
		if (phone == null) {
			return new Result(false, "请输入手机号");
		}

		if (PhoneFormatCheckUtils.isPhoneLegal(phone) == false) {
			return new Result(false, "手机号式不正确");
		}

		try {
			userService.sendSms(phone);
			return new Result(true, "获取成功");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "获取验证码失败");
		}
	}
	
	@RequestMapping("login")
	public Map login() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Map map=new HashMap();
		map.put("loginName", username);
		return map;
	}
}
