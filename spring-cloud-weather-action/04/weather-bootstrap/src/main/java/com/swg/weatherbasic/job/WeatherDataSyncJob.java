package com.swg.weatherbasic.job;

import com.swg.weatherbasic.pojo.City;
import com.swg.weatherbasic.service.ICityDataService;
import com.swg.weatherbasic.service.IWeatherDataService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.List;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 16:23
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Slf4j
public class WeatherDataSyncJob extends QuartzJobBean {
    @Autowired
    private IWeatherDataService weatherDataService;
    @Autowired
    private ICityDataService cityDataService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("天气数据同步任务开始");
        //获取城市列表
        List<City> cityList = null;
        try {
            cityList = cityDataService.listCity();
        } catch (Exception e) {
            log.error("获取城市列表失败！",e);
        }

        //遍历城市id获取天气
        for(City city:cityList){
            String cityId = city.getCityId();
            log.info("定时器更新了{}这个城市的天气信息", city.getCityName());
            weatherDataService.syncDataByCityId(cityId);
        }

        log.info("天气数据同步任务结束");
    }
}
