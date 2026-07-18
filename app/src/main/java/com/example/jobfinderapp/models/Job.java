package com.example.jobfinderapp.models;

public class Job {
    private String id; // Thêm id để sửa lỗi cho ManageJobsActivity
    private String title;
    private String company;
    private String salary;
    private String location;
    private String imageUrl; // Thuộc tính mới để lưu link ảnh động
    private String deadline;    // Thêm để sửa lỗi cho JobDetailActivity
    private String description; // Thêm để sửa lỗi cho JobDetailActivity
    private String requirement; // Thêm để sửa lỗi cho JobDetailActivity

    // Constructor đã được cập nhật thêm tham số imageUrl (Giữ nguyên của bạn)
    public Job(String title, String company, String salary, String location, String imageUrl) {
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    // --- CÁC HÀM GETTER ĐỂ ADAPTER VÀ ACTIVITY LẤY DỮ LIỆU ---

    // Hàm này giúp ManageJobsActivity hết lỗi đỏ
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public String getCompany() { return company; }

    // Hàm này giúp JobDetailActivity hết lỗi ở dòng job.getCompanyName()
    public String getCompanyName() { return company; }

    public String getSalary() { return salary; }
    public String getLocation() { return location; }
    public String getImageUrl() { return imageUrl; }

    // Hàm này giúp JobDetailActivity hết lỗi ở dòng job.getCompanyLogo()
    public String getCompanyLogo() { return imageUrl; }

    // Các hàm dưới đây giúp JobDetailActivity hết lỗi phần thông tin chi tiết
    public String getDeadline() { return deadline != null ? deadline : "Chưa cập nhật"; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirement() { return requirement != null ? requirement : ""; }
    public void setRequirement(String requirement) { this.requirement = requirement; }
}