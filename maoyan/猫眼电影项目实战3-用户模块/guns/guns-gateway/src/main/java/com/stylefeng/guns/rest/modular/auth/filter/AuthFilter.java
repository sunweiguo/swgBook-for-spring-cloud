package com.stylefeng.guns.rest.modular.auth.filter;

import com.stylefeng.guns.core.util.RenderUtil;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.config.properties.JwtProperties;
import com.stylefeng.guns.rest.modular.auth.util.JwtTokenUtil;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import io.jsonwebtoken.JwtException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 对客户端请求的jwt token验证过滤器
 *
 * @author fengshuonan
 * @Date 2017/8/24 14:04
 */
public class AuthFilter extends OncePerRequestFilter {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        //1.如果是http://ip/auth这个路径，说明是登陆的路径，直接放过
        if (request.getServletPath().equals("/" + jwtProperties.getAuthPath())) {
            chain.doFilter(request, response);
            return;
        }

        //2.配置其他的忽略列表
        String ignoreUrl = jwtProperties.getIgnoreUrl();
        String[] ignoreUrls = ignoreUrl.split(",");
        for(String url:ignoreUrls){
            System.out.println("----"+url);
            System.out.println(request.getServletPath());
            if(request.getServletPath().equals(url)){
                //忽略
                chain.doFilter(request, response);
                return;
            }
        }

        //3.如果不是要放过的页面，表示访问的页面都是需要登陆后才能访问，那么正常情况下，我们都是可以获取到登陆时存入token的userid的
        //那么每次请求过来时，都获取一下userid存进ThreadLocal里面，并且验证token是否过期
        final String requestHeader = request.getHeader(jwtProperties.getHeader());
        String authToken = null;
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            authToken = requestHeader.substring(7);

            //通过token获取userId，并且存进threadLocal
            String userId = jwtTokenUtil.getUsernameFromToken(authToken);
            if(userId == null){
                return;
            }
            CurrentUser.saveUserId(userId);

            //验证token是否过期,包含了验证jwt是否正确
            try {
                boolean flag = jwtTokenUtil.isTokenExpired(authToken);
                if (flag) {
                    //RenderUtil.renderJson(response, new ErrorTip(BizExceptionEnum.TOKEN_EXPIRED.getCode(), BizExceptionEnum.TOKEN_EXPIRED.getMessage()));
                    RenderUtil.renderJson(response,ResponseVO.serviceFail("token过期啦"));
                    return;
                }
            } catch (JwtException e) {
                //有异常就是token解析失败
                //RenderUtil.renderJson(response, new ErrorTip(BizExceptionEnum.TOKEN_ERROR.getCode(), BizExceptionEnum.TOKEN_ERROR.getMessage()));
                RenderUtil.renderJson(response,ResponseVO.serviceFail("token无效"));
                return;
            }
        } else {
            //header没有带Bearer字段
            //RenderUtil.renderJson(response, new ErrorTip(BizExceptionEnum.TOKEN_ERROR.getCode(), BizExceptionEnum.TOKEN_ERROR.getMessage()));
            RenderUtil.renderJson(response,ResponseVO.serviceFail("token格式错误"));
            return;
        }
        chain.doFilter(request, response);
    }
}