package com.cn.author.system.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SysDepartUsersVO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**部门id*/
	private String depId;
	/**对应的用户id集合*/
	private List<String> userIdList;
	public SysDepartUsersVO(String depId, List<String> userIdList) {
		super();
		this.depId = depId;
		this.userIdList = userIdList;
	}
	
	
}
