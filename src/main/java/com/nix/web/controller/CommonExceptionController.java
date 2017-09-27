package com.nix.web.controller;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by 11723 on 2017/5/3.
 */
public class CommonExceptionController extends SimpleMappingExceptionResolver{
    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response,Object handler, Exception ex) {
        if (ex instanceof WebException){
            return getModelAndView(((WebException) ex).getViewPath(),((WebException) ex).getMsg());
        }
        String viewName = determineViewName(ex, request);
        if (viewName != null) {
            Integer statusCode = determineStatusCode(request, viewName);
            if (statusCode != null) {
                applyStatusCodeIfPossible(request, response, statusCode);
            }
            return getModelAndView(viewName, ex, request);
        } else {
            return null;
        }
    }
    protected ModelAndView getModelAndView(String viewPath,String msg){
        ModelAndView modelAndView = new ModelAndView(viewPath);
        return modelAndView;
    }
}
