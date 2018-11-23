package com.swg.weatherbasic.service;

import com.swg.weatherbasic.vo.WeatherResponse;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 14:45
 * @DESC
 * @CONTACT 317758022@qq.com
 */
public interface IWeatherDataService {
    WeatherResponse getDataByCityId(String cityId);

    WeatherResponse getDataByCityName(String cityName);
}
