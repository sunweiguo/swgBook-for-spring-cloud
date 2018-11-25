package com.swg.weatherbasic.service;

import com.swg.weatherbasic.pojo.City;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author 【swg】.
 * @Date 2018/11/23 15:26
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@FeignClient("msa-weather-city-eureka")
public interface CityClient {
    @RequestMapping("cities")
    List<City> listCity() throws Exception;
}
