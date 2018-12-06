package com.stylefeng.guns.rest.modular.vo;

import lombok.Data;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 21:43
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Data
public class ResponseVO<M> {
    //0标识成功，1标识业务失败，999标识系统异常
    private int status;
    private String msg;
    private M data;

    private ResponseVO(){}

    public static<M> ResponseVO success(M data){
        ResponseVO responseVO = new ResponseVO();
        responseVO.setStatus(0);
        responseVO.setData(data);
        return responseVO;
    }

    public static<M> ResponseVO success(String smg){
        ResponseVO responseVO = new ResponseVO();
        responseVO.setStatus(0);
        responseVO.setMsg(smg);
        return responseVO;
    }

    public static<M> ResponseVO serviceFail(String message){
        ResponseVO responseVO = new ResponseVO();
        responseVO.setStatus(1);
        responseVO.setMsg(message);
        return responseVO;
    }

    public static<M> ResponseVO appFail(String message){
        ResponseVO responseVO = new ResponseVO();
        responseVO.setStatus(999);
        responseVO.setMsg(message);
        return responseVO;
    }
}
