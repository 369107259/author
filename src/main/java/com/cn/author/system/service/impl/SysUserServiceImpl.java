package com.cn.author.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cn.author.common.utils.ConvertUtils;
import com.cn.author.system.entity.SysUser;
import com.cn.author.system.mapper.SysUserMapper;
import com.cn.author.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @Author: scott
 * @Date: 2018-12-20
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
	
	@Resource
	private SysUserMapper sysUserMapper;

	
	@Override
	public SysUser getUserByName(String username) {
		return sysUserMapper.getUserByName(username);
	}

	/**
	 * 添加用户和用户角色关系
	 *
	 * @param user
	 * @param roles
	 */
	@Override
	public void addUserWithRole(SysUser user, String roles) {

	}

	/**
	 * 修改用户和用户角色关系
	 *
	 * @param user
	 * @param roles
	 */
	@Override
	public void editUserWithRole(SysUser user, String roles) {

	}

	/**
	 * 获取用户的授权角色
	 *
	 * @param username
	 * @return
	 */
	@Override
	public List<String> getRole(String username) {
		return null;
	}


	// 根据部门Id查询
	@Override
	public IPage<SysUser> getUserByDepId(Page<SysUser> page, String departId, String username) {
		return sysUserMapper.getUserByDepId(page, departId,username);
	}


	// 根据角色Id查询
	@Override
	public IPage<SysUser> getUserByRoleId(Page<SysUser> page, String roleId, String username) {
		return sysUserMapper.getUserByRoleId(page,roleId,username);
	}

	/**
	 * 通过用户名获取用户角色集合
	 *
	 * @param username 用户名
	 * @return 角色集合
	 */
	@Override
	public Set<String> getUserRolesSet(String username) {
		return null;
	}

	/**
	 * 通过用户名获取用户权限集合
	 *
	 * @param username 用户名
	 * @return 权限集合
	 */
	@Override
	public Set<String> getUserPermissionsSet(String username) {
		return null;
	}


	@Override
	public void updateUserDepart(String username, String orgCode) {
		baseMapper.updateUserDepart(username, orgCode);
	}

}
