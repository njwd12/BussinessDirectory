package com.example.bussinessdirectory;

public class Company {
    private int id;
    private String name;
    private String address;
    private String phone;
    private String website;
    private String email;
    private String category;
    private double latitude;
    private double longitude;
    private int distance; // во метри, -1 ако нема податок

    // Конструктори
    public Company() {}

    public Company(String name, String address, String phone, String website, double latitude, double longitude, String category) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.distance = -1;
    }

    // Гетери и сетери
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getDistance() { return distance; }
    public void setDistance(int distance) { this.distance = distance; }
}