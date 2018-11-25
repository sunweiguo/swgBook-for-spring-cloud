package com.swg.weatherbasic.utils;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.Reader;
import java.io.StringReader;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 17:09
 * @DESC xml转换为对象
 * @CONTACT 317758022@qq.com
 */
public class XmlBuilder {
    public static Object xmlStrToObj(Class<?> clazz,String xmlStr) throws Exception{
        Object xmlObject = null;
        Reader reader = null;
        JAXBContext context = JAXBContext.newInstance(clazz);
        //xml转为对象的接口
        Unmarshaller unmarshaller = context.createUnmarshaller();

        reader = new StringReader(xmlStr);
        xmlObject = unmarshaller.unmarshal(reader);

        if(null != reader){
            reader.close();
        }

        return xmlObject;
    }
}
