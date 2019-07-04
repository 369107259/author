package com.cn.author.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cn.author.common.response.JsonResult;
import com.cn.author.system.entity.SysUser;
import com.cn.author.system.model.request.SysUserRequestJson;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface ISysUserService extends IService<SysUser> {
	
	public SysUser getUserByName(String username);
	
	/**
	 * 添加用户和用户角色关系
	 * @param sysUserRequestJson
	 */
	public JsonResult addUserWithRole(SysUserRequestJson sysUserRequestJson);


	/**
	 * 修改用户和用户角色关系
	 * @param sysUserRequestJson
	 */
	public void editUserWithRole(SysUserRequestJson sysUserRequestJson);

	/**
	 * 获取用户的授权角色
	 * @param username
	 * @return
	 */
	public List<String> getRole(String username);

	/**
	  * 查询用户信息包括 部门信息
	 * @param username
	 * @return
	 */
//	public SysUserCacheInfo getCacheUser(String username);

	/**
	 * 根据部门Id查询
	 * @param
	 * @return
	 */
	public IPage<SysUser> getUserByDepId(Page<SysUser> page, String departId, String username);

	/**
	 * 根据角色Id查询
	 * @param
	 * @return
	 */
	public IPage<SysUser> getUserByRoleId(Page<SysUser> page, String roleId, String username);

	/**
	 * 通过用户名获取用户角色集合
	 *
	 * @param username 用户名
	 * @return 角色集合
	 */
	Set<String> getUserRolesSet(String username);

	/**
	 * 通过用户名获取用户权限集合
	 *
	 * @param username 用户名
	 * @return 权限集合
	 */
	Set<String> getUserPermissionsSet(String username);

	/**
	 * 根据用户名设置部门ID
	 * @param username
	 * @param orgCode
	 */
	void updateUserDepart(String username, String orgCode);
}
