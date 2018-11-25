package com.swg.eurekaclientfeign.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author 【swg】.
 * @Date 2018/11/23 14:47
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@FeignClient("msa-weather-city-eureka")
public interface CityClient {
    @GetMapping("/cities")
    String listCity();
}
