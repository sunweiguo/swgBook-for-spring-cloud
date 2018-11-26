package com.swg.weatherbasic.service.impl;

import com.swg.weatherbasic.constants.Constant;
import com.swg.weatherbasic.service.IWeatherDataCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @Author 【swg】.
 * @Date 2018/11/21 10:45
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Service
public class WeatherDataCollectionServiceImpl implements IWeatherDataCollectionService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    private static final String WEATHER_URI = "http://wthrcdn.etouch.cn/weather_mini?";
    @Override
    public void syncDataByCityId(String cityId) {
        String uri = WEATHER_URI + "citykey="+cityId;
        this.saveWeatherData(uri);
    }


    private void saveWeatherData(String uri){
        //先去缓存中查询，有就直接拿缓存中的数据，否则调用接口
        String key = uri;
        String strBody = null;

        ValueOperations<String,String> ops = stringRedisTemplate.opsForValue();


        ResponseEntity<String> resString = restTemplate.getForEntity(uri,String.class);

        if(resString.getStatusCodeValue() == 200) {
            strBody = resString.getBody();
        }

        //数据写入缓存
        ops.set(key,strBody, Constant.TIME_OUT, TimeUnit.SECONDS);

    }
}
