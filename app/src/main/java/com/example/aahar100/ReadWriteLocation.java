package com.example.aahar100;

public class ReadWriteLocation {
    public double lat , lng ;
    public ReadWriteLocation(){};

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public ReadWriteLocation(double textLat , double textLng){
        this.lat=textLat;
        this.lng=textLng;
    }
}
