package com.swg.weatherbasic.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 14:33
 * @DESC 昨天天气
 * @CONTACT 317758022@qq.com
 */
@Data
public class Yesterday implements Serializable{
    private String date;
    private String high;
    private String fx;
    private String low;
    private String fl;
    private String type;
}
