package com.example.proj3_final;

import android.content.Context;
import android.telephony.SmsManager;

/*
    Written By: Anthony Schissler
    Date: 6/27/2025

    Class to hold a warehouse item. An item has a name, a quantity, an alert status,
    and an alert threshold.
 */
public class WarehouseItem {

    private String itemName;
    private int quantity;

    private int alertOn;

    private int alertThreshold;

    private boolean alertReached;

    public WarehouseItem(String itemName, int quantity, int alertOn, int alertThreshold)
    {
        this.itemName = itemName;
        this.quantity = quantity;
        this.alertOn = alertOn;
        this.alertThreshold = alertThreshold;
    }

    public void setAlert(int alertThreshold)
    {
        this.alertThreshold = alertThreshold;
        if(alertOn == 0)
            alertOn = 1;

    }

    public int getAlertThreshold()
    {
        return alertThreshold;
    }


    public int getAlertOn()
    {
        return alertOn;
    }

    public int increaseCount(int increaseNum)
    {
        if(increaseNum < (Integer.MAX_VALUE - quantity))
        {
            return quantity += increaseNum;

        }
        return -1;
    }

    public int decreaseCount(int decreaseNum)
    {
        int check = quantity - decreaseNum;
        if(check > -1)
        {
            return quantity = check;
        }
        return -1;
    }

    public String getItemName(){return itemName;}
    public int getQuantity(){return quantity;}

    public void setQuantity(int quantity){this.quantity = quantity;}



}
