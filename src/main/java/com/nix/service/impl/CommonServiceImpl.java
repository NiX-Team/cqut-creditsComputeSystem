package com.nix.service.impl;

import com.nix.service.CommonService;
import com.nix.service.Const;
import com.nix.util.Util;
import com.nix.util.log.LogKit;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Member;
import java.util.*;
@Service
public class CommonServiceImpl implements CommonService{

    private final static String CREDIT = "credit";
    private final static String GRADE = "grade";
    private final static String SUM_CREDIT = "sum_credit";
    private final static String COURSE_COUNT = "course_count";

    /**
     * 获取成绩统计表的列标题和当前学年的课程
     * @param file
     *  成绩统计表的excel文件
     * @return
     *  返回{"column":list,"courses":list}
     * */
    @Override
    public Map<String,Object> getParamMsg(MultipartFile file) {
        Workbook workbook ;
        Map<String,Object> map = new HashMap<>();
        try {
            workbook = getWorkbook(file.getOriginalFilename(),  file.getInputStream());
            map.put("file_id", Const.createId(createFile(file)));
            map.put("create_time",Const.getCreateTime((String) map.get("file_id")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        map.put("column",getColumn(workbook));
        map.put("courese",getCourses(workbook));
        return map;
    }


    @Override
    public Map<String,Object> getParamMsg(String id) {
        Workbook workbook ;
        Map<String,Object> map = new HashMap<>();
        try {
            File file = new File(Const.getFileName(id));
            workbook = getWorkbook(file.getName(), new FileInputStream(file));
            map.put("file_id", id);
            map.put("create_time",Const.getCreateTime(id));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        map.put("column",getColumn(workbook));
        map.put("courese",getCourses(workbook));
        return map;
    }

    /**
     * 获取计算结果
     * @param id
     *  每个计算单元唯一id（id用来获取文件）
     * @param credits
     *  学分所在的列
     * @param results
     *  成绩所在的列
     * @param courses
     *  课程所在列
     * @param columns
     *  筛选的课程列表
     * @param formula
     *  单科最终成绩的计算公式（A*B*0.8）
     *  参考学生学号
     * @return
     *  返回最终成绩列表（智育分）{"11503090201":"58","11503090202":"59"}
     * */
    @Override
    public Map<String,Float> result(String id, Integer credits, Integer results, Integer courses, String[] columns, String formula) {
        Workbook workbook;
        File file = new File(Const.getFileName(id));
        try {
            workbook = getWorkbook(file.getName(),  new FileInputStream(file));
        } catch (Exception e) {
            return null;
        }
        Map<String,Float> map = getStudentEndResult(getStudentMsg(workbook,credits,results,courses),columns,formula);
        writeResult(map,file);
        return map;
    }



    /**
     * 校验计算公式<br>
     * 公式变量有：单科学分(credit) 单科成绩(grade) 计算总学分(sum_credit) 总科目数(course_count)
     * */
    @Override
    public boolean inspectionFormula(String formula) {
        String[] words = formula.split("[\\W]");
        for (String word:words){
            try {
                Float.parseFloat(word);
            }catch (NumberFormatException e){
                if (!(word.equals(CREDIT)  || word.equals(GRADE) || word.equals(SUM_CREDIT) || word.equals(COURSE_COUNT)))
                    return false;
            }
        }
        return true;
    }

    /**
     * 对最终成绩按学号排序
     * */
    private String[] sort(Map<String,Float> map){
        String[] student_id = new String[map.size()];
        int i = 0;
        for (Map.Entry<String,Float> entry:map.entrySet()){
            student_id[i++] = entry.getKey();
        }
        Arrays.sort(student_id);
        return student_id;
    }

    /**
     * 计算完成后把结果保存入文件
     * */
    private void writeResult(Map<String,Float> result,File file){
        String fileName = file.getName();
        file.delete();
        Workbook workbook;
        try {
            workbook = getWorkbook(Const.FILE_PATH + fileName,null);
            Sheet sheet = workbook.createSheet("统计成绩结果");
            int i = 0;
            Row row = sheet.createRow(i++);
            CellStyle style = workbook.createCellStyle();
            sheet.setColumnWidth(0,30*256);
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
            style.setWrapText(true);
            Cell cell = row.createCell(0);
            cell.setCellStyle(style);
            cell.setCellValue("学号");
            cell = row.createCell(1);
            cell.setCellStyle(style);
            cell.setCellValue("最终成绩");
            String[] studentIds = sort(result);
            for (String id:studentIds){
                row = sheet.createRow(i++);
                cell = row.createCell(0);
                cell.setCellStyle(style);
                cell.setCellValue(id);
                cell = row.createCell(1);
                cell.setCellStyle(style);
                cell.setCellValue(result.get(id));
            }
            FileOutputStream fileOutputStream = new FileOutputStream(Const.FILE_PATH + fileName);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
        }catch (Exception e){
            LogKit.error(this.getClass(),"结果写入excel失败");
        }
    }



    /**
     * 根据参考学生筛选的课程算出每个学生课程的成绩
     * @param students
     *  获取到每个学生的全部成绩信息 数据结构
     *  {<br>
     *  "11503090237",[{"课程1":"成绩-学分"},{"课程2":"成绩-学分"}],<br>
     *  "11503090232",[{"课程1":"成绩-学分"},{"课程2":"成绩-学分"}]<br>
     *  }
     * @param columns
     *  前端根据参考学生筛选课程名列表
     * @param formula
     *  最终成绩的计算公式(A*B*0.8)
     * @return
     *  返回每个学生的最终成绩（智育分）{"11503090201":"58","11503090202":"59"}
     * */
    private Map<String,Float> getStudentEndResult(Map<String,Map<String,String>> students,String[] columns,String formula){
        Map<String,Float> results = new HashMap<>();
        for (Map.Entry<String,Map<String,String>> entry:students.entrySet()){
            getOneStudentResult(entry.getKey(), entry.getValue(),columns,formula,results);
        }
        return results;
    }

    /**
     * 计算一个学生的最终成绩
     * @param studentId
     *  单独计算学生的学号
     * @param coursesMap
     *  单独一个学生的课程统计表
     * @param columns
     *  前端根据参考学生筛选课程名列表 数据结构<br>
     *  {<br>
     *  "11503090237",[{"课程1":"成绩-学分"},{"课程2":"成绩-学分"}]<br>
     *  }
     * @param formula
     *  最终成绩的计算公式(A*B*0.8)
     * */
    private void getOneStudentResult(String studentId,Map<String,String> coursesMap,String[] columns,String formula,Map<String,Float> results){
        float endResult = 0f;
        float sumCredit = 0f;
        int courseCount = columns.length;
        //算出总学分
        for (String column:columns){
            String result_credit =  coursesMap.get(column);
            float credit = Float.parseFloat(result_credit.split("-")[1]);
            sumCredit += credit;
        }
        for (String column:columns){
            String result_credit =  coursesMap.get(column);
            float result = stringToResult(result_credit.split("-")[0],null);
            float credit = Float.parseFloat(result_credit.split("-")[1]);
            endResult += parseFormula(formula,result,credit,sumCredit,courseCount);
        }
        endResult = (float) (((int)(endResult * 100))/100.0);
        results.put(studentId, endResult);
    }
    /**
     * 解析计算公式
     * @param formula
     *  计算公式
     * @param result
     *  单科成绩
     * @param credit
     *  单科学分
     * @param sumCredit
     *  需计算的总学分
     * @param courseCount
     *  需计算的总科目数
     * @return
     *  返回单科最终成绩
     * */
    private float parseFormula(String formula,float result,float credit,float sumCredit,int courseCount){
        Map<String,Object> mapping = new HashMap<>();
        mapping.put(CREDIT,Double.valueOf(credit));
        mapping.put(GRADE,Double.valueOf(result));
        mapping.put(SUM_CREDIT,Double.valueOf(sumCredit));
        mapping.put(COURSE_COUNT,Double.valueOf(courseCount));
        return Float.parseFloat(Util.dealEquation(Util.toSuffix(formula),mapping));
    }
    /**
     * 根据对应关系获取成绩
     * */
    private float stringToResult(String result,Map<String,Float> mapping){
        try {
            return Float.parseFloat(result);
        } catch (NumberFormatException e){
            return mapping.get(result);
        }
    }

    /**
     * 获取每个学生的成绩信息
     * @return
     *  map结构{"11503090237":[{"课程1":"成绩-学分"},{"课程2":"成绩-学分"}]}
     * */
    private Map<String,Map<String,String>> getStudentMsg(Workbook workbook,Integer credits, Integer results,Integer courses){
        Sheet sheet = workbook.getSheetAt(0);
        Map<String,Map<String,String>> student = new HashMap();
        String studentId = null;
        int i = 0,j = 0;
        Map<String,String> coursesMap = null;
        for (Iterator<Row> row = sheet.rowIterator(); row.hasNext();i++) {
            if (i == 0){row.next();continue;}
            Row row1 = row.next();
            if (studentId == null || !studentId.equals(row1.getCell(0).toString())){
                studentId = row1.getCell(0).toString();
                coursesMap = new HashMap();
                student.put(studentId,coursesMap);
            }
            if (coursesMap.containsKey(row1.getCell(courses).toString())) continue;
            coursesMap.put(row1.getCell(courses).toString(),row1.getCell(results).toString() + "-" + row1.getCell(credits));
        }
        return student;
    }



    /**
     * 获取Workbook
     * */
    private Workbook getWorkbook(String fileName,InputStream inputStream){
        Workbook workbook = null;
        try {
            if (fileName.matches(".*xls")) {
                if (inputStream != null) workbook = new HSSFWorkbook(inputStream);
                else workbook = new HSSFWorkbook();
            }
            else if (fileName.matches(".*xlsx")) {
                if (inputStream != null) workbook = new XSSFWorkbook(inputStream);
                else workbook = new XSSFWorkbook();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return workbook;
    }

    /**
     * 上传文件永久化到硬盘
     * */
    private String createFile(MultipartFile file) throws IOException {
        String filePath = Const.FILE_PATH + String.valueOf(System.currentTimeMillis()) + String.valueOf((int)Math.random()*10000) + file.getOriginalFilename();
        File file1 = new File(filePath);
        file.transferTo(file1);
        return filePath;
    }

    /**
     * 获取excel表的标题
     * */
    private List getColumn(Workbook workbook){
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(0);
        List titles = new ArrayList();
        for (Iterator<Cell> it = row.cellIterator(); it.hasNext(); ) {
            Cell cell = it.next();
            titles.add(cell.getStringCellValue());
        }
        return titles;
    }
    /**
     * 算出当前学年的课程
     * <br>
     * 默认第一列存放学生学号
     * */
    private List getCourses(Workbook workbook){
        Sheet sheet = workbook.getSheetAt(0);
        int i = 0,j;
        boolean isReadAll = false;
        List<List> lists = new ArrayList<>();
        String studentId = null;
        for (Iterator<Row> row = sheet.rowIterator(); row.hasNext();i++) {
            if (i == 0) {
                if (row.next().getCell(2) == null)
                    isReadAll = true;
                continue;
            }
            List column = new ArrayList();
            j = 0;
            for (Iterator<Cell> it = row.next().cellIterator(); it.hasNext(); j ++) {
                Cell cell = it.next();
                if (!isReadAll){
                    if (j == 0 && i == 1) {
                        studentId = cell.toString();
                    }else if (j == 0 && !studentId.equals(cell.toString()))
                        return lists;
                }
                column.add(cell.toString());
            }
            lists.add(column);
        }
        return lists;
    }

    public static void main(String[] args) {
       float endResult = 25.625352f;
       System.out.println((((int)(endResult * 100))/100.0));
    }
}

