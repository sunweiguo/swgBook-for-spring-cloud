package com.swg.weatherbasic.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 14:32
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Data
public class Weather  implements Serializable {
    private String city;
    private String aqi;
    private List<Forecast> forecast;
    private String ganmao;
    private String wendu;
    private Yesterday yesterday;
}
