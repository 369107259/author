package com.cn.author.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cn.author.common.utils.ConvertUtils;
import com.cn.author.system.entity.SysUser;
import com.cn.author.system.entity.SysUserRole;
import com.cn.author.system.mapper.SysUserMapper;
import com.cn.author.system.mapper.SysUserRoleMapper;
import com.cn.author.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

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
        this.save(user);
        NonNullNewUserRoles(user, roles);
    }

    /**
     * 修改用户和用户角色关系
     *
     * @param user
     * @param roles
     */
    @Override
    public void editUserWithRole(SysUser user, String roles) {
        this.updateById(user);
        //先删后加
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        NonNullNewUserRoles(user, roles);

    }

    private void NonNullNewUserRoles(SysUser user, String roles) {
        if (ConvertUtils.isNotEmpty(roles)) {
            String[] arr = roles.split(",");
            for (String roleId : arr) {
                SysUserRole userRole = new SysUserRole(user.getId(), roleId);
                sysUserRoleMapper.insert(userRole);

            }
        }
    }

    /**
     * 获取用户的授权角色
     *
     * @param username
     * @return
     */
    @Override
    public List<String> getRole(String username) {
        return sysUserRoleMapper.getRoleByUserName(username);
    }


    // 根据部门Id查询
    @Override
    public IPage<SysUser> getUserByDepId(Page<SysUser> page, String departId, String username) {
        return sysUserMapper.getUserByDepId(page, departId, username);
    }


    // 根据角色Id查询
    @Override
    public IPage<SysUser> getUserByRoleId(Page<SysUser> page, String roleId, String username) {
        return sysUserMapper.getUserByRoleId(page, roleId, username);
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
