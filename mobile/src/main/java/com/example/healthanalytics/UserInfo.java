package com.example.healthanalytics;

import java.io.Serializable;

public class UserInfo implements Serializable {

    String  Name;
    String Height;
    String Weight;
    String Gender;
    String DOB;
    String Email;

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }



    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getHeight() {
        return Height;
    }

    public void setHeight(String height) {
        Height = height;
    }

    public String getWeight() {
        return Weight;
    }

    public void setWeight(String weight) {
        Weight = weight;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }


    public UserInfo (String name, String email, String height, String weight, String gender, String DOB)
    {
        Name = name;
        Email = email;
        Height = height;
        Weight = weight;
        Gender = gender;
        this.DOB = DOB;

    }
    public UserInfo()
    {

    }
}
