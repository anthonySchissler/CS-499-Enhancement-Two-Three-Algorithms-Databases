package com.example.proj3_final;

/*
    Written By: Anthony Schissler
    Date: 6/27/2025

    Class to hold a user for our application. Stores
    a username, a password, and a phone number
 */
public class WarehouseUser {

    private String userName;

    private int permission;

    public static final int ADMINISTRATOR = 0;
    public static final int MANAGER = 1;
    public static final int EMPLOYEE = 2;

    protected void setPermission(int permission) {
        this.permission = permission;
    }

    protected static String getPermission(int permission)
    {
        if(permission == 0)
        {
            return "Administrator";
        }
        else if(permission == 1)
        {
            return "Manager";
        }
        else
        {
            return "Employee";
        }
    }

    public int getPermissionInt()
    {
        return permission;
    }

    protected String getPermission()
    {
        if(permission == 0)
        {
            return "Administrator";
        }
        else if(permission == 1)
        {
            return "Manager";
        }
        else
        {
            return "Employee";
        }
    }

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

    public WarehouseUser(String userName, String password, String phoneNumber, int permission)
    {
        this.userName = userName;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.permission = permission;
    }
}
