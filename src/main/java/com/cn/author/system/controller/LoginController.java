package com.cn.author.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.cn.author.common.constant.interfaces.CommonConstant;
import com.cn.author.common.jwt.JwtUtil;
import com.cn.author.common.response.JsonResult;
import com.cn.author.common.utils.PasswordUtil;
import com.cn.author.common.redis.RedisUtil;
import com.cn.author.system.entity.SysUser;
import com.cn.author.system.model.response.LoginUser;
import com.cn.author.system.model.request.SysLoginModel;
import com.cn.author.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author scott
 * @since 2018-12-17
 */
@RestController
@RequestMapping("/sys")
@Api(tags="用户登录")
@Slf4j
public class LoginController {
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	RedisUtil redisUtil;


	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ApiOperation("登录接口")
	public JsonResult<JSONObject> login(@RequestBody SysLoginModel sysLoginModel) {
		String username = sysLoginModel.getUsername();
		String password = sysLoginModel.getPassword();
		SysUser sysUser = sysUserService.getUserByName(username);
		JsonResult<JSONObject> jsonResult = new JsonResult<>();
		if(sysUser==null) {
			jsonResult.error("该用户不存在");
			return jsonResult;
		}else {
			//密码验证
			String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
			String syspassword = sysUser.getPassword();
			if(!syspassword.equals(userpassword)) {
				jsonResult.error("用户名或密码错误");
				return jsonResult;
			}
			//生成token
			String token = JwtUtil.sign(username, syspassword);
			redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
			 //设置超时时间
			redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME/1000);

			JSONObject obj = new JSONObject();
			obj.put("token", token);
			obj.put("userInfo", sysUser);
			jsonResult.setResult(obj);
			jsonResult.success("登录成功");
		}
		return jsonResult;
	}
	
	/**
	 * 退出登录
	 * @return
	 */
	@RequestMapping(value = "/logout")
	public JsonResult logout(HttpServletRequest request, HttpServletResponse response) {
		//用户退出逻辑
		Subject subject = SecurityUtils.getSubject();
		LoginUser sysUser = (LoginUser)subject.getPrincipal();
	    subject.logout();
		log.info(" 用户名:  "+sysUser.getRealname()+",退出成功！ ");

	    String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
	    //清空用户Token缓存
	    redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + token);
	    //清空用户权限缓存：权限Perms和角色集合
	    redisUtil.del(CommonConstant.LOGIN_USER_CACHE_RULES_ROLE + sysUser.getUsername());
	    redisUtil.del(CommonConstant.LOGIN_USER_CACHE_RULES_PERMISSION + sysUser.getUsername());
		return JsonResult.ok("退出登录成功！");
	}
}
