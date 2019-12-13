package com.alisasadkovska.passport.ExcelModel;

public class RowTitle {
   private String countryName, cover;
   private int mobilityScore;

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getMobilityScore() {
        return mobilityScore;
    }

    public void setMobilityScore(int mobilityScore) {
        this.mobilityScore = mobilityScore;
    }
}
