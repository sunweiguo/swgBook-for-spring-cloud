package com.swg.weatherbasic.service.impl;

import com.swg.weatherbasic.pojo.Forecast;
import com.swg.weatherbasic.pojo.Weather;
import com.swg.weatherbasic.service.IWeatherReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author 【swg】.
 * @Date 2018/11/20 16:02
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Service
@Slf4j
public class WeatherReportServiceImpl implements IWeatherReportService {

    @Override
    public Weather getDataByCityId(String cityId) {
        //TODO 从另一个服务获取
        Weather weather = new Weather();
        weather.setAqi("23");
        weather.setCity("南京");
        weather.setGanmao("容易感冒");
        weather.setWendu("10");

        List<Forecast> forecastList = new ArrayList<>();
        Forecast forecast = new Forecast();
        forecast.setDate("10号星期天");
        forecast.setFengli("无风");
        forecast.setType("晴天");
        forecast.setHigh("高温11度");
        forecast.setLow("低温11度");
        forecastList.add(forecast);

        forecast = new Forecast();
        forecast.setDate("11号星期天");
        forecast.setFengli("无风");
        forecast.setType("晴天");
        forecast.setHigh("高温11度");
        forecast.setLow("低温11度");
        forecastList.add(forecast);

        forecast = new Forecast();
        forecast.setDate("12号星期天");
        forecast.setFengli("无风");
        forecast.setType("晴天");
        forecast.setHigh("高温11度");
        forecast.setLow("低温11度");
        forecastList.add(forecast);

        forecast = new Forecast();
        forecast.setDate("13号星期天");
        forecast.setFengli("无风");
        forecast.setType("晴天");
        forecast.setHigh("高温11度");
        forecast.setLow("低温11度");
        forecastList.add(forecast);

        forecast = new Forecast();
        forecast.setDate("14号星期天");
        forecast.setFengli("无风");
        forecast.setType("晴天");
        forecast.setHigh("高温11度");
        forecast.setLow("低温11度");
        forecastList.add(forecast);

        weather.setForecast(forecastList);

        return weather;
    }
}
