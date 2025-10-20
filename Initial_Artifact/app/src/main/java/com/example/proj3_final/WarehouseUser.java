package com.example.proj3_final;

/*
    Written By: Anthony Schissler
    Date: 6/27/2025

    Class to hold a user for our application. Stores
    a username, a password, and a phone number
 */
public class WarehouseUser {

    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String password;

    protected String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public WarehouseUser(String userName, String password, String phoneNumber)
    {
        this.userName = userName;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }
}
