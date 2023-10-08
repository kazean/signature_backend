package org.example.ex10;

import java.util.Optional;

public class Exam10 {
    public Exam10(ExamUser examUser) {
        Optional<ExamUser> optionalUser = Optional.ofNullable(examUser);
        optionalUser.ifPresent(it -> {
            Optional.ofNullable(examUser.getName()).ifPresent(name -> {
                if (!name.isBlank()) {
                    System.out.println(name);
                }
            });
        });

        // 더 직관적
        if (examUser != null && examUser.getName() != null) {
            if (StringUtils.notBlank(examUser.getName())) {
                System.out.println(examUser.getName());
            }
        }
    }

    public static void main(String[] args) {
        ExamUser user = new ExamUser();
        user.setName("abcd");
        new Exam10(user);
    }
}

class StringUtils {
    public static boolean notBlank(String value) {
        return !value.isBlank();
    }
}

class ExamUser {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}