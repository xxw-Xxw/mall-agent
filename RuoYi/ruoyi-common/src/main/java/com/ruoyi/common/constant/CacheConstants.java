package com.ruoyi.common.constant;

/**
 * 缓存的key 常量
 * 
 * @author ruoyi
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 用户单点会话映射 redis key（同账号互踢，value 为该用户当前唯一 token uuid）
     */
    public static final String LOGIN_USER_TOKEN_KEY = "login_user_tokens:";

    /**
     * 商城会员会话 redis key（与 sys_user 管理端会话隔离）
     */
    public static final String STORE_TOKEN_KEY = "store_tokens:";

    /**
     * 商城会员单点会话映射 redis key（同账号互踢）
     */
    public static final String STORE_MEMBER_TOKEN_KEY = "store_member_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";
}
