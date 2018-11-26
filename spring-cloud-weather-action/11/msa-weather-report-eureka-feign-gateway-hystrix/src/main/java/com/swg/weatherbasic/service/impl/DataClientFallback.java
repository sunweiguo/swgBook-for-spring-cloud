package com.swg.weatherbasic.service.impl;

import com.swg.weatherbasic.pojo.City;
import com.swg.weatherbasic.service.DataClient;
import com.swg.weatherbasic.vo.WeatherResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author 【swg】.
 * @Date 2018/11/26 10:27
 * @DESC 断路器默认返回的值
 * @CONTACT 317758022@qq.com
 */
@Component
public class DataClientFallback implements DataClient {
    @Override
    public List<City> listCity() throws Exception {
        List<City> cityList = new ArrayList<>();
        City city = new City();
        city.setCityId("101190101");
        city.setCityName("默认的南京");

        cityList.add(city);
        return cityList;
    }

    @Override
    public WeatherResponse getDataByCityId(String cityId) {
        return null;
    }
}
