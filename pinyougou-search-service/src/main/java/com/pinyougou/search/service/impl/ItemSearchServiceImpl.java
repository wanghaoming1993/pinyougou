package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {
	@Autowired
	private SolrTemplate solrTemplate;
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public Map search(Map searchMap) {

		Map map = new HashedMap();
		// 对空字符串进行处理
		String keywords = searchMap.get("keywords").toString().replace(" ", "");
		searchMap.put("keywords", keywords);

		// 1.查询全文检索数据库的内容
		searchList(map, searchMap);

		// 2.在结果中存入categoryList集合
		searchCategoryList(map, searchMap);

		// 3.在结果中存入specList,brandList集合
		searchBrandAndSpec(searchMap, map);

		return map;
	}

	/**
	 * 查询全文检索库内容，search方法中的map集合中
	 * 
	 * @param map
	 * @param searchMap
	 */
	private void searchList(Map<String, Object> map, Map searchMap) {
		// 高亮查询器
		HighlightQuery query = new SimpleHighlightQuery();
		// 1.1高亮选项 添加高亮内容对象,设置
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		highlightOptions.setSimplePostfix("</em>");

		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		// 1.2查询关键字，相当于where查询
		query.addCriteria(criteria);
		query.setHighlightOptions(highlightOptions);

		// *************************************************
		// 1.3过滤查询(分类查询)
		if (!"".equals(searchMap.get("category"))) {
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 1.4过滤品牌查询
		if (!"".equals(searchMap.get("brand"))) {
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 1.5过滤规格查询
		Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
		if (searchMap.get("spec") != null) {
			for (String key : specMap.keySet()) {
				String value = specMap.get(key);
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_" + key).is(value);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		// 1.61过滤价格查询
		/*
		 * if(!"".equals(searchMap.get("price"))) { FilterQuery filterQuery=new
		 * SimpleFilterQuery(); String priceString=(String) searchMap.get("price");
		 * String[] price = priceString.split("-"); if (!price[0].equals("0")) {
		 * Criteria filterCriteria=new
		 * Criteria("item_price").greaterThanEqual(price[0]);
		 * filterQuery.addCriteria(filterCriteria); query.addFilterQuery(filterQuery); }
		 * if(!price[1].equals("*")) { Criteria filterCriteria=new
		 * Criteria("item_price").lessThanEqual(price[1]);
		 * filterQuery.addCriteria(filterCriteria); query.addFilterQuery(filterQuery); }
		 * 
		 * }
		 */
		// 1.62过滤价格查询2
		if (!"".equals(searchMap.get("price"))) {
			String priceString = (String) searchMap.get("price");
			String[] price = priceString.split("-");

			FilterQuery filterQuery = new SimpleQuery("item_price:[" + price[0] + " TO " + price[1] + "]");
			query.addFilterQuery(filterQuery);
		}

		// 1.7分页查询
		Integer pageNo = (Integer) searchMap.get("pageNo");
		if (pageNo == null) {
			pageNo = 1;
		}

		Integer pageSize = (Integer) searchMap.get("pageSize");
		if (pageSize == null) {
			pageSize = 50;
		}

		// query中设置页面 不是criteria中
		query.setOffset((pageNo - 1) * pageSize);
		query.setRows(pageSize);

		// 排序
		String sortValue = (String) searchMap.get("sort");
		String sortField = (String) searchMap.get("sortField");
		if (!"".equals(sortValue) && sortValue != null && !sortField.equals("")) {
			if (sortValue.equals("ASC")) {
				Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
				query.addSort(sort);
			}
			if (sortValue.equals("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
				query.addSort(sort);
			}
		}

		// **************************************************
		// 高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

		// 获取高亮入口对象（遍历每条数据的高亮对象

		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();

		for (HighlightEntry<TbItem> entry : entryList) {

			// 获取高亮列表对象 根据域的多少确定内容
			List<Highlight> highlights = entry.getHighlights();

			if (highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {
				// 修改page中item的内容
				TbItem item = entry.getEntity();
				item.setTitle(highlights.get(0).getSnipplets().get(0));
			}
		}

		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());
		map.put("total", page.getTotalElements());

	}

	/**
	 * 分组查询分类
	 * 
	 * @param map
	 * @param searchMap
	 */
	private void searchCategoryList(Map<String, Object> map, Map searchMap) {
		List<Object> categoryList = new ArrayList<Object>();

		Query query = new SimpleQuery("*:*");

		// 根据关键字查询，构造查询条件，条件为前台传来的keywords参数
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");

		// Query中存在分组查询条件
		query.setGroupOptions(groupOptions);

		// 获取分页对象
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		// 获取分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");

		// 获取分组结果入口
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

		for (GroupEntry<TbItem> entry : groupEntries) {
			String groupValue = entry.getGroupValue();
			categoryList.add(groupValue);
		}
		map.put("categoryList", categoryList);
	}

	/**
	 * 讲redis中的brand和spec信息存入map集合中
	 * 
	 * @param searchMap
	 * @param map
	 */
	public void searchBrandAndSpec(Map searchMap, Map<String, Object> map) {
		List<String> list = (List<String>) map.get("categoryList");
		if (list != null && list.size() > 0) {
			String category = (String) searchMap.get("category");
			Long typeId = null;
			if (category == null || "".equals(category)) {
				typeId = (Long) redisTemplate.boundHashOps("itemCatList").get(list.get(0));
			} else {
				typeId = (Long) redisTemplate.boundHashOps("itemCatList").get(category);
			}
			if (typeId != null) {
				List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);

				map.put("specList", specList);
				List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
				map.put("brandList", brandList);

			}
		}
	}

	@Override
	public void saveSKU(List<TbItem> list) {
			solrTemplate.saveBeans(list);
			solrTemplate.commit();
	}

	@Override
	public void deleteSKUByIds(Long[] ids) {
		//通过传过来的spuId删除索引库里面的内容 这种方式针对挑选某种域中的参数来删除值
		Query query=new SimpleQuery("*:*");
		Criteria criteria=new Criteria("item_goodsid").in(Arrays.asList(ids));
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}

	/**
	 * 存SKU到索引库
	 * 
	 * @param list
	 */
	

}
