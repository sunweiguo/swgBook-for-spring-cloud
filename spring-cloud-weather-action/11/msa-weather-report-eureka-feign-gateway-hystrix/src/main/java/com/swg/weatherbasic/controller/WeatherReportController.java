package com.swg.weatherbasic.controller;

import com.swg.weatherbasic.service.DataClient;
import com.swg.weatherbasic.service.IWeatherReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author 【swg】.
 * @Date 2018/11/20 16:03
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@RestController
@RequestMapping("/report")
public class WeatherReportController {
    @Autowired
    private DataClient client;
    @Autowired
    private IWeatherReportService weatherReportService;

    @GetMapping("/cityId/{cityId}")
    public ModelAndView getReportByCityId(@PathVariable("cityId") String cityId, Model model) throws Exception {
        model.addAttribute("title","蜗牛天气预报");
        model.addAttribute("cityId",cityId);
        model.addAttribute("cityList",client.listCity());
        model.addAttribute("report",weatherReportService.getDataByCityId(cityId));
        return new ModelAndView("weather/report","reportModel",model);
    }

}
