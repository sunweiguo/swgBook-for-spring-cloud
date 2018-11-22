package com.swg.weatherbasic.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 14:33
 * @DESC 未来天气
 * @CONTACT 317758022@qq.com
 */
@Data
public class Forecast implements Serializable {
    private String date;
    private String high;
    private String fengli;
    private String low;
    private String fengxiang;
    private String type;
}
