package org.example.ex02;

import java.util.Optional;

public class Exam02 {
    private int a; // 0

    Exam02() {
        // 코드 작성
        var b = 20; // 타입 추론
//        var c = null; // X
        int c = 30;
//        int d;
        Integer e = new Integer(100);
        Integer f = 20;
        Integer g = null;

        callFunction(a);
        callFunction(b);
        callFunction(c);
//        callFunction(d);
        callFunction(e);
        callFunction(f);
        callFunction(g); //NPE
        callFunction(null);

    }

    public void callFunction(Integer i) {
        var _i = (i == null) ? Integer.valueOf(100) : Integer.valueOf(i);
        var temp = _i;
        System.out.println(temp * 20);
    }
    public static void main(String[] args) {
        new Exam02();
    }
}
