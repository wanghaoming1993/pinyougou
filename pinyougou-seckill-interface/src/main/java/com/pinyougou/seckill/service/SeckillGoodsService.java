package com.pinyougou.seckill.service;
import java.util.List;
import com.pinyougou.pojo.TbSeckillGoods;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillGoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillGoods seckillGoods);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillGoods seckillGoods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillGoods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum,int pageSize);
	
	
	//第一次从数据库中查询数据 第二次从缓存中查询Goods数据
	public List<TbSeckillGoods> findSeckillGoods();
	
	//获取单个goods的详细情况,从缓存中取
	public TbSeckillGoods findseckillGoodsByRedis(Long goodId);
	
	public void sumbitOrder(String userId,Long orderId);
	
	public void deleteUserOrder(String userId,Long goodsId);
	
}
