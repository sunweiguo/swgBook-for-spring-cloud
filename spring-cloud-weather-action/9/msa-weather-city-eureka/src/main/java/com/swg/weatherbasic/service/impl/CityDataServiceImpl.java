package com.swg.weatherbasic.service.impl;

import com.swg.weatherbasic.pojo.City;
import com.swg.weatherbasic.pojo.CityList;
import com.swg.weatherbasic.service.ICityDataService;
import com.swg.weatherbasic.utils.XmlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 17:14
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Service
@Slf4j
public class CityDataServiceImpl implements ICityDataService {
    @Override
    public List<City> listCity() throws Exception {
        //读取xml文件
        Resource resource = new ClassPathResource("citylist.xml");
        //读取文件的buffer流
        BufferedReader bf = new BufferedReader(new InputStreamReader(resource.getInputStream(),"UTF-8"));
        StringBuffer buffer = new StringBuffer();
        String line = "";

        while((line = bf.readLine()) != null){
            buffer.append(line);
        }
        //此时数据已经读到buffer里了
        bf.close();

        //xml转换为java对象
        CityList cityList = (CityList) XmlBuilder.xmlStrToObj(CityList.class,buffer.toString());

        return cityList.getCityList();
    }
}
