package com.lostfound.model;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private String staffId;

    public Admin() {
        super();
    }

    public Admin(int id, String name, String email, String staffId) {
        super(id, name, email);
        this.staffId = staffId;
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
}
