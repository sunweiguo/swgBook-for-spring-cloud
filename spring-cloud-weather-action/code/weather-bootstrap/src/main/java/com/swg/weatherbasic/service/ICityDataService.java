package com.swg.weatherbasic.service;

import com.swg.weatherbasic.pojo.City;
import com.swg.weatherbasic.pojo.CityList;

import java.util.List;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 17:13
 * @DESC
 * @CONTACT 317758022@qq.com
 */
public interface ICityDataService {
    List<City> listCity() throws Exception;
}
