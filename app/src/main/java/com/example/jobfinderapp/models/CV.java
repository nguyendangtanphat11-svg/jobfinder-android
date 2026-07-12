package com.example.jobfinderapp.models;

public class CV {
    private int id;
    private int userId;
    private String cvName;
    private String education;
    private String skills;
    private String experience;
    private String objective;

    public CV() {}

    public CV(int id, int userId, String cvName, String education, String skills, String experience, String objective) {
        this.id = id;
        this.userId = userId;
        this.cvName = cvName;
        this.education = education;
        this.skills = skills;
        this.experience = experience;
        this.objective = objective;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCvName() { return cvName; }
    public void setCvName(String cvName) { this.cvName = cvName; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
}
