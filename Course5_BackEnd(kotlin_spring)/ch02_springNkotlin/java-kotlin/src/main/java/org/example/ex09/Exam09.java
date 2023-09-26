package org.example.ex09;

public class Exam09 {
    public Exam09(StoreUser storeUser) {
        // service logic
        if (("MASTER").equals(storeUser.getRole())) {
            //master
        } else if (("ADMIN").equals(storeUser.getRole())) {
            //admin
        } else if (("USER").equals(storeUser.getRole())) {
            // user
        } else {
            // default
        }

        var role = "";
        switch (storeUser.getRole()) {
            case "MASTER":
            case "ADMIN":
                role = "MASTER";
                break;
            case "USER":
                break;
            default:
                // default
        }

        try {

        } catch (Exception e) {
            var result = "";
            if (e instanceof NullPointerException) {

            } else if (e instanceof NumberFormatException) {
            }
        }
    }

    public static void main(String[] args) {
        var storeUser = new StoreUser();
        new Exam09(storeUser);
    }
}

class StoreUser {
    private String name;
    private String role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}