package com.nix.service;

import com.nix.util.log.LogKit;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Const {
    static {
        maintenance();
    }
    private final static ConcurrentHashMap<String,Object> COMPUTATIONS_ID = new ConcurrentHashMap();//
    public final static String FILE_PATH = "E://test/";//xls文件保存到文件夹路径
    public final static Integer TIME_OUT_ID = 60*24;//分钟


    public final static synchronized String createId(String fileName){
        while (true) {
            int id = (int) (1000000 + (Math.random() * 10000000));
            if (COMPUTATIONS_ID.contains(id)) continue;
            COMPUTATIONS_ID.put(String.valueOf(id),fileName);
            COMPUTATIONS_ID.put(id + "_time",System.currentTimeMillis());
            LogKit.info(Const.class,"文件保存成功.id:" + id + "  file_name:" + fileName);
            return String.valueOf(id);
        }
    }
    public final static String getFileName(String id){
        return (String) COMPUTATIONS_ID.get(id);
    }

    public final static Date getCreateTime(String id){
        return new Date((Long) COMPUTATIONS_ID.get(id + "_time"));
    }

    public final static synchronized void removeMap(String id){
        COMPUTATIONS_ID.remove(id);
    }

    private static void maintenance(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Long now = System.currentTimeMillis();
                        for (Map.Entry<String, Object> entry : Const.COMPUTATIONS_ID.entrySet()) {
                            if (entry.getKey().matches("[\\d]+")) {
                                String id = entry.getKey();
                                Long time = (Long) Const.COMPUTATIONS_ID.get(id + "_time");
                                if (time + 1000 * 60 * TIME_OUT_ID < now) {
                                    File file = new File((String) entry.getValue());
                                    LogKit.info(Const.class,"id:" + id + "过期 开始删除对应文件");
                                    file.delete();
                                    removeMap(id);
                                    removeMap(id + "_time");
                                }
                            }
                        }
                        LogKit.info(Const.class,"维护文件id线程开始休眠");
                        Thread.sleep(24*60*60*1000);
                    }catch (Exception e){
                        LogKit.error(Const.class,"维护文件id线程休眠失败");
                    }
                }
            }
        }).start();
    }
}
