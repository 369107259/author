package com.cn.author.system.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.author.common.exception.BusinessException;
import com.cn.author.common.response.JsonResult;
import com.cn.author.common.universal.query.QueryGenerator;
import com.cn.author.common.utils.ConvertUtils;
import com.cn.author.common.utils.PasswordUtil;
import com.cn.author.system.entity.SysUser;
import com.cn.author.system.entity.SysUserDepart;
import com.cn.author.system.entity.SysUserRole;
import com.cn.author.system.model.DepartIdModel;
import com.cn.author.system.model.SysDepartUsersVO;
import com.cn.author.system.model.SysUserDepartsVO;
import com.cn.author.system.model.SysUserRoleVO;
import com.cn.author.system.service.ISysUserDepartService;
import com.cn.author.system.service.ISysUserRoleService;
import com.cn.author.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Slf4j
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

	@Autowired
	private ISysUserService sysUserService;

	@Autowired
	private ISysUserRoleService sysUserRoleService;

	@Autowired
	private ISysUserDepartService sysUserDepartService;

	@Autowired
	private ISysUserRoleService userRoleService;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JsonResult queryPageList(SysUser user, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                    @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
		QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(user, req.getParameterMap());
		Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
		IPage<SysUser> pageList = sysUserService.page(page, queryWrapper);
		return JsonResult.ok(pageList);
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public JsonResult<SysUser> add(@RequestBody JSONObject jsonObject) {
		JsonResult<SysUser> result = new JsonResult<SysUser>();
		String selectedRoles = jsonObject.getString("selectedroles");
		try {
			SysUser user = JSON.parseObject(jsonObject.toJSONString(), SysUser.class);
			user.setCreateTime(new Date());//设置创建时间
			String salt = ConvertUtils.randomGen(8);
			user.setSalt(salt);
			String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), salt);
			user.setPassword(passwordEncode);
			user.setStatus(1);
			user.setDelFlag("0");
			sysUserService.addUserWithRole(user, selectedRoles);
			result.success("添加成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
            result.error("操作失败");
		}
		return result;
	}

	@RequestMapping(value = "/edit", method = RequestMethod.PUT)
	public JsonResult<SysUser> edit(@RequestBody JSONObject jsonObject) {
		JsonResult<SysUser> result = new JsonResult<SysUser>();
		try {
			SysUser sysUser = sysUserService.getById(jsonObject.getString("id"));
			if(sysUser==null) {
                throw new BusinessException("未找到对应实体");
			}else {
				SysUser user = JSON.parseObject(jsonObject.toJSONString(), SysUser.class);
				user.setUpdateTime(new Date());
				//String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), sysUser.getSalt());
				user.setPassword(sysUser.getPassword());
				String roles = jsonObject.getString("selectedroles");
				sysUserService.editUserWithRole(user, roles);
				result.success("修改成功!");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
            result.error("操作失败");
		}
		return result;
	}

	/**
	 * 删除用户
	 */
	@DeleteMapping(value = "/delete")
	public JsonResult<SysUser> delete(@RequestParam(name="id") String id) {
		JsonResult<SysUser> result = new JsonResult<SysUser>();
		// 定义SysUserDepart实体类的数据库查询LambdaQueryWrapper
		LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<SysUserDepart>();
		SysUser sysUser = sysUserService.getById(id);
		if(sysUser==null) {
            result.error("未找到对应实体");
		}else {
			// 当某个用户被删除时,删除其ID下对应的部门数据
			query.eq(SysUserDepart::getUserId, id);
			boolean ok = sysUserService.removeById(id);
			sysUserDepartService.remove(query);
			if(ok) {
				result.success("删除成功!");
			}
		}
		return result;
	}

	/**
	 * 批量删除用户
	 */
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public JsonResult<SysUser> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		// 定义SysUserDepart实体类的数据库查询对象LambdaQueryWrapper
		LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<SysUserDepart>();
		String[] idArry = ids.split(",");
		JsonResult<SysUser> result = new JsonResult<SysUser>();
		if(ids==null || "".equals(ids.trim())) {
            throw new BusinessException("参数不识别！");
		}else {
			this.sysUserService.removeByIds(Arrays.asList(ids.split(",")));
			// 当批量删除时,删除在SysUserDepart中对应的所有部门数据
			for(String id : idArry) {
				query.eq(SysUserDepart::getUserId, id);
				this.sysUserDepartService.remove(query);
			}
			result.success("删除成功!");
		}
		return result;
	}

	/**
	  * 冻结&解冻用户
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "/frozenBatch", method = RequestMethod.PUT)
	public JsonResult<SysUser> frozenBatch(@RequestBody JSONObject jsonObject) {
		JsonResult<SysUser> result = new JsonResult<SysUser>();
		try {
			String ids = jsonObject.getString("ids");
			String status = jsonObject.getString("status");
			String[] arr = ids.split(",");
			for (String id : arr) {
				if(ConvertUtils.isNotEmpty(id)) {
					this.sysUserService.update(new SysUser().setStatus(Integer.parseInt(status)),
							new UpdateWrapper<SysUser>().lambda().eq(SysUser::getId,id));
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
            throw new BusinessException("操作失败"+e.getMessage());
		}
		result.success("操作成功!");
		return result;

    }

    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public JsonResult<SysUser> queryById(@RequestParam(name = "id", required = true) String id) {
        JsonResult<SysUser> result = new JsonResult<SysUser>();
        SysUser sysUser = sysUserService.getById(id);
        if (sysUser == null) {
            result.fault("未找到对应实体");
        } else {
            result.setResult(sysUser);
        }
        return result;
    }

    @RequestMapping(value = "/queryUserRole", method = RequestMethod.GET)
    public JsonResult<List<String>> queryUserRole(@RequestParam(name = "userid", required = true) String userid) {
        JsonResult<List<String>> result = new JsonResult<>();
        List<String> list = new ArrayList<String>();
        List<SysUserRole> userRole = sysUserRoleService.list(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, userid));
        if (userRole == null || userRole.size() <= 0) {
            result.fault("未找到用户相关角色信息");
        } else {
            for (SysUserRole sysUserRole : userRole) {
                list.add(sysUserRole.getRoleId());
            }
            result.setResult(list);
        }
        return result;
    }


    /**
     * 校验用户账号是否唯一<br>
     * 可以校验其他 需要检验什么就传什么。。。
     *
     * @param sysUser
     * @return
     */
    @RequestMapping(value = "/checkOnlyUser", method = RequestMethod.GET)
    public JsonResult<Boolean> checkUsername(SysUser sysUser) {
        JsonResult<Boolean> result = new JsonResult<>();
        result.setResult(true);//如果此参数为false则程序发生异常
        String id = sysUser.getId();
        log.info("--验证用户信息是否唯一---id:" + id);
        try {
            SysUser oldUser = null;
            if (ConvertUtils.isNotEmpty(id)) {
                oldUser = sysUserService.getById(id);
            } else {
                sysUser.setId(null);
            }
            //通过传入信息查询新的用户信息
            SysUser newUser = sysUserService.getOne(new QueryWrapper<SysUser>(sysUser));
            if (newUser != null) {
                //如果根据传入信息查询到用户了，那么就需要做校验了。
                if (oldUser == null) {
                    //oldUser为空=>新增模式=>只要用户信息存在则返回false
                    result.setMessage("用户账号已存在");
                    return result;
                } else if (!id.equals(newUser.getId())) {
                    //否则=>编辑模式=>判断两者ID是否一致-
                    result.setMessage("用户账号已存在");
                    return result;
                }
            }

        } catch (Exception e) {
            result.setResult(false);
            result.setMessage(e.getMessage());
            return result;
        }
        return result;
    }

    /**
     * 修改密码
     */
    @RequestMapping(value = "/changPassword", method = RequestMethod.PUT)
    public JsonResult<SysUser> changPassword(@RequestBody SysUser sysUser) {
        JsonResult<SysUser> result = new JsonResult<SysUser>();
        String password = sysUser.getPassword();
        sysUser = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, sysUser.getUsername()));
        if (sysUser == null) {
            throw new BusinessException("未找到对应实体");
        } else {
            String salt = ConvertUtils.randomGen(8);
            sysUser.setSalt(salt);
            String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
            sysUser.setPassword(passwordEncode);
            this.sysUserService.updateById(sysUser);
            result.setResult(sysUser);
            result.success("密码修改完成！");
        }
        return result;
    }

    /**
     * 查询指定用户和部门关联的数据
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/userDepartList", method = RequestMethod.GET)
    public JsonResult<List<DepartIdModel>> getUserDepartsList(@RequestParam(name = "userId", required = true) String userId) {
        JsonResult<List<DepartIdModel>> result = new JsonResult<>();
        try {
            List<DepartIdModel> depIdModelList = this.sysUserDepartService.queryDepartIdsOfUser(userId);
            if (depIdModelList != null && depIdModelList.size() > 0) {
                result.setMessage("查找成功");
                result.setResult(depIdModelList);
            } else {
                result.setMessage("查找失败");
            }
            return result;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            result.setMessage("查找过程中出现了异常: " + e.getMessage());
            return result;
        }

    }

    /**
     * 给指定用户添加对应的部门
     *
     * @param sysUserDepartsVO
     * @return
     */
    @RequestMapping(value = "/addUDepartIds", method = RequestMethod.POST)
    public JsonResult<String> addSysUseWithrDepart(@RequestBody SysUserDepartsVO sysUserDepartsVO) {
        boolean ok = this.sysUserDepartService.addSysUseWithrDepart(sysUserDepartsVO);
        JsonResult<String> result = new JsonResult<String>();
        try {
            if (ok) {
                result.setMessage("添加成功!");
            } else {
                throw new Exception("添加失败!");
            }
            return result;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            result.setMessage("添加数据的过程中出现市场了: " + e.getMessage());
            return result;
        }

    }

    /**
     * 根据用户id编辑对应的部门信息
     *
     * @param sysUserDepartsVO
     * @return
     */
    @RequestMapping(value = "/editUDepartIds", method = RequestMethod.PUT)
    public JsonResult<String> editSysUserWithDepart(@RequestBody SysUserDepartsVO sysUserDepartsVO) {
        JsonResult<String> result = new JsonResult<String>();
        boolean ok = sysUserDepartService.editSysUserWithDepart(sysUserDepartsVO);
        if (ok) {
            result.setMessage("更新成功!");
            return result;
        }
        result.setMessage("更新失败!");
        return result;
    }

    /**
     * 生成在添加用户情况下没有主键的问题,返回给前端,根据该id绑定部门数据
     *
     * @return
     */
    @RequestMapping(value = "/generateUserId", method = RequestMethod.GET)
    public JsonResult<String> generateUserId() {
        JsonResult<String> result = new JsonResult<>();
        System.out.println("我执行了,生成用户ID==============================");
        String userId = UUID.randomUUID().toString().replace("-", "");
        result.setResult(userId);
        return result;
    }

    /**
     * 根据部门id查询用户信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryUserByDepId", method = RequestMethod.GET)
    public JsonResult<List<SysUser>> queryUserByDepId(@RequestParam(name = "id", required = true) String id) {
        JsonResult<List<SysUser>> result = new JsonResult<>();
        List<SysUser> userList = sysUserDepartService.queryUserByDepId(id);
        try {
            result.setResult(userList);
            return result;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            return result;
        }
    }

    /**
     * 查询所有用户所对应的角色信息
     *
     * @return
     */
    @RequestMapping(value = "/queryUserRoleMap", method = RequestMethod.GET)
    public JsonResult<Map<String, String>> queryUserRole() {
        JsonResult<Map<String, String>> result = new JsonResult<>();
        Map<String, String> map = userRoleService.queryUserRole();
        result.setResult(map);
        return result;
    }

    /**
     * 导出excel
     *
     * @param request
     * @param sysUser
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysUser sysUser, HttpServletRequest request) {
        // Step.1 组装查询条件
        QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUser, request.getParameterMap());
        //Step.2 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        List<SysUser> pageList = sysUserService.list(queryWrapper);
        //导出文件名称
        mv.addObject(NormalExcelConstants.FILE_NAME, "用户列表");
        mv.addObject(NormalExcelConstants.CLASS, SysUser.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("用户列表数据", "导出人:Jeecg", "导出信息"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public JsonResult<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();// 获取上传文件对象
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SysUser> listSysUsers = ExcelImportUtil.importExcel(file.getInputStream(), SysUser.class, params);
                for (SysUser sysUserExcel : listSysUsers) {
                    if (sysUserExcel.getPassword() == null) {
                        // 密码默认为“123456”
                        sysUserExcel.setPassword("123456");
                    }
                    sysUserService.save(sysUserExcel);
                }
                return JsonResult.ok("文件导入成功！数据行数：" + listSysUsers.size());
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                return JsonResult.fault("抱歉! 您导入的数据中用户名已经存在.");
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                	log.error(e.getMessage(), e);
                }
            }
        }
        return JsonResult.fault("文件导入失败！");
    }

    /**
	 * @功能：根据id 批量查询
	 * @param userIds
	 * @return
	 */
	@RequestMapping(value = "/queryByIds", method = RequestMethod.GET)
	public JsonResult<Collection<SysUser>> queryByIds(@RequestParam String userIds) {
		JsonResult<Collection<SysUser>> result = new JsonResult<>();
		String[] userId = userIds.split(",");
		Collection<String> idList = Arrays.asList(userId);
		Collection<SysUser> userRole = sysUserService.listByIds(idList);
		result.setResult(userRole);
		return result;
	}

	/**
	 * 首页密码修改
	 */
	@RequestMapping(value = "/updatePassword", method = RequestMethod.PUT)
	public JsonResult<SysUser> changPassword(@RequestBody JSONObject json) {
		JsonResult<SysUser> result = new JsonResult<SysUser>();
		String username = json.getString("username");
		String oldpassword = json.getString("oldpassword");
		SysUser user = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
		if(user==null) {
			throw new BusinessException("未找到用户!");
		}
		String passwordEncode = PasswordUtil.encrypt(username, oldpassword, user.getSalt());
		if(!user.getPassword().equals(passwordEncode)) {
            throw new BusinessException("旧密码输入错误!");
		}

		String password = json.getString("password");
		String confirmpassword = json.getString("confirmpassword");
		if(ConvertUtils.isEmpty(password)) {
            throw new BusinessException("新密码不存在!");
		}

		if(!password.equals(confirmpassword)) {
            throw new BusinessException("两次输入密码不一致!");
		}
		String newpassword = PasswordUtil.encrypt(username, password, user.getSalt());
		this.sysUserService.update(new SysUser().setPassword(newpassword), new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, user.getId()));
		result.success("密码修改完成！");
		return result;
	}

    @RequestMapping(value = "/userRoleList", method = RequestMethod.GET)
    public JsonResult<IPage<SysUser>> userRoleList(@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                               @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        JsonResult<IPage<SysUser>> result = new JsonResult<IPage<SysUser>>();
        Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
        String roleId = req.getParameter("roleId");
        String username = req.getParameter("username");
        IPage<SysUser> pageList = sysUserService.getUserByRoleId(page,roleId,username);
        result.setResult(pageList);
        return result;
    }

    /**
     * 给指定角色添加用户
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/addSysUserRole", method = RequestMethod.POST)
    public JsonResult<String> addSysUserRole(@RequestBody SysUserRoleVO sysUserRoleVO) {
        JsonResult<String> result = new JsonResult<String>();
        try {
            String sysRoleId = sysUserRoleVO.getRoleId();
            for(String sysUserId:sysUserRoleVO.getUserIdList()) {
                SysUserRole sysUserRole = new SysUserRole(sysUserId,sysRoleId);
                QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<SysUserRole>();
                queryWrapper.eq("role_id", sysRoleId).eq("user_id",sysUserId);
                SysUserRole one = sysUserRoleService.getOne(queryWrapper);
                if(one==null){
                    sysUserRoleService.save(sysUserRole);
                }

            }

            result.setMessage("添加成功!");
            return result;
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.setMessage("出错了: " + e.getMessage());
            return result;
        }
    }
    /**
     *   删除指定角色的用户关系
     * @param
     * @return
     */
    @RequestMapping(value = "/deleteUserRole", method = RequestMethod.DELETE)
    public JsonResult<SysUserRole> deleteUserRole(@RequestParam(name="roleId") String roleId,
                                                    @RequestParam(name="userId",required=true) String userId
    ) {
        JsonResult<SysUserRole> result = new JsonResult<SysUserRole>();
        try {
            QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<SysUserRole>();
//            LambdaQueryWrapper<SysUserRole> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//            lambdaQueryWrapper.eq(SysUserRole::getRoleId,roleId).eq(SysUserRole::getUserId,userId);
            queryWrapper.eq("role_id", roleId).eq("user_id",userId);
            sysUserRoleService.remove(queryWrapper);
            result.success("删除成功!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("删除失败！");
        }
        return result;
    }

    /**
     * 批量删除指定角色的用户关系
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/deleteUserRoleBatch", method = RequestMethod.DELETE)
    public JsonResult<SysUserRole> deleteUserRoleBatch(
            @RequestParam(name="roleId") String roleId,
            @RequestParam(name="userIds",required=true) String userIds) {
        JsonResult<SysUserRole> result = new JsonResult<SysUserRole>();
        try {
            QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<SysUserRole>();
            queryWrapper.eq("role_id", roleId).in("user_id", Arrays.asList(userIds.split(",")));
            sysUserRoleService.remove(queryWrapper);
            result.success("删除成功!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("删除失败！");
        }
        return result;
    }

    /**
     * 部门用户列表
     */
    @RequestMapping(value = "/departUserList", method = RequestMethod.GET)
    public JsonResult<IPage<SysUser>> departUserList(@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        JsonResult<IPage<SysUser>> result = new JsonResult<IPage<SysUser>>();
        Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
        String depId = req.getParameter("depId");
        String username = req.getParameter("username");
        IPage<SysUser> pageList = sysUserService.getUserByDepId(page,depId,username);
        result.setResult(pageList);
        return result;
    }

    /**
     * 给指定部门添加对应的用户
     */
    @RequestMapping(value = "/editSysDepartWithUser", method = RequestMethod.POST)
    public JsonResult<String> editSysDepartWithUser(@RequestBody SysDepartUsersVO sysDepartUsersVO) {
        JsonResult<String> result = new JsonResult<String>();
        try {
            String sysDepId = sysDepartUsersVO.getDepId();
            for(String sysUserId:sysDepartUsersVO.getUserIdList()) {
                SysUserDepart sysUserDepart = new SysUserDepart(null,sysUserId,sysDepId);
                QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<SysUserDepart>();
                queryWrapper.eq("dep_id", sysDepId).eq("user_id",sysUserId);
                SysUserDepart one = sysUserDepartService.getOne(queryWrapper);
                if(one==null){
                    sysUserDepartService.save(sysUserDepart);
                }
            }

            result.setMessage("添加成功!");
            return result;
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.setMessage("出错了: " + e.getMessage());
            return result;
        }
    }

    /**
     *   删除指定机构的用户关系
     */
    @RequestMapping(value = "/deleteUserInDepart", method = RequestMethod.DELETE)
    public JsonResult<SysUserDepart> deleteUserInDepart(@RequestParam(name="depId") String depId,
                                                    @RequestParam(name="userId",required=true) String userId
    ) {
        JsonResult<SysUserDepart> result = new JsonResult<SysUserDepart>();
        try {
            QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<SysUserDepart>();
            queryWrapper.eq("dep_id", depId).eq("user_id",userId);
            sysUserDepartService.remove(queryWrapper);
            result.success("删除成功!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("删除失败！");
        }
        return result;
    }

    /**
     * 批量删除指定机构的用户关系
     */
    @RequestMapping(value = "/deleteUserInDepartBatch", method = RequestMethod.DELETE)
    public JsonResult<SysUserDepart> deleteUserInDepartBatch(
            @RequestParam(name="depId") String depId,
            @RequestParam(name="userIds",required=true) String userIds) {
        JsonResult<SysUserDepart> result = new JsonResult<SysUserDepart>();
        try {
            QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<SysUserDepart>();
            queryWrapper.eq("dep_id", depId).in("user_id", Arrays.asList(userIds.split(",")));
            sysUserDepartService.remove(queryWrapper);
            result.success("删除成功!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("删除失败！");
        }
        return result;
    }

}
