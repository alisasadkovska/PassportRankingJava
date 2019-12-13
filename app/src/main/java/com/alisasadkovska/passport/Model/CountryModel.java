package com.alisasadkovska.passport.Model;

public class CountryModel {
    private String Name, Image, Cover;
    private Double Latitude, Longitude;


    public CountryModel() {
    }

    public CountryModel(String name, String image, String cover, Double latitude, Double longitude) {
        Name = name;
        Image = image;
        Cover = cover;
        Latitude = latitude;
        Longitude = longitude;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getCover() {
        return Cover;
    }

    public void setCover(String cover) {
        Cover = cover;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }
}
