package com.example.jobfinderapp.models;

public class Job {
    private int id;
    private String title;
    private int companyId;
    private int categoryId;
    private String salary;
    private String location;
    private String description;
    private String requirement;
    private String deadline;
    
    // Extra fields for display
    private String companyName;
    private String companyLogo;
    private String applyDate;
    private String status;

    public Job() {}

    public Job(int id, String title, int companyId, int categoryId, String salary, String location, String description, String requirement, String deadline) {
        this.id = id;
        this.title = title;
        this.companyId = companyId;
        this.categoryId = categoryId;
        this.salary = salary;
        this.location = location;
        this.description = description;
        this.requirement = requirement;
        this.deadline = deadline;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLogo() { return companyLogo; }
    public void setCompanyLogo(String companyLogo) { this.companyLogo = companyLogo; }

    public String getApplyDate() { return applyDate; }
    public void setApplyDate(String applyDate) { this.applyDate = applyDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
