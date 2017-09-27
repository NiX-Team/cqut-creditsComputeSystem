package com.vote.api.controller;

import com.vote.api.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
@Controller
@RequestMapping(value = "/vote")
public class ApiController {
    @Autowired
    private ApiService apiService;

    /**
     * 获取实时参数(datav数据格式)
     * */
    @ResponseBody
    @RequestMapping(value = "/api",method = RequestMethod.GET)
    public String getVlaue(@RequestParam(value = "name")String name){
        switch (name){
            case "title" : return apiService.getTitle();
            case "table" : return apiService.getTable();
            case "chart" : return apiService.getVoteKeyAndValue();
            case "sum_vote" : return apiService.getSumVote();
        }
        return null;
    }
}
