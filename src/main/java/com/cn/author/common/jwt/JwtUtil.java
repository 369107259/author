package com.cn.author.common.jwt;

import com.cn.author.common.constant.interfaces.CommonConstant;
import com.cn.author.common.constant.interfaces.DataBaseConstant;
import com.cn.author.common.exception.BusinessException;
import com.cn.author.common.utils.ConvertUtils;
import com.cn.author.common.utils.SpringContextUtils;
import com.cn.author.common.utils.system.JeecgDataAutorUtils;
import com.cn.author.system.entity.SysUser;
import com.cn.author.system.model.response.SysUserCacheInfo;
import com.google.common.base.Joiner;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @Author Scott
 * @Date 2018-07-12 14:23
 * @Desc JWT工具类
 **/
@Slf4j
public class JwtUtil {

    private static final String secretKey = "user-@QWER}sa@CRFV_qwertyu<?.>7Yh%bff(vo#lm,jh+-)HY)NG56&*HY";

    // 过期时间30分钟
    public static final long EXPIRE_TIME = 30 * 60 * 1000;

    /**
     * 校验token是否正确
     *
     * @param token 密钥
     * @return 是否正确
     */
    public static boolean verify(String token) {
        try {
            Jwts.parser().setSigningKey(generalKey()).parseClaimsJws(token).getBody();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static String getUserName(String token) {
        Claims claims = Jwts.parser().setSigningKey(generalKey()).parseClaimsJws(token).getBody();
        if (claims == null){
            throw new BusinessException("token非法无效");
        }
        return claims.get("userName", String.class);
    }

    /**
     * 获得token中的用户信息
     *
     * @return token中包含的用户名
     */
    public static SysUser getSysUser(String token) {
        try {
            SysUser sysUser = new SysUser();
            Claims claims = Jwts.parser().setSigningKey(generalKey()).parseClaimsJws(token).getBody();
            Iterator<Map.Entry<String, Object>> iterator = claims.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                if ("id".equals(entry.getKey())) {
                    sysUser.setId(entry.getValue().toString());
                }
                if ("userName".equals(entry.getKey())) {
                    sysUser.setUsername(entry.getValue().toString());
                }
            }
            return sysUser;
        } catch (Exception e) {
            log.info("获取用户失败！", e);
            throw new BusinessException("获取用户失败");
        }
    }

    /**
     * 生成签名,5min后过期
     *
     * @param sysUser 用户信心
     * @return 加密的token
     */
    public static String sign(SysUser sysUser) {
        //指定签名算法（header部分）
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        //签发时间
        Date iatDate = new Date();
        //过期时间
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        //加密key
        SecretKey secretKey = generalKey();
        //设置claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", sysUser.getId());
        claims.put("userName", sysUser.getUsername());
        JwtBuilder jwtBuilder = Jwts.builder().setSubject(sysUser.getId()).setClaims(claims).setId(UUID.randomUUID().toString()).setIssuedAt(iatDate).signWith(signatureAlgorithm, secretKey).setExpiration(date);
        return jwtBuilder.compact();
    }

    /**
     * 根据request中的token获取用户账号
     *
     * @param request
     * @return
     */
    public static String getUserNameByToken(HttpServletRequest request) throws BusinessException {
        String accessToken = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
        SysUser sysUser = getSysUser(accessToken);
        return sysUser.getUsername();
    }

    /**
     * 从session中获取变量
     *
     * @param key
     * @return
     */
    public static String getSessionData(String key) {
        //${myVar}%
        //得到${} 后面的值
        String moshi = "";
        if (key.indexOf("}") != -1) {
            moshi = key.substring(key.indexOf("}") + 1);
        }
        String returnValue = null;
        if (key.contains("#{")) {
            key = key.substring(2, key.indexOf("}"));
        }
        if (ConvertUtils.isNotEmpty(key)) {
            HttpSession session = SpringContextUtils.getHttpServletRequest().getSession();
            returnValue = (String) session.getAttribute(key);
        }
        //结果加上${} 后面的值
        if (returnValue != null) {
            returnValue = returnValue + moshi;
        }
        return returnValue;
    }

    /**
     * 从当前用户中获取变量
     *
     * @param key
     * @param user
     * @return
     */
    public static String getUserSystemData(String key, SysUserCacheInfo user) {
        if (user == null) {
            user = JeecgDataAutorUtils.loadUserInfo();
        }
        //#{sys_user_code}%
        String moshi = "";
        if (key.indexOf("}") != -1) {
            moshi = key.substring(key.indexOf("}") + 1);
        }
        String returnValue = null;
        //针对特殊标示处理#{sysOrgCode}，判断替换
        if (key.contains("#{")) {
            key = key.substring(2, key.indexOf("}"));
        } else {
            key = key;
        }
        //替换为系统登录用户帐号
        if (key.equals(DataBaseConstant.SYS_USER_CODE) || key.equals(DataBaseConstant.SYS_USER_CODE_TABLE)) {
            returnValue = user.getSysUserCode();
        }
        //替换为系统登录用户真实名字
        if (key.equals(DataBaseConstant.SYS_USER_NAME) || key.equals(DataBaseConstant.SYS_USER_NAME_TABLE)) {
            returnValue = user.getSysUserName();
        }

        //替换为系统用户登录所使用的机构编码
        if (key.equals(DataBaseConstant.SYS_ORG_CODE) || key.equals(DataBaseConstant.SYS_ORG_CODE_TABLE)) {
            returnValue = user.getSysOrgCode();
        }
        //替换为系统用户所拥有的所有机构编码
        if (key.equals(DataBaseConstant.SYS_MULTI_ORG_CODE) || key.equals(DataBaseConstant.SYS_MULTI_ORG_CODE)) {
            if (user.isOneDepart()) {
                returnValue = user.getSysMultiOrgCode().get(0);
            } else {
                returnValue = Joiner.on(",").join(user.getSysMultiOrgCode());
            }
        }
        //替换为当前系统时间(年月日)
        if (key.equals(DataBaseConstant.SYS_DATE) || key.equals(DataBaseConstant.SYS_DATE_TABLE)) {
            returnValue = user.getSysDate();
        }
        //替换为当前系统时间（年月日时分秒）
        if (key.equals(DataBaseConstant.SYS_TIME) || key.equals(DataBaseConstant.SYS_TIME_TABLE)) {
            returnValue = user.getSysTime();
        }
        //流程状态默认值（默认未发起）
        if (key.equals(DataBaseConstant.BPM_STATUS_TABLE) || key.equals(DataBaseConstant.BPM_STATUS_TABLE)) {
            returnValue = "1";
        }
        if (returnValue != null) {
            returnValue = returnValue + moshi;
        }
        return returnValue;
    }

    /**
     * 由字符串生成加密key
     *
     * @return
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.decodeBase64(secretKey);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

}
