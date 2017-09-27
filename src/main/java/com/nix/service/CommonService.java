package com.nix.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface CommonService {
    Map getParamMsg(MultipartFile file);
    Map getParamMsg(String id);
    Map result(String id, Integer credits, Integer results, Integer courses, String[] column, String formula);
    boolean inspectionFormula(String formula);
}
