package com.cn.author.system.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
public class SysUserDepartsVO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**用户id*/
	private String userId;
	/**对应的部门id集合*/
	private List<String> departIdList;
	public SysUserDepartsVO(String userId, List<String> departIdList) {
		super();
		this.userId = userId;
		this.departIdList = departIdList;
	}
	
	
}
