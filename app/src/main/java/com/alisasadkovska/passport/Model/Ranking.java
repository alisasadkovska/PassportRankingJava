package com.alisasadkovska.passport.Model;

public class Ranking {
    private String Name, Cover, Color, Continent, Investment, Timestamp;
    private Integer TotalScore, VisaFree, VisaOnArrival, ETa, VisaRequired;
    private Long Progress;

    public Ranking() {
    }

    public Ranking(String name, String cover, String color, String continent, String investment, String timestamp, Integer totalScore, Integer visaFree, Integer visaOnArrival, Integer eTa, Integer visaRequired, Long progress) {
        Name = name;
        Cover = cover;
        Color = color;
        Continent = continent;
        Investment = investment;
        Timestamp = timestamp;
        TotalScore = totalScore;
        VisaFree = visaFree;
        VisaOnArrival = visaOnArrival;
        ETa = eTa;
        VisaRequired = visaRequired;
        Progress = progress;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCover() {
        return Cover;
    }

    public void setCover(String cover) {
        Cover = cover;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
    }

    public String getContinent() {
        return Continent;
    }

    public void setContinent(String continent) {
        Continent = continent;
    }

    public String getInvestment() {
        return Investment;
    }

    public void setInvestment(String investment) {
        Investment = investment;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public Integer getTotalScore() {
        return TotalScore;
    }

    public void setTotalScore(Integer totalScore) {
        TotalScore = totalScore;
    }

    public Integer getVisaFree() {
        return VisaFree;
    }

    public void setVisaFree(Integer visaFree) {
        VisaFree = visaFree;
    }

    public Integer getVisaOnArrival() {
        return VisaOnArrival;
    }

    public void setVisaOnArrival(Integer visaOnArrival) {
        VisaOnArrival = visaOnArrival;
    }

    public Integer getETa() {
        return ETa;
    }

    public void setETa(Integer ETa) {
        this.ETa = ETa;
    }

    public Integer getVisaRequired() {
        return VisaRequired;
    }

    public void setVisaRequired(Integer visaRequired) {
        VisaRequired = visaRequired;
    }

    public Long getProgress() {
        return Progress;
    }

    public void setProgress(Long progress) {
        Progress = progress;
    }
}
