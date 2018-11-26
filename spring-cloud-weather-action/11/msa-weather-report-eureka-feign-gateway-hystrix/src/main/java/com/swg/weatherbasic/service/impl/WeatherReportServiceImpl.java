package com.swg.weatherbasic.service.impl;

import com.swg.weatherbasic.pojo.Weather;
import com.swg.weatherbasic.service.DataClient;
import com.swg.weatherbasic.service.IWeatherReportService;
import com.swg.weatherbasic.vo.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author 【swg】.
 * @Date 2018/11/20 16:02
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Service
@Slf4j
public class WeatherReportServiceImpl implements IWeatherReportService {
    @Autowired
    private DataClient dataClient;

    @Override
    public Weather getDataByCityId(String cityId) {
        WeatherResponse res = dataClient.getDataByCityId(cityId);
        Weather weather = null;
        if(res != null){
            weather = res.getData();
        }
        return weather;
    }
}
