package com.vote.controller;

import com.nix.controller.base.BaseController;
import com.nix.util.Util;
import com.nix.util.log.LogKit;
import com.vote.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.enterprise.context.Destroyed;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/vote/system")
public class SystemController extends BaseController{
    @Autowired
    private ApiService apiService;

    /**
     * 设置投票系统的属性
     * */
    @ResponseBody
    @RequestMapping(value = "/setting",method = RequestMethod.POST)
    public Map<String,Object> setting(@RequestParam(value = "md5",defaultValue = "") String md5, @RequestParam(value = "type",defaultValue = "队伍")String type,
                                      @RequestParam(value = "keys",defaultValue = "{}")String[] keys, @RequestParam(value = "title",defaultValue = "")String title){
        Map<String,Object> map = new HashMap<>();
        if (md5 == null || !apiService.checkMd5(md5)){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"权限码错误");
            return map;
        }
        if (title.isEmpty()){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"请输入标题");
            return map;
        }
        if (keys.length == 0){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"投票对象组不能为空");
            return map;
        }
        if (!apiService.getOpenSetting()){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"系统运行中，无法初始化");
            return map;
        }
        apiService.saveSetting(type,keys,title);
        map.put(STATUS,SUCCESS);
        return map;
    }

    /**
     * 设置投票系统投票规则
     * @param md5
     *  md5授权码
     * @param count
     *  当前投票项目一共有多少票 当投票满后将无法投票
     * @param oneDayCount
     *  一个人一次能够投给多少候选组多少票（一个ip地址一个人）默认一个ip一票 -1表示不限制
     * @param oneSumCount
     *  一个人总共能够投多少票 默认-1 表示不开启个人总票计数
     * @param spacing
     *  一个人间隔多久能再次投票（单位秒） 默认-1 表示不支持再次投票
     * @param openIps
     *  ip白名单
     *  @param closeIps
     *  ip黑名单
     *
     * */
    @ResponseBody
    @RequestMapping(value = "/rule",method = RequestMethod.POST)
    public Map<String,Object> rule(@RequestParam(value = "md5",defaultValue = "") String md5,@RequestParam(value = "count",defaultValue = "-1") Long count,@RequestParam(value = "one_day_count",defaultValue = "1") Long oneDayCount,
                                   @RequestParam(value = "one_sum_count",defaultValue = "-1") Long oneSumCount,@RequestParam(value = "spacing",defaultValue = "-1") Long spacing,@RequestParam(value = "open_ips",defaultValue = "{}")String[] openIps,
                                   @RequestParam(value = "close_ips",defaultValue = "{}")String[] closeIps){
        Map<String,Object> map = new HashMap<>();
        if (md5.isEmpty() || !apiService.checkMd5(md5)){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"权限码错误");
            return map;
        }
        if (apiService.getOpenSetting()){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"系统未初始化");
            return map;
        }
        if (openIps.length > 0 && closeIps.length > 0){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"ip白名单和ip黑名单不能同时存在");
            return map;
        }
        String[] ips = openIps.length == 0 ? closeIps : openIps;
        for (String ip:ips) {
            if (!Util.isLegitimacy(ip)){
                map.put(STATUS,FAIL);
                map.put(MESSAGE,"ip不合法");
                return map;
            }
        }
        apiService.settingRule(count,oneDayCount,oneSumCount,spacing,openIps,closeIps);
        map.put(STATUS,SUCCESS);
        return map;
    }
    /**
     * 投票
     * */
    @ResponseBody
    @RequestMapping(value = "/vote",method = RequestMethod.GET)
    public Map<String,Object> vote(HttpServletRequest request,@RequestParam(value = "key",defaultValue = "") String key){
        Map<String,Object> map = new HashMap<>();
        if (key.isEmpty()){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"请选择投票项");
            return map;
        }
        if (!apiService.isVoteOpen()){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"投票未开始");
            return map;
        }
        String msg = apiService.vote(Util.getIpAddress(request),key);
        if ( msg  != null){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,msg);
            return map;
        }

        map.put(STATUS,SUCCESS);
        LogKit.info(this.getClass(), Util.getIpAddress(request) + "给" +key + "投一票" );
        return map;
    }

    /**
     * 设置是否开启投票
     * */
    @ResponseBody
    @RequestMapping(value = "/is_vote",method = RequestMethod.GET)
    public Map<String,Object> stopVote(@RequestParam(value = "md5",defaultValue = "") String md5,@RequestParam(value = "vote")Boolean vote){
        Map<String,Object> map = new HashMap<>();
        if (md5.isEmpty() || !apiService.checkMd5(md5)){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"权限码错误");
            return map;
        }
        apiService.setVoteOpen(vote);
        map.put(STATUS,SUCCESS);
        return map;
    }

    /**
     * 本次投票结束
     * */
    @ResponseBody
    @RequestMapping(value = "/stop",method = RequestMethod.GET)
    public Map<String,Object> closeVote(@RequestParam(value = "md5",defaultValue = "") String md5){
        Map<String,Object> map = new HashMap<>();
        if (md5 == null || !apiService.checkMd5(md5)){
            map.put(STATUS,FAIL);
            map.put(MESSAGE,"权限码错误");
            return map;
        }
        apiService.setOpenSetting(true);
        map.put(STATUS,SUCCESS);
        return map;
    }
}
