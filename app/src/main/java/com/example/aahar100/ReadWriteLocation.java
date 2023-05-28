package com.example.aahar100;

public class ReadWriteLocation {
    public double lat , lng ;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String name,number;
    public ReadWriteLocation(){};
    public double getLat() {
        return lat;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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
    public ReadWriteLocation(double textLat , double textLng, String textName, String textnumber){
        this.lat=textLat;
        this.lng=textLng;
        this.name=textName;
        this.number=textnumber;
    }
}
