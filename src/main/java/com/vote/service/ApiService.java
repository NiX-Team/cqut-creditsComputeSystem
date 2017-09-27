package com.vote.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component("voteService")
public interface ApiService {
    boolean checkMd5(String md5);
    void saveSetting(String type, String[] keys, String title);
    String vote(String ip, String key);
    void settingRule(Long count, Long oneDayCount, Long oneSumCount, Long spacing, String[] openIps, String[] closeIps);
    boolean getOpenSetting();
    boolean isVoteOpen();
    void setVoteOpen(boolean voteOpen);
    void setOpenSetting(boolean openSetting);
}
