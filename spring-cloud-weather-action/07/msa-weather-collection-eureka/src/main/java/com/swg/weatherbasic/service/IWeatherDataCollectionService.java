package com.swg.weatherbasic.service;

/**
 * @Author 【swg】.
 * @Date 2018/11/21 10:44
 * @DESC 天气数据采集服务
 * @CONTACT 317758022@qq.com
 */
public interface IWeatherDataCollectionService {
    /**
     * 根据城市ID同步天气
     * @param cityId
     */
    void syncDataByCityId(String cityId);
}
