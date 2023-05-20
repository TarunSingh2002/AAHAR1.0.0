package com.example.aahar100;
public class ReadWriteUserHistory {
    public String food , description;

    public ReadWriteUserHistory(String food, String description) {
        this.food = food;
        this.description = description;
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public ReadWriteUserHistory (){};
}
