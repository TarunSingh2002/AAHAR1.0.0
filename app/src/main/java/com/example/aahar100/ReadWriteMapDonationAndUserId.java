package com.example.aahar100;

public class ReadWriteMapDonationAndUserId {
    public String donationId;

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public ReadWriteMapDonationAndUserId(){};
    public ReadWriteMapDonationAndUserId( String textDonationId){
        this.donationId=textDonationId;
    }
}
