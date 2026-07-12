package com.example.jobfinderapp.models;

public class Company {
    private int id;
    private String name;
    private String address;
    private String website;
    private String logo;
    private String description;

    public Company() {}

    public Company(int id, String name, String address, String website, String logo, String description) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.website = website;
        this.logo = logo;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
