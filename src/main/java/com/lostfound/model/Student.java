package com.lostfound.model;

public class Student extends User {
    private static final long serialVersionUID = 1L;

    private String regNo;

    public Student() {
        super();
    }

    public Student(int id, String name, String email, String regNo) {
        super(id, name, email);
        this.regNo = regNo;
    }

    @Override
    public String getRole() {
        return "STUDENT";
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }
}
