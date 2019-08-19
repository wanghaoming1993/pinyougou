package com.pinyougou.seckill.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillGoodsService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id) {
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			seckillGoodsMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();

		if (seckillGoods != null) {
			if (seckillGoods.getTitle() != null && seckillGoods.getTitle().length() > 0) {
				criteria.andTitleLike("%" + seckillGoods.getTitle() + "%");
			}
			if (seckillGoods.getSmallPic() != null && seckillGoods.getSmallPic().length() > 0) {
				criteria.andSmallPicLike("%" + seckillGoods.getSmallPic() + "%");
			}
			if (seckillGoods.getSellerId() != null && seckillGoods.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + seckillGoods.getSellerId() + "%");
			}
			if (seckillGoods.getStatus() != null && seckillGoods.getStatus().length() > 0) {
				criteria.andStatusLike("%" + seckillGoods.getStatus() + "%");
			}
			if (seckillGoods.getIntroduction() != null && seckillGoods.getIntroduction().length() > 0) {
				criteria.andIntroductionLike("%" + seckillGoods.getIntroduction() + "%");
			}

		}

		Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 查询秒杀的商品,然后缓存起来
	 */
	@Override
	public List<TbSeckillGoods> findSeckillGoods() {
		// 获取sekillGoods中的值
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		if (seckillGoodsList == null || seckillGoodsList.size() == 0) {
			TbSeckillGoodsExample example = new TbSeckillGoodsExample();
			Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo("1");
			criteria.andStartTimeLessThanOrEqualTo(new Date());
			criteria.andEndTimeGreaterThanOrEqualTo(new Date());
			criteria.andNumGreaterThan(0);
			seckillGoodsList = seckillGoodsMapper.selectByExample(example);
			// 将seckillGoods的键值对存入，键是其goodId，值为其整个对象
			for (TbSeckillGoods seckillGoods : seckillGoodsList) {
				redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			}

		}
		System.out.println(seckillGoodsList);
		return seckillGoodsList;

	}

	@Override
	public TbSeckillGoods findseckillGoodsByRedis(Long goodIds) {
		// 从redis中取出数据
		return (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(goodIds);

	}
	//提交订单
	@Override
	public void sumbitOrder(String userId, Long orderId) {
		//实现逻辑：1.通过前端传来的orderId去redis中查询是否有该订单，如果没有返回没有该商品
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(orderId);
		if (seckillGoods==null) {
			throw new RuntimeException("商品不存在");
		}
//		2.如果有，判断该商品的stockCount是否大于零，如果订单已经抢完
		if (seckillGoods.getStockCount()<=0) {
			throw new RuntimeException("商品已经抢完了");
		}
		
		//3.如果有，将redis中的库存减一，重新存储到redis中，然后创建该秒杀订单，以用户名为key，以订单有对象,
		seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
		redisTemplate.boundHashOps("seckillGoods").put(orderId, seckillGoods);
		
		//4.如果此订单完成之后，秒杀商品库存为零，就立即同步更新到数据库,同时删除redis中该对象
		if (seckillGoods.getStockCount()==0) {
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
			redisTemplate.boundHashOps("seckillGoods").delete(orderId);
		}
		//5.创建秒杀订单对象
		TbSeckillOrder seckillOrder=new TbSeckillOrder();
		seckillOrder.setId(idWorker.nextId());
		seckillOrder.setSeckillId(orderId);
		seckillOrder.setMoney(new BigDecimal(seckillGoods.getCostPrice().doubleValue()*100));
		seckillOrder.setUserId(userId);
		seckillOrder.setSellerId(seckillGoods.getSellerId());
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setStatus("0");//未支付
		redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
	}

	@Override
	public void deleteUserOrder(String userId, Long goodsId) {
		//实现逻辑
		TbSeckillOrder seckillOrder=(TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder!=null&&seckillOrder.getSeckillId().longValue()==goodsId) {
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
		}
		
		TbSeckillGoods seckillGoods=(TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(goodsId);
		if (seckillGoods==null) {
			seckillGoods= seckillGoodsMapper.selectByPrimaryKey(goodsId);
			seckillGoods.setStockCount(1);
			redisTemplate.boundHashOps("seckillGoods").put(goodsId, seckillGoods);
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
		}
		seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
		redisTemplate.boundHashOps("seckillGoods").put(goodsId, seckillGoods);	
	}

}
