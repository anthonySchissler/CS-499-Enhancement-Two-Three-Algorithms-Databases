package com.example.proj3_final;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.pdf.models.ListItem;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;
import java.util.ArrayList;

/*
    Written By: Anthony Schissler
    Date: 6/27/2025

    Class to hold our Home Activity. From this activity, users can view and manipulate all objects in the
    Warehouse Item database, as well as set alerts for those objects. Users can navigate to the
    Add Item Activity from here as well through the bottom navigation menu
 */
public class HomeActivity extends AppCompatActivity {
    String currentUser;

    TextView userAndPermissionText;

    ListView itemList;

    public ListItemAdapter itemListAdapter;

    ArrayList<WarehouseItem> warehouseItems;

    com.google.android.material.bottomnavigation.BottomNavigationView NavBar;

    WarehouseItemsSQLDriver warehouseItemsDB;

    private static boolean smsPermission;

    private static String phoneNumber;
    AlertDialog dialog;

    private static boolean visitedPage = false;


    //Build Activity elements
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        //Assign elements to variables
        userAndPermissionText = findViewById(R.id.userAndPermissionText);
        itemList = findViewById(R.id.itemList);
        NavBar = findViewById(R.id.NavBar);
        warehouseItemsDB = new WarehouseItemsSQLDriver(this);

        //Use for removal of Database
        //this.deleteDatabase("WarehouseItems.DB");

        //Grab passed information from Login Activity to display current user information
        Bundle getData = getIntent().getExtras();
        assert getData != null;
        currentUser = getData.getString("User_Name");
        phoneNumber = getData.getString("Phone_Number");
        userAndPermissionText.setText("Current User: " + currentUser + " | Phone Number: " + phoneNumber);

        //If our Warehouse Items database has more than one object
        if(warehouseItemsDB.getCount() > 0)
        {
            //Update UI with those items
            warehouseItems = warehouseItemsDB.returnAllItems();

            //Call our custom adapter to display the rows
            ListItemAdapter customRows = new ListItemAdapter(this, R.layout.list_item, warehouseItems);
            itemList.setAdapter(customRows);
        }

        //Set listener for navigation bar
        NavBar.setOnItemSelectedListener(item ->
        {
            //If Home selected on Home, do nothing
            if (item.getItemId() == R.id.home_navigation)
            {
                return true;
            }

            //If Add Item selected on Home, move to Add Item
            else if (item.getItemId() == R.id.add_item_navigation)
            {
                //Pass user login info
                Bundle dataPass = new Bundle();
                dataPass.putString("User_Name", currentUser);
                dataPass.putString("Phone_Number", phoneNumber);

                //Move context to Add Item Activity
                Intent startAddItem  = new Intent(HomeActivity.this, AddItemActivity.class);
                startAddItem.putExtras(dataPass);
                startActivity(startAddItem);
                overridePendingTransition( R.anim.slide_in_other, R.anim.slide_out);
                return true;
            }
            return true;
        });

    }

    public static String getPhoneNumber()
    {
        return phoneNumber;
    }

    public static void setSMSPermission(boolean permission)
    {
        smsPermission = permission;
    }
    public static boolean getSMSPermission()
    {
        return smsPermission;
    }


}
