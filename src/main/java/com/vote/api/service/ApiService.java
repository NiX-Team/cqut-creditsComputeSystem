package com.vote.api.service;

import org.springframework.stereotype.Component;

@Component("aliDatavApiService")
public interface ApiService {
    String getTitle();
    String getType();
    String getVoteKeyAndValue();
    String getSumVote();
    String getTable();
}
