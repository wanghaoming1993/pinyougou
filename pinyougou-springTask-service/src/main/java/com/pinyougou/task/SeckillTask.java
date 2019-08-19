package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

@Component
public class SeckillTask {
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Scheduled(cron = "5/20 * * * * ?")
	public void updateRedisFromDB() {
		// 查询redis中，然后排除redis已经存在的值，把新的值更新到redis数据库中
		
		List ids = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
		

		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");
		criteria.andStartTimeLessThanOrEqualTo(new Date());
		criteria.andEndTimeGreaterThanOrEqualTo(new Date());
		criteria.andNumGreaterThan(0);
		if (ids.size()>0) {
			criteria.andIdNotIn(ids);
		}
		List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
		// 将seckillGoods的键值对存入，键是其goodId，值为其整个对象
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			
		}
	
	}
	
	@Scheduled(cron="* * * * * ?")
	public void outDateGoods() {
		//删除过期的
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		for(TbSeckillGoods seckillGoods:seckillGoodsList) {
			if (seckillGoods.getEndTime().getTime()-new Date().getTime()<0) {
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
				redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
				System.out.println("删除的id为"+seckillGoods.getId());
			}
		}
	}
	
	
	
}
