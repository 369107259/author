package com.cn.author.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cn.author.system.entity.SysPermissionDataRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 权限规则 Mapper 接口
 * </p>
 *
 * @Author huangYong
 * @since 2019-04-01
 */
public interface SysPermissionDataRuleMapper extends BaseMapper<SysPermissionDataRule> {
	
	/**
	  * 根据用户名和权限id查询
	 * @param username
	 * @param permissionId
	 * @return
	 */
	public List<String> queryDataRuleIds(@Param("username") String username, @Param("permissionId") String permissionId);

}
