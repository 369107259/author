package com.cn.author.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cn.author.system.entity.SysUser;
import com.cn.author.system.entity.SysUserDepart;
import com.cn.author.system.model.DepartIdModel;
import com.cn.author.system.model.SysUserDepartsVO;
import java.util.List;

/**
 * <p>
 * SysUserDpeart用户组织机构service
 * </p>
 * @Author ZhiLin
 *
 */
public interface ISysUserDepartService extends IService<SysUserDepart> {
	
	/**
	 * 根据指定用户添加部门信息
	 * @param sysUserDepartsVO
	 * @return
	 */
	boolean addSysUseWithrDepart(SysUserDepartsVO sysUserDepartsVO);
	
	/**
	 * 根据指定用户id查询部门信息
	 * @param userId
	 * @return
	 */
	List<DepartIdModel> queryDepartIdsOfUser(String userId);
	
	/**
	 * 根据指定用户id编辑部门信息
	 * @param sysUserDepartsVO
	 * @return
	 */
	boolean editSysUserWithDepart(SysUserDepartsVO sysUserDepartsVO);
	
	/**
	 * 根据部门id查询用户信息
	 * @param depId
	 * @return
	 */
	List<SysUser> queryUserByDepId(String depId);
}
