package com.cn.author.common.universal;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cn.author.common.response.JsonResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**   
 *
 * <p>说明： 用户API接口层</P>
 * @version: 0.0.1-SNAPSHOT
 * @author: HuangYong
 *
 */
public class BaseController<S extends IService<T>,T>{
	
	@Autowired
    protected S baseService;

	protected JsonResult<T> result = new JsonResult<T>();
	/**
	 * @explain 查询对象  <swagger GET请求>
	 * @param   id 对象参数
	 * @return  JsonResult
	 * @author  HuangYong
	 * @time    2019年4月9日
	 */
	@GetMapping("/getById/{id}")
	@ApiOperation(value = "获取对象", notes = "作者：HuangYong")
	@ApiImplicitParam(paramType="path", name = "id", value = "对象id", required = true, dataType = "Long")
	public JsonResult<T> getUserById(@PathVariable("id")Long id){
		T obj=baseService.getById(id);
		if (null!=obj ) {
			 result.success(obj);
		}else {
			 result.error("查询对象不存在！");
		}
		return result;
	}
	
	/**
	 * @explain 删除对象
	 * @param   id 对象参数
	 * @return  JsonResult
	 * @author  HuangYong
	 * @time    2019年4月9日
	 */
	@PostMapping("/deleteById")
	@ApiOperation(value = "删除", notes = "作者：HuangYong")
	@ApiImplicitParam(paramType="query", name = "id", value = "对象id", required = true, dataType = "Long")
	public JsonResult<T> deleteById(Long id){
		JsonResult<T> result=new JsonResult<T>();
		T obj=baseService.getById(id);
		if (null!=obj) {
		  boolean rsg = baseService.removeById(id);
		  if (rsg) {
			    result.success("删除成功");
		  }else {
			   result.error("删除失败！");
		  }
		}else {
			 result.error("删除的对象不存在！");
		}
		return result;
	}
	
	/**
	 * @explain 添加
	 * @param   entity 对象参数
	 * @return  Boolean
	 * @author  HuangYong
	 * @time    2019年4月9日
	 */
	@PostMapping("/insert")
	@ApiOperation(value = "添加", notes = "作者：HuangYong")
	public JsonResult<T> insert(@RequestBody T entity){
		JsonResult<T> result=new JsonResult<T>();
		if (null!=entity) {
			boolean rsg = baseService.save(entity);
			if (rsg) {
				  result.success("添加成功");
			  }else {
				  result.error("添加失败！");
			  }
		}else {
			result.error("请传入正确参数！");
		}
		return result;
	}
	
	/**
	 * @explain 修改
	 * @param   entity 对象参数
	 * @return  Boolean
	 * @author  HuangYong
	 * @time    2019年4月9日
	 */
	@PostMapping("/update")
	@ApiOperation(value = "修改", notes = "作者：HuangYong")
	public JsonResult<T> update(@RequestBody T entity){
		JsonResult<T> result=new JsonResult<T>();
		if (null!=entity) {
			boolean rsg = baseService.updateById(entity);
			if (rsg) {
				  result.success("修改成功");
			  }else {
				  result.error("修改失败！");
			  }
		}else {
			result.error("请传入正确参数！");
		}
		return result;
	}
	
	/**
	 * @explain 分页条件查询用户   
	 * @param   param 对象参数
	 * @return  PageInfo<User>
	 * @author  HuangYong
	 * @time    2019年5月20日
	 */
	@GetMapping("/getPages")
	@ApiOperation(value = "分页查询", notes = "分页查询返回[IPage<T>],作者：黄勇")
	public JsonResult<IPage<T>> getUserPages(@RequestBody PageParam<T> param){
		JsonResult<IPage<T>> returnPage=new JsonResult<IPage<T>>();
		Page<T> page=new Page<T>(param.getPageNum(),param.getPageSize());
		QueryWrapper<T> queryWrapper =new QueryWrapper<T>();
		queryWrapper.setEntity(param.getParam());
		//分页数据
		IPage<T> pageData=baseService.page(page, queryWrapper);
		returnPage.success(pageData);
		
		return returnPage;
	}
}
