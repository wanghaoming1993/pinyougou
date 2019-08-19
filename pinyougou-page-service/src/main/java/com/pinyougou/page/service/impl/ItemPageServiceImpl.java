package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.template.Configuration;
import freemarker.template.Template;



/**
 * 生成静态页面的方法
 * @author Administrator
 *
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {

	@Autowired
	private FreeMarkerConfig freemarkerConfig;
	@Value("${pagedir}")
	private String pagedir;
	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbItemMapper itemMapper;

	/**
	 * 通过此方法 可以通过访问Tomcat的内容 将数据生成到html上 html上就可以直接采用各种方式将数据显示出来
	 */
	@Override
	public boolean genItemHtml(Long goodsId) {
		// TODO Auto-generated method stub
		try {
			Configuration configuration = freemarkerConfig.getConfiguration();
			Template template = configuration.getTemplate("item.ftl");

			
			Writer out = new FileWriter(new File(pagedir + goodsId + ".html"));
			Map map = new HashMap();
			//获取goods和goodsDesc和ItemCat
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			TbItemCat category1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id());
			TbItemCat category2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id());
			TbItemCat category3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
			
			
			//真实展示的是sku 现在将sku展示到静态页面上
			TbItemExample example=new TbItemExample();
			Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);
			criteria.andStatusEqualTo("1");
			//设置为默认排序 参数为排序字段
			example.setOrderByClause("is_default desc");
			List<TbItem> itemList = itemMapper.selectByExample(example);
			
			map.put("goods", goods);
			map.put("goodsDesc", goodsDesc);
			map.put("category1", category1);
			map.put("category2", category2);
			map.put("category3", category3);
			map.put("itemList", itemList);
			
			template.process(map, out);
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	/**
	 * 删除页面的方法
	 * @param ids
	 * @return
	 */
	public boolean deletePage(Long[] ids) {
		try {
			for(Long id:ids) {
				new File(pagedir + id + ".html").delete();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
