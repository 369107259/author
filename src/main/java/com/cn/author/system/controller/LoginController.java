package com.cn.author.system.controller;

import com.cn.author.common.constant.interfaces.CommonConstant;
import com.cn.author.common.jwt.JwtUtil;
import com.cn.author.common.redis.RedisUtil;
import com.cn.author.common.response.JsonResult;
import com.cn.author.common.utils.PasswordUtil;
import com.cn.author.system.entity.SysDepart;
import com.cn.author.system.entity.SysUser;
import com.cn.author.system.model.request.LoginRequestJson;
import com.cn.author.system.model.response.LoginUser;
import com.cn.author.system.model.response.UserResponseJson;
import com.cn.author.system.service.ISysDepartService;
import com.cn.author.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
	@Autowired
	private ISysDepartService sysDepartService;

	@PostMapping(value = "/login")
	@ApiOperation("登录接口")
	public JsonResult login(@RequestBody LoginRequestJson loginRequestJson) {
		String username = loginRequestJson.getUsername();
		String password = loginRequestJson.getPassword();
		SysUser sysUser = sysUserService.getUserByName(username);
		if(sysUser==null) {
			return JsonResult.fault("该用户不存在");
		}else {
			//密码验证
			String encryptPassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
			String sysPassword = sysUser.getPassword();
			if(!sysPassword.equals(encryptPassword)) {
				return JsonResult.fault("用户名或密码错误");
			}
			//生成token
			String token = JwtUtil.sign(sysUser);
			redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
			 //设置超时时间
			redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME/1000);

			//获取用户部门信息
			List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());

			UserResponseJson userResponseJson = new UserResponseJson();
			BeanUtils.copyProperties(sysUser,userResponseJson);
			userResponseJson.setToken(token);
			userResponseJson.setDeparts(departs);
			return JsonResult.ok("登录成功",userResponseJson);
		}
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
