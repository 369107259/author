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
public class JsonResult<T> implements Serializable {

	private static final long serialVersionUID = 1L;

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
	private Integer code = 0;

	/**
	 * 返回数据对象 data
	 */
	@ApiModelProperty(value = "返回数据对象")
	private T result;

	public JsonResult() {

	}

	/**
	 * 时间戳
	 */
	@ApiModelProperty(value = "时间戳")
	private long timestamp = System.currentTimeMillis();


	/**
	 * <p>返回成功,有数据</p>
	 * @param message 操作说明
	 * @param data 对象
	 * @return JsonResult
	 */
	public JsonResult<T> success(String message,T data) {
		this.setCode(CommonConstant.CODE_SUCCESS);
		this.setMessage(CommonConstant.OPERATE_SUCCESS);
		this.setMessage(message);
		this.setResult(data);
		return this;
	}

	/**
	 * <p>返回成功,有数据</p>
	 * @param data 对象
	 * @return JsonResult
	 */
	public JsonResult<T> success(T data) {
		return success(CommonConstant.OPERATE_SUCCESS,data);
	}
	/**
	 * <p>返回成功,无数据</p>
	 * @param message 操作说明
	 * @return JsonResult
	 */
	public JsonResult<T> success(String message) {
		return success(message,null);
	}
	/**
	 * <p>返回失败,无数据</p>
	 * @param message 消息
	 * @return JsonResult
	 */
	public JsonResult<T> error(String message) {
		return error(message,null);
	}
	/**
	 * <p>返回失败,有数据</p>
	 * @param message 消息
	 * @param data 对象
	 * @return JsonResult
	 */
	public JsonResult<T> error(String message,T data) {
		this.setCode(CommonConstant.CODE_FAILED);
		this.setMessage(CommonConstant.OPERATE_FAILED);
		this.setMessage(message);
		this.setResult(data);
		return this;
	}


	public static JsonResult<Object> error500(String message) {
		return error500(CommonConstant.CODE_FAILED,message);
	}

	public static JsonResult<Object> error500(int code, String msg) {
		JsonResult<Object> r = new JsonResult<Object>();
		r.setCode(code);
		r.setMessage(msg);
		r.setSuccess(false);
		return r;
	}

	public static JsonResult<Object> ok() {
		JsonResult<Object> r = new JsonResult<Object>();
		r.setSuccess(true);
		r.setCode(CommonConstant.CODE_SUCCESS);
		r.setMessage(CommonConstant.OPERATE_SUCCESS);
		return r;
	}

	public static JsonResult<Object> ok(String msg) {
		return ok(msg,null);
	}

	public static JsonResult<Object> ok(Object data) {
		return ok(CommonConstant.OPERATE_SUCCESS,data);
	}

	public static JsonResult<Object> ok(String msg, Object data) {
		JsonResult<Object> r = new JsonResult<Object>();
		r.setSuccess(true);
		r.setCode(CommonConstant.CODE_SUCCESS);
		r.setMessage(msg);
		r.setResult(data);
		return r;
	}
}