package com.example.jobfinderapp.models;

public class Job {
    private String title;
    private String company;
    private String salary;
    private String location;
    private String imageUrl; // Thuộc tính mới để lưu link ảnh động

    // Constructor đã được cập nhật thêm tham số imageUrl
    public Job(String title, String company, String salary, String location, String imageUrl) {
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    // Các hàm Getter để Adapter lấy dữ liệu
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getSalary() { return salary; }
    public String getLocation() { return location; }
    public String getImageUrl() { return imageUrl; }
}