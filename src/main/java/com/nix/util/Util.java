package com.nix.util;

import com.nix.util.log.LogKit;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Util {
    /**
     *提前将 符号的优先级定义好
     */
    private static final Map<Character, Integer> basic = new HashMap<Character, Integer>();
    static {
        basic.put('-', 1);
        basic.put('+', 1);
        basic.put('*', 2);
        basic.put('/', 2);
        basic.put('(', 0);//在运算中  （）的优先级最高，但是此处因程序中需要 故设置为0
    }

    /**
     * 将  中缀表达式  转化为  后缀表达式
     * <b>表达式仅支持运算符 数字字符 字母字符 下划线</b>
     */
    public final static String toSuffix(String infix){
        List<String> queue = new ArrayList<String>();                                    //定义队列  用于存储 数字  以及最后的  后缀表达式
        List<Character> stack = new ArrayList<Character>();                             //定义栈    用于存储  运算符  最后stack中会被 弹空

        char[] charArr = infix.trim().toCharArray();                                    //字符数组  用于拆分数字或符号
        String standard = "*/+-()";                                                        //判定标准 将表达式中会出现的运算符写出来
        char ch = '&';                                                                    //在循环中用来保存 字符数组的当前循环变量的  这里仅仅是初始化一个值  没有意义
        int len = 0;                                                                    //用于记录字符长度 【例如100*2,则记录的len为3 到时候截取字符串的前三位就是数字】
        for (int i = 0; i < charArr.length; i++) {                                        //开始迭代

            ch = charArr[i];                                                            //保存当前迭代变量
            if(Character.isDigit(ch)) {                                                    //如果当前变量为 数字
                len++;
            }else if(Character.isLetter(ch) || ch == '_') {                                            //如果当前变量为  字母
                len++;
            }else if(ch == '.'){                                                        //如果当前变量为  .  会出现在小数里面
                len++;
            }else if(Character.isSpaceChar(ch)) {                                        //如果当前变量为 空格  支持表达式中有空格出现
                if(len > 0) {                                                            //若为空格 代表 一段结束 ，就可以往队列中  存入了  【例如100 * 2  100后面有空格 就可以将空格之前的存入队列了】
                    queue.add(String.valueOf(Arrays.copyOfRange(charArr, i - len, i)));    //往 队列存入 截取的 字符串
                    len = 0;                                                            //长度置空
                }
                continue;                                                                //如果空格出现，则一段结束  跳出本次循环
            }else if(standard.indexOf(ch) != -1) {                                        //如果是上面标准中的 任意一个符号
                if(len > 0) {                                                            //长度也有
                    queue.add(String.valueOf(Arrays.copyOfRange(charArr, i - len, i)));    //说明符号之前的可以截取下来做数字
                    len = 0;                                                            //长度置空
                }
                if(ch == '(') {                                                            //如果是左括号
                    stack.add(ch);                                                        //将左括号 放入栈中
                    continue;                                                            //跳出本次循环  继续找下一个位置
                }
                if (!stack.isEmpty()) {                                                    //如果栈不为empty
                    int size = stack.size() - 1;                                        //获取栈的大小-1  即代表栈最后一个元素的下标
                    boolean flag = false;                                                //设置标志位
                    while (size >= 0 && ch == ')' && stack.get(size) != '(') {            //若当前ch为右括号，则 栈里元素从栈顶一直弹出，直到弹出到 左括号
                        queue.add(String.valueOf(stack.remove(size)));                    //注意此处条件：ch并未入栈，所以并未插入队列中；同样直到找到左括号的时候，循环结束了，所以左括号也不会放入队列中【也就是：后缀表达式中不会出现括号】
                        size--;                                                            //size-- 保证下标永远在栈最后一个元素【栈中概念：指针永远指在栈顶元素】
                        flag = true;                                                    //设置标志位为true  表明一直在取（）中的元素
                    }
                    while (size >= 0 && !flag && basic.get(stack.get(size)) >= basic.get(ch)) {    //若取得不是（）内的元素，并且当前栈顶元素的优先级>=对比元素 那就出栈插入队列
                        queue.add(String.valueOf(stack.remove(size)));                    //同样  此处也是remove()方法，既能得到要获取的元素，也能将栈中元素移除掉
                        size--;
                    }
                }
                if(ch != ')') {                                                            //若当前元素不是右括号
                    stack.add(ch);                                                        //就要保证这个符号 入栈
                } else {                                                                //否则就要出栈 栈内符号
                    stack.remove(stack.size() - 1);
                }
            }
            if(i == charArr.length - 1) {                                                //如果已经走到了  中缀表达式的最后一位
                if(len > 0) {                                                            //如果len>0  就截取数字
                    queue.add(String.valueOf(Arrays.copyOfRange(charArr, i - len+1, i+1)));
                }
                int size = stack.size() - 1;                                            //size表示栈内最后一个元素下标
                while (size >= 0) {                                                        //一直将栈内  符号全部出栈 并且加入队列中  【最终的后缀表达式是存放在队列中的，而栈内最后会被弹空】
                    queue.add(String.valueOf(stack.remove(size)));
                    size--;
                }
            }

        }
        return queue.stream().collect(Collectors.joining(","));                            //将队列中元素以,分割 返回字符串
    }

    /**
     * 将 后缀表达式 进行  运算 计算出结果
     * @param equation
     *  后缀表达式
     * @param mapping
     *  表达式与值得映射关系（AB*） {"A":10,"B":20}
     * @return
     */
    public final static String dealEquation(String equation, Map<String,Object> mapping){
        String [] arr = equation.split(",");//根据, 拆分字符串
        List<String> list = new ArrayList<String>();
        for (int i = 0;i < arr.length;i ++) {
            Double newResult = null;
            int size = list.size();
            switch (arr[i]) {
                case "+":
                    newResult = getMappingValue(mapping,list.remove(size - 2)) + getMappingValue(mapping,list.remove(size - 2));list.add(String.valueOf(newResult));
                    break;
                case "-":
                    newResult = getMappingValue(mapping,list.remove(size - 2)) - getMappingValue(mapping,list.remove(size - 2));list.add(String.valueOf(newResult));
                    break;
                case "*":
                    newResult = getMappingValue(mapping,list.remove(size - 2)) * getMappingValue(mapping,list.remove(size - 2));list.add(String.valueOf(newResult));
                    break;
                case "/":
                    newResult = getMappingValue(mapping,list.remove(size - 2)) / getMappingValue(mapping,list.remove(size - 2));list.add(String.valueOf(newResult));
                    break;
                default:list.add(arr[i]);
            }
        }
        return list.size() == 1 ? list.get(0) : null ;
    }
    private final static Double getMappingValue(Map<String,Object> mapping,String key){
        if (mapping != null && mapping.containsKey(key))
            return Double.parseDouble(mapping.get(key).toString());
        return Double.valueOf(key);
    }

    /**
     * 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址;
     *
     * @param request
     * @return
     * @throws IOException
     */
    public final static String getIpAddress(HttpServletRequest request){
        // 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } else if (ip.length() > 15) {
            String[] ips = ip.split(",");
            for (int index = 0; index < ips.length; index++) {
                String strIp = (String) ips[index];
                if (!("unknown".equalsIgnoreCase(strIp))) {
                    ip = strIp;
                    break;
                }
            }
        }
//        LogKit.info(Util.class,"ip:" + ip);
        return ip;
    }

    /**
     * 校验ip地址是否合法
     * @param ip
     *  校验的额IP地址 需要时192.168.0.100样例或者网段
     * */
    public static boolean isLegitimacy(String ip){
        String[] ips = ip.split("[.]{1}");
        if (ips.length != 4) return false;
        for (String str:ips){
            try {
                if (Integer.valueOf(str) > 255 || Integer.valueOf(str) < 0)
                    return false;
            }catch (Exception e){
                return false;
            }
        }
        return true;
    }


//    public static char[] ipConversionsBx(String ip){
//        if (!Util.isLegitimacy(ip)) return null;
//        String[] ips = ip.split("[.]{1}");
//        StringBuffer chars = new StringBuffer();
//        for (int i = 0;i < 4;i ++) {
//            char[] binary = Integer.toBinaryString(Short.valueOf(ips[i])).toCharArray();
//            if (binary.length < 8){
//                binary = Integer.toBinaryString(Short.valueOf(ips[i])  + 128).toCharArray();
//                binary[0] = '0';
//            }
//            chars.append(binary);
//        }
//        return chars.toString().toCharArray();
//    }

    public static void main(String[] args) {
        String ip = "192.168.0.100";
//        System.out.println(String.valueOf(ipConversionsBx(ip)).replaceAll("[0]+",""));
    }
}
