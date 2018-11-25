package com.swg.weatherbasic.service;

import com.swg.weatherbasic.pojo.City;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author 【swg】.
 * @Date 2018/11/23 15:31
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@FeignClient("msa-eureka-client-zuul")
public interface DataClient {
    /**
     * 获取城市列表
     */
    @RequestMapping("city/cities")
    List<City> listCity() throws Exception;

}
