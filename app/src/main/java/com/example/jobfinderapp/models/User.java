package com.example.jobfinderapp.models;

public class User {

    private int id;
    private String fullname;
    private String email;
    private String password;
    private String phone;
    private String avatar;

    public User() {
    }

    public User(int id, String fullname, String email,
                String password, String phone, String avatar) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.avatar = avatar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}