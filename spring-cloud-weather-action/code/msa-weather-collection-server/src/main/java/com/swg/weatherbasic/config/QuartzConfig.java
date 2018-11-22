package com.swg.weatherbasic.config;

import com.swg.weatherbasic.constants.Constant;
import com.swg.weatherbasic.job.WeatherDataSyncJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 16:24
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Configuration
public class QuartzConfig {

    //定义一个jobDetail,就是注册一个定时任务，具体如何执行时在WeatherDataSyncJob中定义
    //具体何时执行，是下面的Trigger定义
    @Bean
    public JobDetail weatherDataSyncDetail(){
        return JobBuilder.newJob(WeatherDataSyncJob.class).
                withIdentity("WeatherDataSyncJob").
                storeDurably().build();
    }
    //触发器
    @Bean
    public Trigger weatherDataSyncTrigger(){
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                    .simpleSchedule()
                        .withIntervalInSeconds(Constant.JOB_INTERVAL)//两秒去自动执行一次
                            .repeatForever();
        return TriggerBuilder.newTrigger().forJob(weatherDataSyncDetail())
                .withIdentity("weatherDataSyncTrigger")
                .withSchedule(scheduleBuilder).build();
    }

}
