package com.example.jobfinderapp.models;

public class Experience {
    private final int id; private final String title, company, location, startDate, endDate, description, achievements;
    public Experience(int id, String title, String company, String startDate, String endDate) { this(id,title,company,null,startDate,endDate,null,null); }
    public Experience(int id,String title,String company,String location,String startDate,String endDate,String description,String achievements){this.id=id;this.title=title;this.company=company;this.location=location;this.startDate=startDate;this.endDate=endDate;this.description=description;this.achievements=achievements;}
    public int getId(){return id;} public String getTitle(){return title;} public String getCompany(){return company;} public String getLocation(){return location;} public String getStartDate(){return startDate;} public String getEndDate(){return endDate;} public String getDescription(){return description;} public String getAchievements(){return achievements;}
}
