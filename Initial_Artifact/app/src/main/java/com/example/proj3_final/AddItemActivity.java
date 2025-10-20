package com.example.proj3_final;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/*
    Written By: Anthony Schissler
    Date: 6/27/2025

    Class to hold our AddItem Activity. This allows the user to add new items into the database
    with a specified quantity
 */
public class AddItemActivity extends AppCompatActivity {

    String currentUser;
    String phoneNumber;
    TextView initialItemQuantity, itemNameTitle, userAndPermissionText, newItemTitle;

    EditText itemNameText, editItemQuantityNumberSigned;

    com.google.android.material.bottomnavigation.BottomNavigationView NavBar;

    Button addItemButton;

    WarehouseItemsSQLDriver warehouseDB;

    //Build UI
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_activity);

        //Assign variables
        initialItemQuantity = findViewById(R.id.initialItemQuantity);
        itemNameTitle = findViewById(R.id.itemNameTitle);
        userAndPermissionText = findViewById(R.id.userAndPermissionText);
        newItemTitle = findViewById(R.id.addItemTitle);
        itemNameText = findViewById(R.id.itemNameText);
        editItemQuantityNumberSigned = findViewById(R.id.editItemQuantityNumberSigned);
        NavBar = findViewById(R.id.NavBar);
        addItemButton = findViewById(R.id.addItemButton);

        //Open connection to the WarehouseDB
        warehouseDB = new WarehouseItemsSQLDriver(this);

        //Grab user login information
        Bundle getData = getIntent().getExtras();
        assert getData != null;
        currentUser = getData.getString("User_Name");
        phoneNumber = getData.getString("Phone_Number");
        userAndPermissionText.setText("Current User: " + currentUser + " | Phone Number: " + phoneNumber);

        //Set listener to call add Item method
        addItemButton.setOnClickListener(view ->
        {
            addItem();
        });

        //Listener for Navbar
        NavBar.setOnItemSelectedListener(item ->
        {
            //Move to home
            if (item.getItemId() == R.id.home_navigation)
            {
                Bundle dataPass = new Bundle();
                dataPass.putString("User_Name", currentUser);
                dataPass.putString("Phone_Number", phoneNumber);

                Intent startHomeActivity  = new Intent(AddItemActivity.this, HomeActivity.class);
                startHomeActivity.putExtras(dataPass);
                startActivity(startHomeActivity);

                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                return true;
            }
            //Do nothing
            else if (item.getItemId() == R.id.add_item_navigation)
            {
                return true;
            }
            return true;
        });
    }

    //Method to add an item to the database
    protected boolean addItem()
    {
        //Check if empty/valid first
        if(textNotEmpty(itemNameText.getText().toString()) && textNotEmpty(editItemQuantityNumberSigned.getText().toString()))
        {

            //Build a new warehouse item with no alert set
            WarehouseItem item = new WarehouseItem(itemNameText.getText().toString(), Integer.parseInt(editItemQuantityNumberSigned.getText().toString()), 0, -1);
            boolean check = warehouseDB.createWarehouseItem(item);

            //Check if the item was created
            if(check)
            {
                //Notify user of successful item creation
                Toast.makeText(this, "Added " + itemNameText.getText().toString() +
                        " with a quantity of " + editItemQuantityNumberSigned.getText().toString(), Toast.LENGTH_LONG).show();
                return true;
            }
            else
            {
                //Notify user the item was not able to be created due to it already existing
                Toast.makeText(this, "Failed to add item - already exists in the database", Toast.LENGTH_LONG).show();
                return false;
            }

        }
        else
        {
            //Notify user of failed item addition due to missing information
            Toast.makeText(this, "Failed to add item - please fill both name and quantity out on the form.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    //check if a String is null or void of characters
    protected boolean textNotEmpty(String stringIn)
    {
        if(stringIn == null || stringIn.equals(""))
        {
            return false;
        }
        return true;
    }
}
