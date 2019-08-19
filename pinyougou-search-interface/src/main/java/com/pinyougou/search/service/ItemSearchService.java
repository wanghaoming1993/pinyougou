package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbItem;

public interface ItemSearchService {

	public Map search(Map searchMap);
	
	public void saveSKU(List<TbItem> list);
	
	public void deleteSKUByIds(Long[] ids);
}
