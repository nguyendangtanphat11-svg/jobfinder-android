package com.example.jobfinderapp.models;

public class User {
    private int id;
    private String email;
    private String fullname;
    private String password;
    private String role;
    private String status;
    private String phone;
    private String location;
    private String avatar;
    private String bio;
    private String education; // Thêm trường học vấn
    private String dateOfBirth, gender, address, professionalTitle, jobSearchStatus;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getDateOfBirth(){return dateOfBirth;} public void setDateOfBirth(String v){dateOfBirth=v;}
    public String getGender(){return gender;} public void setGender(String v){gender=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;}
    public String getProfessionalTitle(){return professionalTitle;} public void setProfessionalTitle(String v){professionalTitle=v;}
    public String getJobSearchStatus(){return jobSearchStatus;} public void setJobSearchStatus(String v){jobSearchStatus=v;}
}
