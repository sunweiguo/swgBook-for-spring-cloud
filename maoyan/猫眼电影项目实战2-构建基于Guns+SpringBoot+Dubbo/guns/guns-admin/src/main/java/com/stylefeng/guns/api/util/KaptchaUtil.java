package com.stylefeng.guns.api.util;

import com.stylefeng.guns.config.properties.GunsProperties;
import com.stylefeng.guns.core.util.SpringContextHolder;

/**
 * 验证码工具类
 */
public class KaptchaUtil {

    /**
     * 获取验证码开关
     */
    public static Boolean getKaptchaOnOff() {
        return SpringContextHolder.getBean(GunsProperties.class).getKaptchaOpen();
    }
}