package com.example.bussinessdirectory;

public class Company {
    String name;
    String address;
    String phone;
    String website;
    double latitude;
    double longitude;
    String category;

    // Konstruktor bez lokacija i kategorija
    public Company(String name, String address, String phone, String website) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.latitude = 0;
        this.longitude = 0;
        this.category = "";
    }

    // Konstruktor so lokacija, bez kategorija
    public Company(String name, String address, String phone, String website, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = "";
    }

    // konstruktor so se
    public Company(String name, String address, String phone, String website,
                   double latitude, double longitude, String category) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getWebsite() { return website; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getCategory() { return category; }
}