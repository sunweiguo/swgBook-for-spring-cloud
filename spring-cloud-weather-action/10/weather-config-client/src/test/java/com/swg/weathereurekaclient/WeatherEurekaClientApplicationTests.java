package com.swg.weathereurekaclient;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WeatherEurekaClientApplicationTests {
    @Value("${auther}")
    private String auther;

    @Test
    public void contextLoads() {
        System.out.println("===="+auther);
        Assert.assertEquals("oursnail.cn",auther);
    }

}
