package com.nix.controller;

import com.nix.controller.base.BaseController;
import com.nix.service.CommonService;
import com.nix.service.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/nix")
public class CommonController extends BaseController{
    @Autowired
    private CommonService commonService;

    /**
     * 上传文件
     * @param file xls文件;
     * @return 返回xls的所有类标题 并返回当前计算单元的唯一id
     * */
    @ResponseBody
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public Map<String,Object> upload(@RequestParam(value = "file", required = false) MultipartFile file){
        Map<String,Object> map = new HashMap<>();
        if (!file.getOriginalFilename().matches(".*xls") && !file.getOriginalFilename().matches(".*xlsx")){
            map.put("status",FAIL);
            String filePath = Const.FILE_PATH + String.valueOf(System.currentTimeMillis()) + String.valueOf((int)Math.random()*10000) + file.getOriginalFilename();
            File file1 = new File(filePath);
            try {
                file.transferTo(file1);
                System.out.println(file1.getName());
                System.out.println("保存文件成功");
            } catch (IOException e) {
                System.out.println("保存文件失败");
                e.printStackTrace();
            }
            map.put("msg","不支持该后缀的excel表");
            return map;
        }
        map.put("status",SUCCESS);
        map.putAll(commonService.getParamMsg(file));
        return map;
    }

    /**
     *根据筛选条件选出数据并计算
     * */
    @ResponseBody
    @RequestMapping(value = "/result",method = RequestMethod.POST)
    public Map<String,Object> result(@RequestParam(value = "id")Integer id,@RequestParam(value = "credits")Integer credits,
                      @RequestParam(value = "results")Integer results,@RequestParam(value = "courses") Integer courses,
                      @RequestParam(value = "columns")String[] columns,@RequestParam(value = "formula") String formula){
        if (id == null || results == null || results == null || columns == null || courses == null) return null;
        Map<String,Object> map = new HashMap<>();
        if (!commonService.inspectionFormula(formula)){
            map.put("status",FAIL);
            map.put("msg","公式格式错误");
            return map;
        }
        try {
            map.put("content",commonService.result(String.valueOf(id),credits,results,courses,columns,formula));
            map.put("status",SUCCESS);
        }catch (Exception e){
            map.put("status",FAIL);
            map.put("msg","课程行选择错误或者列选择有错误(请确保事例同学的重修课程，个人选修课程一剔除)");
            return map;
        }

        return map;
    }


    /**
     * 根据计算单元id值获取excel信息返回前端筛选
     * */
    @ResponseBody
    @RequestMapping(value = "/{id}",method = RequestMethod.POST)
    public Map<String,Object> getExcelMsgById(@PathVariable("id") String id){
        Map<String,Object> map = commonService.getParamMsg(id);
        if (map == null){
            map.put("status",FAIL);
            map.put("msg","错误id");
            return map;
        }
        map.put("status",SUCCESS);
        return map;
    }

    /**
     * 获取下载地址
     * */
    @ResponseBody
    @RequestMapping("/download_url")
    public Map<String,Object> getDownloadUrl(@RequestParam(value = "id")String id){
        String fileName = Const.getFileName(id).replaceAll(Const.FILE_PATH,"");
        HashMap<String,Object> map = new HashMap<>();
        map.put("redirect", "/nix/download.do?fileName=" + fileName);
        return map;
    }
    /**
     * 断点续传
     * */
    @ResponseBody
    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response,@RequestParam(value = "fileName")String fileName) {
        String path = Const.FILE_PATH + fileName;
        File file = new File(path);
        FileInputStream in = null;
        ServletOutputStream out = null;
        try {
            response.reset();
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/x-download");
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Length", String.valueOf(file.length()));
            response.setHeader("Content-Disposition", "attachment; filename=\"" + new String(fileName.getBytes("UTF-8"), "iso-8859-1") + "\"");
            in = new FileInputStream(file.getPath());
            long pos = 0;
            if (null != request.getHeader("Range")) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                try {
                    pos = Long.parseLong(request.getHeader("Range").replaceAll("bytes=", "").replaceAll("-", ""));
                } catch (NumberFormatException e) {
                    pos = 0;
                }
            }
            out = response.getOutputStream();
            String contentRange = new StringBuffer("bytes ").append(pos + "").append("-").append((file.length() - 1) + "").append("/").append(file.length() + "").toString();
            response.setHeader("Content-Range", contentRange);
            in.skip(pos);
            byte[] buffer = new byte[1024 * 10];
            int length = 0;
            while ((length = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, length);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
