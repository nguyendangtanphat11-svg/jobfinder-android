package com.example.jobfinderapp.models;

public class Company {
    private int id;
    private int userId;
    private String name;
    private String address;
    private String website;
    private String logo;
    private String description;
    private String contactEmail, contactPhone, industry, companySize, representativeName;

    public Company() {}

    public Company(int id, int userId, String name, String address, String website, String logo, String description) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.website = website;
        this.logo = logo;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

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
    public String getContactEmail() { return contactEmail; } public void setContactEmail(String v) { contactEmail=v; }
    public String getContactPhone() { return contactPhone; } public void setContactPhone(String v) { contactPhone=v; }
    public String getIndustry() { return industry; } public void setIndustry(String v) { industry=v; }
    public String getCompanySize() { return companySize; } public void setCompanySize(String v) { companySize=v; }
    public String getRepresentativeName() { return representativeName; } public void setRepresentativeName(String v) { representativeName=v; }
}
