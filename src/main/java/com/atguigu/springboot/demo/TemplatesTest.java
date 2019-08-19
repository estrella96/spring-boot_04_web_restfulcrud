package com.atguigu.springboot.demo;

import java.util.ArrayList;

/**
 * @auth xiaobai
 * @date 2018/11/25 - 18:44
 *
 * IDEA中代码模块所处的位置:settings - Editor -Live Templates /Postfix Completion
 * 2.常用的模板
 */
public class TemplatesTest {

    //模板六: prsf : 可生成 private static final
    private static final String AAA = "aaaa";

    //变形:psf
    public static final int NUM = 1;
    //变形:psfi
    public static final int NUM1 = 2;
    //变形:psfs
    public static final String  BBB = "bbbb";


    //模板一:psvm
    public static void main(String[] args) {

        //模板二:sout
        System.out.println("hello");
        //变形:soutp / soutm /soutv / xxx.sout
        System.out.println("args = [" + args + "]");
        System.out.println("TemplatesTest.main");

        //模板三:fori
        String[] arr = new String[]{"张三","李四","网路"};

        for (int i = 0; i <arr.length ; i++) {
            System.out.println(arr[i]);
        }

        //变形: iter
        for (String s : arr) {
            System.out.println(s);
        }
        //变形:itar
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            System.out.println(s);
        }

        //模板四:list.for
        ArrayList list = new ArrayList();
        list.add(123);
        list.add(344);
        list.add(314);

        for (Object o : list) {

        }
        //变形:list.fori
        for (int i = 0; i < list.size(); i++) {

        }
        //变形:list.forr  倒序遍历
        for (int i = list.size() - 1; i >= 0; i--) {

        }

        //模板5: ifn
        if (list == null) {

        }
        //变形: inn
        if (list != null) {

        }

        //变形:xxx.nn /xxx.null
        if (list != null) {

        }
        if (list == null) {

        }





    }

    public void method(){
        System.out.println("TemplatesTest.method");



    }



}
