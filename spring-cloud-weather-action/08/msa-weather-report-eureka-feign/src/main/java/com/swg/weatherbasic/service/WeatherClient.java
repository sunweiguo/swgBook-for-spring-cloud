package com.swg.weatherbasic.service;

import com.swg.weatherbasic.vo.WeatherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author 【swg】.
 * @Date 2018/11/23 15:31
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@FeignClient("msa-weather-data-eureka")
public interface WeatherClient {
    @RequestMapping("weather/cityId/{cityId}")
    WeatherResponse getDataByCityId(@PathVariable("cityId") String cityId);

}
