package com.cn.author.common.response;

import com.cn.author.common.constant.interfaces.CommonConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *   接口返回数据格式
 * @author scott
 * @email jeecgos@163.com
 * @date  2019年1月19日
 */
@Data
@ApiModel(value="接口返回对象", description="接口返回对象")
public class Result implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 时间戳
	 */
	@ApiModelProperty(value = "时间戳")
	private long timestamp = System.currentTimeMillis();

	/**
	 * 成功标志
	 */
	@ApiModelProperty(value = "成功标志")
	private boolean success = true;

	/**
	 * 返回处理消息
	 */
	@ApiModelProperty(value = "返回处理消息")
	private String message = "操作成功！";

	/**
	 * 返回代码
	 */
	@ApiModelProperty(value = "返回代码")
	private Integer code;
	
	/**
	 * 返回数据对象 data
	 */
	@ApiModelProperty(value = "返回数据对象")
	private Object data;


	public Result(Integer code,String message,  Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public Result(String message, Object data) {
		this.code = CommonConstant.CODE_SUCCESS;
		this.message = message;
		this.data = data;
	}

	public Result(Object data) {
		this.code = CommonConstant.CODE_SUCCESS;
		this.message = CommonConstant.OPERATE_SUCCESS;
		this.data = data;
	}


	public static Result ok() {
		return new Result(null);
	}

	public static Result ok(Object data) {
		return new Result(data);
	}

	public static Result ok(String msg) {
		return new Result(msg,null);
	}

	public static Result ok(String msg,Object data) {
		return new Result(msg,data);
	}

	
	public static Result error() {
		return new Result(CommonConstant.CODE_FAILED, CommonConstant.OPERATE_FAILED,null);
	}
	public static Result error(String msg) {
		return new Result(CommonConstant.CODE_FAILED, msg,null);
	}

	public static Result error(Integer code, String msg) {
		return new Result(code, msg, null);
	}

}