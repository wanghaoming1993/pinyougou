package com.pinyougou.manager.controller;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("brand")
public class BrandController {

	@Reference
	private BrandService brandService;

	@RequestMapping("findAll")
	public List<TbBrand> findAll() {
		return brandService.findAll();
	}

	@RequestMapping("findPage")
	public PageResult findPage(@RequestBody TbBrand tbBrand,int page, int pageSize) {
		PageResult pageResult = brandService.findPage(tbBrand,page, pageSize);
		return pageResult;
	}

	@RequestMapping("save")
	public Result save(@RequestBody TbBrand tbBrand) {

		Result result = new Result();

		if (tbBrand.getId() != null) {
			try {
				brandService.update(tbBrand);
				result.setSuccess(true);
				result.setMessage("存储成功");
			} catch (Exception e) {
				e.printStackTrace();
				result.setSuccess(false);
				result.setMessage("存储失败");
			}
			return result;
		}

		try {
			brandService.save(tbBrand);
			result.setSuccess(true);
			result.setMessage("存储成功");
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.setMessage("存储失败");
		}
		return result;
	}

	@RequestMapping("findOne")
	public TbBrand findOne(Long id) {
		TbBrand tbBrand = brandService.findOne(id);
		return tbBrand;
	}
	
	@RequestMapping("dele")
	public Result dele(Long[] ids) {
		Result result = new Result();
		try {
			brandService.delete(ids);
			result.setSuccess(true);
			result.setMessage("删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.setMessage("删除失败");
		}
		return result;
	}
	
	
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
	return brandService.selectOptionList();
	}
	
	

}
