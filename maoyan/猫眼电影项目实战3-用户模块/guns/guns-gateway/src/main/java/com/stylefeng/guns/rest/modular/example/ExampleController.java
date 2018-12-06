package com.stylefeng.guns.rest.modular.example;

import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.common.SimpleObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 常规控制器
 *
 * @author fengshuonan
 * @date 2017-08-23 16:02
 */
@Controller
@RequestMapping("/hello")
public class ExampleController {

    @RequestMapping("")
    public ResponseEntity hello() {

        System.out.println(CurrentUser.getUserId());

        return ResponseEntity.ok("您当前的id为："+CurrentUser.getUserId());
    }
}
