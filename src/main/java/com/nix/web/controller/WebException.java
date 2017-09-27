package com.nix.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by 11723 on 2017/5/3.
 */
@Component
public class WebException extends Exception{
    private String msg;
    private String viewPath;

    public WebException(String msg){
        this.viewPath = "WEB/view/error.html";
        this.msg = msg;
    }
    public WebException(){

    }
    public WebException(String viewPath, String msg){
        this.msg = msg;
        this.viewPath = viewPath;
    }
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getViewPath() {
        return viewPath;
    }

    public void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

}
