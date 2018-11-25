package com.swg.weatherbasic.vo;

import com.swg.weatherbasic.pojo.Weather;
import com.swg.weatherbasic.pojo.Yesterday;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 14:42
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Data
public class WeatherResponse implements Serializable {
    private Weather data;
    private Integer status;
    private String desc;


}
