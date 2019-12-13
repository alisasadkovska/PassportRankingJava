package com.alisasadkovska.passport.Model;

public class SpinnerModel {

    private boolean isHeader;
    private int drawable;
    private String name;

    public SpinnerModel(boolean isHeader, int drawable, String name) {
        this.isHeader = isHeader;
        this.drawable = drawable;
        this.name = name;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
