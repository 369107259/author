package com.cn.author.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.author.common.exception.BusinessException;
import com.cn.author.common.jwt.JwtUtil;
import com.cn.author.common.response.JsonResult;
import com.cn.author.common.utils.ConvertUtils;
import com.cn.author.common.utils.MD5Util;
import com.cn.author.common.utils.system.PermissionDataUtil;
import com.cn.author.system.entity.SysPermission;
import com.cn.author.system.entity.SysPermissionDataRule;
import com.cn.author.system.entity.SysRolePermission;
import com.cn.author.system.model.SysPermissionTree;
import com.cn.author.system.model.TreeModel;
import com.cn.author.system.service.ISysPermissionDataRuleService;
import com.cn.author.system.service.ISysPermissionService;
import com.cn.author.system.service.ISysRolePermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单权限表 前端控制器
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Slf4j
@RestController
@RequestMapping("/sys/permission")
public class SysPermissionController {

	@Autowired
	private ISysPermissionService sysPermissionService;

	@Autowired
	private ISysRolePermissionService sysRolePermissionService;

	@Autowired
	private ISysPermissionDataRuleService sysPermissionDataRuleService;

	/**
	 * 加载数据节点
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JsonResult<List<SysPermissionTree>> list() {
		JsonResult<List<SysPermissionTree>> result = new JsonResult<>();
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag, 0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			List<SysPermissionTree> treeList = new ArrayList<>();
			getTreeList(treeList, list, null);
			result.setResult(treeList);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException("查询失败");
		}
		return result;
	}

	/**
	 * 查询用户拥有的菜单权限和按钮权限（根据TOKEN）
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getUserPermissionByToken", method = RequestMethod.GET)
	public JsonResult<?> getUserPermissionByToken(@RequestParam(name = "token", required = true) String token) {
		JsonResult<JSONObject> result = new JsonResult<JSONObject>();
		try {
			if (ConvertUtils.isEmpty(token)) {
				return JsonResult.fault("TOKEN不允许为空！");
			}
			log.info(" ------ 通过令牌获取用户拥有的访问菜单 ---- TOKEN ------ " + token);
			String username = JwtUtil.getUserName(token);
			List<SysPermission> metaList = sysPermissionService.queryByUser(username);
			PermissionDataUtil.addIndexPage(metaList);
			JSONObject json = new JSONObject();
			JSONArray menujsonArray = new JSONArray();
			this.getPermissionJsonArray(menujsonArray, metaList, null);
			JSONArray authjsonArray = new JSONArray();
			this.getAuthJsonArray(authjsonArray, metaList);
			//查询所有的权限
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag, 0);
			query.eq(SysPermission::getMenuType, 2);
			//query.eq(SysPermission::getStatus, "1");
			List<SysPermission> allAuthList = sysPermissionService.list(query);
			JSONArray allauthjsonArray = new JSONArray();
			this.getAllAuthJsonArray(allauthjsonArray, allAuthList);
			json.put("menu", menujsonArray);
			json.put("auth", authjsonArray);
			json.put("allAuth", allauthjsonArray);
			result.setResult(json);
			result.success("查询成功");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException("查询失败:" + e.getMessage());
		}
		return result;
	}

	/**
	  * 添加菜单
	 * @param permission
	 * @return
	 */
	@RequiresRoles({ "admin" })
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public JsonResult<SysPermission> add(@RequestBody SysPermission permission) {
		JsonResult<SysPermission> result = new JsonResult<SysPermission>();
		try {
			permission = PermissionDataUtil.intelligentProcessData(permission);
			sysPermissionService.addPermission(permission);
			result.success("添加成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException("操作失败");
		}
		return result;
	}

	/**
	  * 编辑菜单
	 * @param permission
	 * @return
	 */
	@RequiresRoles({ "admin" })
	@CacheEvict(value="loginUser_cacheRules", allEntries=true)
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	public JsonResult<SysPermission> edit(@RequestBody SysPermission permission) {
		JsonResult<SysPermission> result = new JsonResult<>();
		try {
			permission = PermissionDataUtil.intelligentProcessData(permission);
			sysPermissionService.editPermission(permission);
			result.success("修改成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException("操作失败");
		}
		return result;
	}

	/**
	  * 删除菜单
	 * @param id
	 * @return
	 */
	@RequiresRoles({ "admin" })
	@CacheEvict(value="loginUser_cacheRules", allEntries=true)
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public JsonResult<SysPermission> delete(@RequestParam(name = "id", required = true) String id) {
		JsonResult<SysPermission> result = new JsonResult<>();
		try {
			sysPermissionService.deletePermission(id);
			sysPermissionService.deletePermRuleByPermId(id);
			result.success("删除成功!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
		return result;
	}

	/**
	  * 批量删除菜单
	 * @param ids
	 * @return
	 */
	@RequiresRoles({ "admin" })
	@CacheEvict(value="loginUser_cacheRules", allEntries=true)
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public JsonResult<SysPermission> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		JsonResult<SysPermission> result = new JsonResult<>();
		try {
			String arr[] = ids.split(",");
			for (String id : arr) {
				if (ConvertUtils.isNotEmpty(id)) {
					sysPermissionService.deletePermission(id);
				}
			}
			result.success("删除成功!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException("删除失败!");
		}
		return result;
	}

	/**
	 * 获取全部的权限树
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public JsonResult<Map<String, Object>> queryTreeList() {
		JsonResult<Map<String, Object>> result = new JsonResult<>();
		// 全部权限ids
		List<String> ids = new ArrayList<>();
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag, 0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			for (SysPermission sysPer : list) {
				ids.add(sysPer.getId());
			}
			List<TreeModel> treeList = new ArrayList<>();
			getTreeModelList(treeList, list, null);

			Map<String, Object> resMap = new HashMap<String, Object>();
			resMap.put("treeList", treeList); // 全部树节点数据
			resMap.put("ids", ids);// 全部树ids
			result.setResult(resMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 异步加载数据节点
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryListAsync", method = RequestMethod.GET)
	public JsonResult<List<TreeModel>> queryAsync(@RequestParam(name = "pid", required = false) String parentId) {
		JsonResult<List<TreeModel>> result = new JsonResult<>();
		try {
			List<TreeModel> list = sysPermissionService.queryListByParentId(parentId);
			if (list == null || list.size() <= 0) {
				throw new BusinessException("未找到角色信息");
			} else {
				result.setResult(list);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * 查询角色授权
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryRolePermission", method = RequestMethod.GET)
	public JsonResult<List<String>> queryRolePermission(@RequestParam(name = "roleId", required = true) String roleId) {
		JsonResult<List<String>> result = new JsonResult<>();
		try {
			List<SysRolePermission> list = sysRolePermissionService.list(new QueryWrapper<SysRolePermission>().lambda().eq(SysRolePermission::getRoleId, roleId));
			result.setResult(list.stream().map(SysRolePermission -> String.valueOf(SysRolePermission.getPermissionId())).collect(Collectors.toList()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 保存角色授权
	 * 
	 * @return
	 */
	@RequestMapping(value = "/saveRolePermission", method = RequestMethod.POST)
	@RequiresRoles({ "admin" })
	public JsonResult<String> saveRolePermission(@RequestBody JSONObject json) {
		long start = System.currentTimeMillis();
		JsonResult<String> result = new JsonResult<>();
		try {
			String roleId = json.getString("roleId");
			String permissionIds = json.getString("permissionIds");
			String lastPermissionIds = json.getString("lastpermissionIds");
			this.sysRolePermissionService.saveRolePermission(roleId, permissionIds, lastPermissionIds);
			result.setMessage("保存成功！");
			log.info("======角色授权成功=====耗时:" + (System.currentTimeMillis() - start) + "毫秒");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException("授权失败！");
		}
		return result;
	}

	private void getTreeList(List<SysPermissionTree> treeList, List<SysPermission> metaList, SysPermissionTree temp) {
		for (SysPermission permission : metaList) {
			String tempPid = permission.getParentId();
			SysPermissionTree tree = new SysPermissionTree(permission);
			if (temp == null && ConvertUtils.isEmpty(tempPid)) {
				treeList.add(tree);
				if (!tree.getIsLeaf()) {
					getTreeList(treeList, metaList, tree);
				}
			} else if (temp != null && tempPid != null && tempPid.equals(temp.getId())) {
				temp.getChildren().add(tree);
				if (!tree.getIsLeaf()) {
					getTreeList(treeList, metaList, tree);
				}
			}

		}
	}

	private void getTreeModelList(List<TreeModel> treeList, List<SysPermission> metaList, TreeModel temp) {
		for (SysPermission permission : metaList) {
			String tempPid = permission.getParentId();
			TreeModel tree = new TreeModel(permission);
			if (temp == null && ConvertUtils.isEmpty(tempPid)) {
				treeList.add(tree);
				if (!tree.getIsLeaf()) {
					getTreeModelList(treeList, metaList, tree);
				}
			} else if (temp != null && tempPid != null && tempPid.equals(temp.getKey())) {
				temp.getChildren().add(tree);
				if (!tree.getIsLeaf()) {
					getTreeModelList(treeList, metaList, tree);
				}
			}

		}
	}
	
	/**
	  *  获取权限JSON数组
	 * @param jsonArray
	 * @param allList
	 */
	private void getAllAuthJsonArray(JSONArray jsonArray, List<SysPermission> allList) {
		JSONObject json = null;
		for (SysPermission permission : allList) {
			json = new JSONObject();
			json.put("action", permission.getPerms());
			json.put("status", permission.getStatus());
			json.put("type", permission.getPermsType());
			json.put("describe", permission.getName());
			jsonArray.add(json);
		}
	}

	/**
	  *  获取权限JSON数组
	 * @param jsonArray
	 * @param metaList
	 */
	private void getAuthJsonArray(JSONArray jsonArray, List<SysPermission> metaList) {
		for (SysPermission permission : metaList) {
			if(permission.getMenuType()==null) {
				continue;
			}
			JSONObject json = null;
			if(permission.getMenuType()==2&&"1".equals(permission.getStatus())) {
				json = new JSONObject();
				json.put("action", permission.getPerms());
				json.put("type", permission.getPermsType());
				json.put("describe", permission.getName());
				jsonArray.add(json);
			}
		}
	}
	/**
	  *  获取菜单JSON数组
	 * @param jsonArray
	 * @param metaList
	 * @param parentJson
	 */
	private void getPermissionJsonArray(JSONArray jsonArray, List<SysPermission> metaList, JSONObject parentJson) {
		for (SysPermission permission : metaList) {
			if (permission.getMenuType() == null) {
				continue;
			}
			String tempPid = permission.getParentId();
			JSONObject json = getPermissionJsonObject(permission);
			if(json==null) {
				continue;
			}
			if (parentJson == null && ConvertUtils.isEmpty(tempPid)) {
				jsonArray.add(json);
				if (!permission.isLeaf()) {
					getPermissionJsonArray(jsonArray, metaList, json);
				}
			} else if (parentJson != null && ConvertUtils.isNotEmpty(tempPid) && tempPid.equals(parentJson.getString("id"))) {
				// 类型( 0：一级菜单 1：子菜单 2：按钮 )
				if (permission.getMenuType() == 2) {
					JSONObject metaJson = parentJson.getJSONObject("meta");
					if (metaJson.containsKey("permissionList")) {
						metaJson.getJSONArray("permissionList").add(json);
					} else {
						JSONArray permissionList = new JSONArray();
						permissionList.add(json);
						metaJson.put("permissionList", permissionList);
					}
					// 类型( 0：一级菜单 1：子菜单 2：按钮 )
				} else if (permission.getMenuType() == 1 || permission.getMenuType() == 0) {
					if (parentJson.containsKey("children")) {
						parentJson.getJSONArray("children").add(json);
					} else {
						JSONArray children = new JSONArray();
						children.add(json);
						parentJson.put("children", children);
					}

					if (!permission.isLeaf()) {
						getPermissionJsonArray(jsonArray, metaList, json);
					}
				}
			}

		}
	}

	private JSONObject getPermissionJsonObject(SysPermission permission) {
		JSONObject json = new JSONObject();
		// 类型(0：一级菜单 1：子菜单 2：按钮)
		if (permission.getMenuType() == 2) {
			//json.put("action", permission.getPerms());
			//json.put("type", permission.getPermsType());
			//json.put("describe", permission.getName());
			return null;
		} else if (permission.getMenuType() == 0 || permission.getMenuType() == 1) {
			json.put("id", permission.getId());
			if (permission.isRoute()) {
				json.put("route", "1");// 表示生成路由
			} else {
				json.put("route", "0");// 表示不生成路由
			}

			if (isWWWHttpUrl(permission.getUrl())) {
				json.put("path", MD5Util.MD5Encode(permission.getUrl(), "utf-8"));
			} else {
				json.put("path", permission.getUrl());
			}

			// 重要规则：路由name (通过URL生成路由name,路由name供前端开发，页面跳转使用)
			if (ConvertUtils.isNotEmpty(permission.getComponentName())) {
				json.put("name", permission.getComponentName());
			} else {
				json.put("name", urlToRouteName(permission.getUrl()));
			}

			// 是否隐藏路由，默认都是显示的
			if (permission.isHidden()) {
				json.put("hidden", true);
			}
			// 聚合路由
			if (permission.isAlwaysShow()) {
				json.put("alwaysShow", true);
			}
			json.put("component", permission.getComponent());
			JSONObject meta = new JSONObject();
			// 默认所有的菜单都加路由缓存，提高系统性能
			meta.put("keepAlive", "true");
			meta.put("title", permission.getName());
			if (ConvertUtils.isEmpty(permission.getParentId())) {
				// 一级菜单跳转地址
				json.put("redirect", permission.getRedirect());
				if (ConvertUtils.isNotEmpty(permission.getIcon())) {
					meta.put("icon", permission.getIcon());
				}
			} else {
				if (ConvertUtils.isNotEmpty(permission.getIcon())) {
					meta.put("icon", permission.getIcon());
				}
			}
			if (isWWWHttpUrl(permission.getUrl())) {
				meta.put("url", permission.getUrl());
			}
			json.put("meta", meta);
		}

		return json;
	}

	/**
	 * 判断是否外网URL 例如： http://localhost:8080/jeecg-boot/swagger-ui.html#/ 支持特殊格式： {{
	 * window._CONFIG['domianURL'] }}/druid/ {{ JS代码片段 }}，前台解析会自动执行JS代码片段
	 * 
	 * @return
	 */
	private boolean isWWWHttpUrl(String url) {
		if (url != null && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("{{"))) {
			return true;
		}
		return false;
	}

	/**
	 * 通过URL生成路由name（去掉URL前缀斜杠，替换内容中的斜杠‘/’为-） 举例： URL = /isystem/role RouteName =
	 * isystem-role
	 * 
	 * @return
	 */
	private String urlToRouteName(String url) {
		if (ConvertUtils.isNotEmpty(url)) {
			if (url.startsWith("/")) {
				url = url.substring(1);
			}
			url = url.replace("/", "-");

			// 特殊标记
			url = url.replace(":", "@");
			return url;
		} else {
			return null;
		}
	}

	/**
	 * 根据菜单id来获取其对应的权限数据
	 * 
	 * @param sysPermissionDataRule
	 * @return
	 */
	@RequestMapping(value = "/getPermRuleListByPermId", method = RequestMethod.GET)
	public JsonResult<List<SysPermissionDataRule>> getPermRuleListByPermId(SysPermissionDataRule sysPermissionDataRule) {
		List<SysPermissionDataRule> permRuleList = sysPermissionDataRuleService.getPermRuleListByPermId(sysPermissionDataRule.getPermissionId());
		JsonResult<List<SysPermissionDataRule>> result = new JsonResult<>();
		result.setResult(permRuleList);
		return result;
	}

	/**
	 * 添加菜单权限数据
	 * 
	 * @param sysPermissionDataRule
	 * @return
	 */
	@RequestMapping(value = "/addPermissionRule", method = RequestMethod.POST)
	public JsonResult<SysPermissionDataRule> addPermissionRule(@RequestBody SysPermissionDataRule sysPermissionDataRule) {
		JsonResult<SysPermissionDataRule> result = new JsonResult<SysPermissionDataRule>();
		try {
			sysPermissionDataRule.setCreateTime(new Date());
			sysPermissionDataRuleService.savePermissionDataRule(sysPermissionDataRule);
			result.success("添加成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException("操作失败");
		}
		return result;
	}

	@RequestMapping(value = "/editPermissionRule", method = { RequestMethod.PUT, RequestMethod.POST })
	public JsonResult<SysPermissionDataRule> editPermissionRule(@RequestBody SysPermissionDataRule sysPermissionDataRule) {
		JsonResult<SysPermissionDataRule> result = new JsonResult<SysPermissionDataRule>();
		try {
			sysPermissionDataRuleService.saveOrUpdate(sysPermissionDataRule);
			result.success("更新成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException("操作失败");
		}
		return result;
	}

	/**
	 * 删除菜单权限数据
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/deletePermissionRule", method = RequestMethod.DELETE)
	public JsonResult<SysPermissionDataRule> deletePermissionRule(@RequestParam(name = "id", required = true) String id) {
		JsonResult<SysPermissionDataRule> result = new JsonResult<SysPermissionDataRule>();
		try {
			sysPermissionDataRuleService.deletePermissionDataRule(id);
			result.success("删除成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException("操作失败");
		}
		return result;
	}

	/**
	 * 查询菜单权限数据
	 * 
	 * @param sysPermissionDataRule
	 * @return
	 */
	@RequestMapping(value = "/queryPermissionRule", method = RequestMethod.GET)
	public JsonResult<List<SysPermissionDataRule>> queryPermissionRule(SysPermissionDataRule sysPermissionDataRule) {
		JsonResult<List<SysPermissionDataRule>> result = new JsonResult<>();
		try {
			List<SysPermissionDataRule> permRuleList = sysPermissionDataRuleService.queryPermissionRule(sysPermissionDataRule);
			result.setResult(permRuleList);
			result.success("查询成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException("操作失败");
		}
		return result;
	}

}
