package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {
	
	@Autowired
	private TbBrandMapper tbBrandMapper;
	/**
	 * 查询 所有的品牌
	 */
	@Override
	public List<TbBrand> findAll() {
		// TODO Auto-generated method stub
		return tbBrandMapper.selectByExample(null);
	}
	@Override
	public PageResult findPage(TbBrand tbBrand,int pageNum, int pageSize) {
		PageHelper.startPage(pageNum,pageSize);
		
		
		//封装了增加条件
		TbBrandExample example=new TbBrandExample();
		Criteria criteria = example.createCriteria();
		if (tbBrand.getName()!=null&&tbBrand.getName().length()>0) {
			criteria.andNameLike(tbBrand.getName());
		}
		if (tbBrand.getFirstChar()!=null&&tbBrand.getFirstChar().length()>0) {
			criteria.andFirstCharLike(tbBrand.getFirstChar());
		}
		
		
		Page<TbBrand> page=(Page<TbBrand>)tbBrandMapper.selectByExample(example);
		return new PageResult(page.getTotal(),page.getResult()) ;
	}
	@Override
	public void save(TbBrand tbBrand) {
		tbBrandMapper.insert(tbBrand);
	}
	@Override
	public TbBrand findOne(Long id) {
		// TODO Auto-generated method stub
		return tbBrandMapper.selectByPrimaryKey(id);
	}
	@Override
	public void update(TbBrand tbBrand) {
		// TODO Auto-generated method stub
		tbBrandMapper.updateByPrimaryKey(tbBrand);
	}
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids) {
			tbBrandMapper.deleteByPrimaryKey(id);
		}
		
	}
	
	public List<Map> selectOptionList(){
		
		return tbBrandMapper.selectOptionList();
	}

}
