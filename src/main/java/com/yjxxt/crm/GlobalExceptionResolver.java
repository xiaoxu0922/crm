package com.yjxxt.crm;


import com.alibaba.fastjson.JSON;
import com.yjxxt.crm.base.ResultInfo;
import com.yjxxt.crm.exceptions.NoLoginException;
import com.yjxxt.crm.exceptions.ParamsException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class GlobalExceptionResolver implements HandlerExceptionResolver {
    /**
     * 方法返回值类型
     * 视图
     * JSON
     * 如何判断方法的返回类型：
     * 如果方法级别配置了 @ResponseBody 注解，表示方法返回的是JSON；
     * 反之，返回的是视图页面
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) {
        //未登录异常
        if(ex instanceof NoLoginException){
            ModelAndView mav = new ModelAndView("redirect:/index");
            return mav;
        }
        //实例化对象
        ModelAndView mav = new ModelAndView("error");
        //存储
        mav.addObject("code", 300);
        mav.addObject("msg", "服务器崩溃啦！");
        // 判断 HandlerMethod
        if (handler instanceof HandlerMethod) {
            HandlerMethod h = (HandlerMethod) handler;
            ResponseBody responseBody = h.getMethod().getDeclaredAnnotation(ResponseBody.class);
            //判断是否有ResponseBody：有ResponseBody，表示方法返回的是JSON；
            if (responseBody == null) {
                //返回视图名称
                if (ex instanceof ParamsException) {
                    ParamsException pe = (ParamsException) ex;
                    mav.addObject("code", pe.getCode());
                    mav.addObject("msg", pe.getMsg());
                }
                return mav;
            } else {
                //返回json resultInfo
                ResultInfo resultInfo = new ResultInfo();
                resultInfo.setCode(300);
                resultInfo.setMsg("参数异常");
                //返回视图名称
                if (ex instanceof ParamsException) {
                    ParamsException pe = (ParamsException) ex;
                    resultInfo.setCode(pe.getCode());
                    resultInfo.setMsg(pe.getMsg());
                }
                //响应resultInfo
                resp.setContentType("application/json;charset=utf-8");
                PrintWriter pw = null;
                try {
                    pw = resp.getWriter();
                    //resultInfo-->json
                    pw.write(JSON.toJSONString(resultInfo));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (pw != null) {
                        pw.close();
                    }
                }
                return null;
            }
        }
        return mav;
    }
}