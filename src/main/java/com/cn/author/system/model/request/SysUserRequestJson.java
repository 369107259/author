package com.cn.author.system.model.request;

import com.cn.author.system.entity.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author 黄勇
 * @CreateTime 2019/7/4 16:03
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserRequestJson extends SysUser {

    List<String> selectedRoles;

    List<String> selectedDeparts;
}
