package com.iar.myapplication;

public class Plumber {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String pincode;
    private String serviceArea;
    private boolean available;
    private String hourlyRate;
    private double rating;
    private int numRatings;

    public Plumber() { }

    public Plumber(String uid, String name, String email, String phone, String address, String pincode, String serviceArea, boolean available, String hourlyRate, double rating, int numRatings) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.pincode = pincode;
        this.serviceArea = serviceArea;
        this.available = available;
        this.hourlyRate = hourlyRate;
        this.rating = rating;
        this.numRatings = numRatings;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getServiceArea() { return serviceArea; }
    public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(String hourlyRate) { this.hourlyRate = hourlyRate; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getNumRatings() { return numRatings; }
    public void setNumRatings(int numRatings) { this.numRatings = numRatings; }
}