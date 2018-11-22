package com.swg.weatherbasic.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swg.weatherbasic.service.IWeatherDataService;
import com.swg.weatherbasic.vo.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 14:46
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Service
@Slf4j
public class WeatherDataServiceImpl implements IWeatherDataService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    private static final String WEATHER_URI = "http://wthrcdn.etouch.cn/weather_mini?";

    private static final long TIME_OUT = 30*60L;

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
        //先去缓存中查询，有就直接拿缓存中的数据，否则调用接口
        String key = uri;
        String strBody = null;
        WeatherResponse resp = null;
        ObjectMapper mapper = new ObjectMapper();

        ValueOperations<String,String> ops = stringRedisTemplate.opsForValue();


        if(stringRedisTemplate.hasKey(uri)){
            log.info("Redis has data!");
            strBody = ops.get(key);
        }else{
            log.info("Redis don't thas data!");
            ResponseEntity<String> resString = restTemplate.getForEntity(uri,String.class);



            if(resString.getStatusCodeValue() == 200) {
                strBody = resString.getBody();
            }

            //数据写入缓存
            ops.set(key,strBody,TIME_OUT, TimeUnit.SECONDS);
        }

        try {
            resp = mapper.readValue(strBody,WeatherResponse.class);
        }catch (IOException e){
            log.error("Error!",e);
        }

        return resp;
    }


}
