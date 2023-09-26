package org.example.ex01;

public class Exam01 {

    Exam01() {
        // 코드 작성
        String name = "홍길동";
        String format = "사용자 이름은 : %s";
        String result = String.format(format, name);
        System.out.println(result);

        int age = 10;
        Integer _age = 10;

        double d = 10d;
        Double _d = 20.0;

        float f = 20f;
        Float _f = 20.0f;

        long l = 20L;
        Long _l = 20L;

        boolean bool = false;
        Boolean _bool = false;
    }
    public static void main(String[] args) {
        new Exam01();
    }
}
