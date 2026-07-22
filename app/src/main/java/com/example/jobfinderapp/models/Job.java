package com.example.jobfinderapp.models;

public class Job {
    private int id;
    private String title;
    private String companyName;
    private String companyLogo;
    private String location;
    private String salary;
    private String jobType;
    private String applyDate;

    // Các trường mở rộng
    private String description;
    private String requirement;
    private String deadline;
    private int categoryId;
    private int companyId;
    private String status;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    // 1. Constructor rỗng
    public Job() {}

    // 2. Constructor nhận 5 tham số String (Khắc phục lỗi tạo object dạng mock data đơn giản)
    public Job(String title, String companyName, String location, String salary, String jobType) {
        this.title = title;
        this.companyName = companyName;
        this.location = location;
        this.salary = salary;
        this.jobType = jobType;
    }

    // 3. Constructor nhận 6 tham số String (Bao gồm Logo/Image)
    public Job(String title, String companyName, String companyLogo, String location, String salary, String jobType) {
        this.title = title;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
        this.location = location;
        this.salary = salary;
        this.jobType = jobType;
    }

    // 4. Constructor đầy đủ các trường cơ bản với ID
    public Job(int id, String title, String companyName, String companyLogo, String location, String salary, String jobType, String applyDate) {
        this.id = id;
        this.title = title;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
        this.location = location;
        this.salary = salary;
        this.jobType = jobType;
        this.applyDate = applyDate;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLogo() { return companyLogo; }
    public void setCompanyLogo(String companyLogo) { this.companyLogo = companyLogo; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getApplyDate() { return applyDate; }
    public void setApplyDate(String applyDate) { this.applyDate = applyDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}