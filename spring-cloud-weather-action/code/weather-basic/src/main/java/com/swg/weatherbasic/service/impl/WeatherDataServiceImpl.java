package com.swg.weatherbasic.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swg.weatherbasic.service.IWeatherDataService;
import com.swg.weatherbasic.vo.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 14:46
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Service
public class WeatherDataServiceImpl implements IWeatherDataService {
    @Autowired
    private RestTemplate restTemplate;

    private static final String WEATHER_URI = "http://wthrcdn.etouch.cn/weather_mini?";

    @Override
    public WeatherResponse getDataByCityId(String cityId) {
        String uri = WEATHER_URI + "citykey="+cityId;

        return doGetWeather(uri);
    }

    @Override
    public WeatherResponse getDataByCityName(String cityName) {
        String uri = WEATHER_URI + "city="+cityName;

        return doGetWeather(uri);
    }

    private WeatherResponse doGetWeather(String uri){
        ResponseEntity<String> resString = restTemplate.getForEntity(uri,String.class);

        ObjectMapper mapper = new ObjectMapper();
        WeatherResponse resp = null;

        String strBody = null;
        if(resString.getStatusCodeValue() == 200){
            strBody = resString.getBody();
        }
        try {
            resp = mapper.readValue(strBody,WeatherResponse.class);
        }catch (IOException e){
            e.printStackTrace();
        }
        return resp;
    }


}
