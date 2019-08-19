package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

public interface BrandService {

	List<TbBrand> findAll();
	PageResult findPage(TbBrand tbBrand,int page,int pageSize);
	void save(TbBrand tbBrand);
	TbBrand findOne(Long id);
	void update(TbBrand tbBrand);
	void delete(Long[] ids);
	List<Map> selectOptionList();
}
