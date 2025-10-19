package com.example.proj3_final;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/*
    Written By: Anthony Schissler
    Date: 6/27/2025

    Class used as a custom implementation of the ArrayAdapter class.
    This is used to display our database items in a table on the homepage,
    and allow the user to manipulate those items using custom rows and buttons
 */
public class ListItemAdapter extends ArrayAdapter<WarehouseItem>
{
    ListItemAdapter adapter;
    private int resourceLayout;
    final Activity mContext;

    ArrayList<WarehouseItem> warehouseItems;

    WarehouseItemsSQLDriver warehouseItemsDB;

    public String itemListType;

    //Constructor calling the super (ArrayAdapter)
    public ListItemAdapter(Activity context, int resource, ArrayList<WarehouseItem> warehouseItems) {
        super(context, resource, warehouseItems);
        this.resourceLayout = resource;
        this.mContext = context;
        this.warehouseItems = warehouseItems;
        this.adapter = this;
        warehouseItemsDB = new WarehouseItemsSQLDriver(this.getContext());
    }

    //Generates the view for this list item
    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        //Generate the view
        View thisView = convertView;
        if (thisView == null) {
            LayoutInflater layoutInflater;
            layoutInflater = LayoutInflater.from(mContext);
            thisView = layoutInflater.inflate(resourceLayout, null);
        }

        //Grab the item at the current index of the iterator
        WarehouseItem warehouseItem = getItem(pos);

        //If it's not null
        if (warehouseItem != null) {

            //Assign row elements to variables
            TextView itemNameField = (TextView) thisView.findViewById(R.id.itemName);
            TextView itemQuantityField = (TextView) thisView.findViewById(R.id.itemQuantity);
            ImageButton removeQuantity = (ImageButton) thisView.findViewById(R.id.removeItem);
            ImageButton addQuantity = (ImageButton) thisView.findViewById(R.id.addItem);
            ImageButton manageAlert = (ImageButton) thisView.findViewById(R.id.manageAlert);

            //Set the item name
            if (itemNameField != null) {
                itemNameField.setText("Item Name: " + warehouseItem.getItemName());
            }

            //Set the item quantity
            if (itemQuantityField != null) {
                itemQuantityField.setText("Quantity: " + String.valueOf(warehouseItem.getQuantity()));
            }

            //Check if the Remove Quantity button was created
            if (removeQuantity != null)
            {
                //Set a listener on click of the button
                removeQuantity.setOnClickListener(item ->
                {
                    //Subtract a singular quantity from the item
                    warehouseItemsDB.subtractItemQuantity(warehouseItem);
                    sendSMS(getContext(), warehouseItemsDB.readWarehouseItem(warehouseItem.getItemName()));

                    //Refresh the list to update UI
                    ArrayList<WarehouseItem> newList = warehouseItemsDB.returnAllItems(HomeActivity.sortType);
                    warehouseItems.clear();
                    warehouseItems.addAll(newList);
                    adapter.notifyDataSetChanged();

                });

                //Set a listener on a long click of the button
                removeQuantity.setOnLongClickListener(item ->
                {
                    //Start method to handle quantity decrease of more than one
                    onDecreaseButtonHoldWindow(item, warehouseItem);

                    //Update UI
                    ArrayList<WarehouseItem> newList = warehouseItemsDB.returnAllItems(HomeActivity.sortType);
                    warehouseItems.clear();
                    warehouseItems.addAll(newList);
                    return true;
                });
            }

            //Check if Add Quantity button has been created
            if (addQuantity != null)
            {
                //Set listener on click
                addQuantity.setOnClickListener(item ->
                {
                    //Increase the quantity of this item by one
                    warehouseItemsDB.addItemQuantity(warehouseItem);

                    //Update UI
                    ArrayList<WarehouseItem> newList = warehouseItemsDB.returnAllItems(HomeActivity.sortType);
                    warehouseItems.clear();
                    warehouseItems.addAll(newList);
                    adapter.notifyDataSetChanged();
                });

                //Set listener on long click
                addQuantity.setOnLongClickListener(item ->
                {
                    //Start method to handle quantity increase greater than one
                    onIncreaseButtonHoldWindow(item, warehouseItem);
                    return true;
                });
            }

            //Check if Manage Alert button has been created
            if(manageAlert != null)
            {
                //Set on click listener
                manageAlert.setOnClickListener(item ->
                {
                    //Start method to handle modifying alerts on the object
                    onAlertButtonSelection(item, warehouseItem);
                });

                //If the object currently has the alert flag set
                if(warehouseItem.getAlertOn() == 1)
                {
                    //Change the alert icon to a filled, red icon
                    manageAlert.setImageResource(R.drawable.baseline_add_alert_24);
                    manageAlert.setColorFilter(mContext.getColor(R.color.red));
                }
                else
                {
                    //Otherwise change to a hollow, black icon
                    manageAlert.setImageResource(R.drawable.outline_add_alert_24);
                    manageAlert.setColorFilter(mContext.getColor(R.color.black));
                }
            }

        }

        //Return the current view for this row
        return thisView;
    }

    //Method to decrease the quantity of an item by more than one via a popup window
    public void onDecreaseButtonHoldWindow(View view, WarehouseItem item)
    {
        //Inflate popup window with the decrease quantity popup
        View popup = LayoutInflater.from(mContext).inflate(R.layout.popup_decrease_quantity, null);
        final PopupWindow popupWindow = new PopupWindow(popup, 800, 800, true);

        //Assign elements to variables
        Button confirmButton = (Button)popup.findViewById(R.id.confirmButton);
        Button cancelButton = (Button)popup.findViewById(R.id.cancelButton);
        EditText decreaseAmountField = (EditText)popup.findViewById(R.id.decreaseAmountField);

        //Set listener on click for confirmation of decrease amount
        confirmButton.setOnClickListener(viewPopup ->
        {
            //Submit subtraction to the warehouse database driver
            warehouseItemsDB.subtractItemQuantity(Integer.parseInt(decreaseAmountField.getText().toString()), item);
            sendSMS(view.getContext(), warehouseItemsDB.readWarehouseItem(item.getItemName()));

            //Notify user upon decrease amount
            Toast.makeText(mContext, "Quantity decreased by " + decreaseAmountField.getText().toString(), Toast.LENGTH_LONG).show();

            //Update UI
            ArrayList<WarehouseItem> newList = warehouseItemsDB.returnAllItems(HomeActivity.sortType);
            warehouseItems.clear();
            warehouseItems.addAll(newList);
            adapter.notifyDataSetChanged();

            //Dismiss popup
            popupWindow.dismiss();
        });

        //On cancel, dismiss popup
        cancelButton.setOnClickListener(viewPopup ->
        {
            popupWindow.dismiss();
        });

        //Display inflated popup at specified location
        popupWindow.showAtLocation(popup.getRootView(), Gravity.CENTER, 0 ,0);
    }

    //Method to increase the quantity of an item by more than one via a popup window
    public void onIncreaseButtonHoldWindow(View view, WarehouseItem item)
    {
        //Inflate popup window with the increase quantity popup
        View popup = LayoutInflater.from(mContext).inflate(R.layout.popup_increase_quantity, null);
        final PopupWindow popupWindow = new PopupWindow(popup, 800, 800, true);

        //Assign elements to variables
        Button confirmButton = (Button)popup.findViewById(R.id.confirmButton);
        Button cancelButton = (Button)popup.findViewById(R.id.cancelButton);
        EditText increaseAmountField = (EditText)popup.findViewById(R.id.increaseAmountField);

        //Set on click listener for confirmation of increase amount
        confirmButton.setOnClickListener(viewPopup ->
        {
            //Submit addition to the warehouse database driver
            warehouseItemsDB.addItemQuantity(Integer.parseInt(increaseAmountField.getText().toString()), item);

            //Notify user of quantity change
            Toast.makeText(mContext, "Quantity increase by " + increaseAmountField.getText().toString(), Toast.LENGTH_LONG).show();

            //Update UI
            ArrayList<WarehouseItem> newList = warehouseItemsDB.returnAllItems(HomeActivity.sortType);
            warehouseItems.clear();
            warehouseItems.addAll(newList);
            adapter.notifyDataSetChanged();

            //Close popup window
            popupWindow.dismiss();
        });

        //Dismiss popup
        cancelButton.setOnClickListener(viewPopup ->
        {
            popupWindow.dismiss();
        });


        //Display popup at specified location
        popupWindow.showAtLocation(popup.getRootView(), Gravity.CENTER, 0 ,0);
    }

    //Method for manipulating alerts on a Warehouse Item
    public void onAlertButtonSelection(View view, WarehouseItem item)
    {
        //Inflate popup with SMS alert configuration
        View popup = LayoutInflater.from(mContext).inflate(R.layout.popup_sms_alert, null);
        final PopupWindow popupWindow = new PopupWindow(popup, 850, 850, true);

        //Assign elements to variables
        Button confirmButton = (Button)popup.findViewById(R.id.confirmButton);
        Button cancelButton = (Button)popup.findViewById(R.id.cancelButton);
        Button removeAlert = (Button)popup.findViewById(R.id.removeAlerts);
        EditText alertThreshold = (EditText)popup.findViewById(R.id.alertThresholdField);
        TextView alertStatus = (TextView)popup.findViewById(R.id.alertStatus);

        //Set click listener to confirmation button
        confirmButton.setOnClickListener(viewPopup ->
        {
            //Grab alert threshold and send to database driver for this Warehouse Item
            boolean checkAlert = warehouseItemsDB.setAlertThreshold(Integer.parseInt(alertThreshold.getText().toString()), item);
            if(checkAlert)
            {
                //Notify user of the alert added
                Toast.makeText(mContext, "Alert added to " + item.getItemName(), Toast.LENGTH_LONG).show();

                //Update UI
                ArrayList<WarehouseItem> newList = warehouseItemsDB.returnAllItems(HomeActivity.sortType);
                warehouseItems.clear();
                warehouseItems.addAll(newList);
                adapter.notifyDataSetChanged();

                //Dismiss popup
                popupWindow.dismiss();
            }
            else
            {
                Toast.makeText(mContext, "Please enter a threshold lower than the current quantity", Toast.LENGTH_LONG).show();
            }

        });

        //Cancel Alert popup
        cancelButton.setOnClickListener(viewPopup ->
        {
            popupWindow.dismiss();
        });

        //Set on click listener to Remove Alert button
        removeAlert.setOnClickListener(viewPopup ->
        {
            //Call method to remove alert on the object
            onRemoveAlertButtonSelection(viewPopup, item);

            //Inform user of the alert removal
            Toast.makeText(mContext, "Alert removed from " + item.getItemName(), Toast.LENGTH_LONG).show();

            //Update UI
            ArrayList<WarehouseItem> newList = warehouseItemsDB.returnAllItems(HomeActivity.sortType);
            warehouseItems.clear();
            warehouseItems.addAll(newList);
            adapter.notifyDataSetChanged();

            //Dismiss popup
            popupWindow.dismiss();
        });

        //If an alert is currently set
        if(item.getAlertOn() == 1)
        {
            //Show the Alert Removal button
            removeAlert.setVisibility(View.VISIBLE);

            //Show the alert threshold
            alertStatus.setText("Alert set on: " + item.getAlertThreshold() + " items");
        }

        //otherwise
        else
        {
            //Hide the alert removal button
            removeAlert.setVisibility(View.INVISIBLE);

            //Show the alert status
            alertStatus.setText("Alert Status: OFF");
        }

        //Display popup at specified location
        popupWindow.showAtLocation(popup, Gravity.CENTER, 0 ,0);
    }

    //Method to handle removal of alerts
    public void onRemoveAlertButtonSelection(View view, WarehouseItem item)
    {
        //Send alert removal to Warehouse DB driver
        warehouseItemsDB.clearAlerts(item);

        //Update UI
        ArrayList<WarehouseItem> newList = warehouseItemsDB.returnAllItems(HomeActivity.sortType);
        warehouseItems.clear();
        warehouseItems.addAll(newList);
        adapter.notifyDataSetChanged();
    }

    //Method for handling SMS Messaging when items are decreased
    public void sendSMS(Context view, WarehouseItem item)
    {

        // check permission and request if necessary
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(mContext,
                    Manifest.permission.SEND_SMS)) {
                //Toast.makeText(mContext,"SMS Permission are required for alert display", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(mContext,
                        new String[] {Manifest.permission.SEND_SMS},
                        0);
            }
        }

        //If we have SMS Permissions
        else
        {
            //If this item falls at or below the threshold
            if(item.getQuantity() <= item.getAlertThreshold())
            {
                String message = item.getItemName() + " has fallen below the alert threshold of " + item.getAlertThreshold() + ". Current " +
                    " value: " + item.getQuantity();

                try
                {
                    //Send an sms alert to the user's phone number
                    SmsManager alertManager = SmsManager.getDefault();
                    alertManager.sendTextMessage(HomeActivity.getPhoneNumber(), null, message, null, null);
                    Toast.makeText(view, "SMS message has been sent to " + HomeActivity.getPhoneNumber(), Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    Toast.makeText(view, "Unable to send SMS", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }

            }
        }

    }


}
