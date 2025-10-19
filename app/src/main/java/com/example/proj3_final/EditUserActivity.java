package com.example.proj3_final;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/*
    Written By: Anthony Schissler
    Date: 10/5/2025

    Class to hold our Edit User Activity. From this activity, Administrators are able to both
    edit and delete accounts within the Users database. All aspects of a user are able to be edited from this
    activity.
 */
public class EditUserActivity  extends AppCompatActivity {
    private String currentUser;
    private String phoneNumber;
    private String permission;
    TextView userAndPermissionText;
    ArrayList<WarehouseUser> warehouseUsers;
    ListView userList;
    com.google.android.material.bottomnavigation.BottomNavigationView NavBar;
    ArrayList<WarehouseUser> searchList;
    UserSQLDriver userDB;
    EditText searchBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_users_activity);

        //Assign all elements to variables
        userAndPermissionText = findViewById(R.id.userAndPermissionText);
        searchBar = findViewById(R.id.searchBar);
        searchList = new ArrayList<WarehouseUser>();
        userList = findViewById(R.id.userList);
        NavBar = findViewById(R.id.NavBar);

        //Open up a connection to our user database
        userDB = new UserSQLDriver(this);

        //Grab passed information from Login Activity to display current user information
        Bundle getData = getIntent().getExtras();
        assert getData != null;
        currentUser = getData.getString("User_Name");
        permission = getData.getString("Permission");
        userAndPermissionText.setText("Current User: " + currentUser + " | Permission: " + permission);

        if(userDB.getCount() > 0)
        {
            //Fill the list of users from the database
            warehouseUsers = userDB.returnAllItems();

            //Call our custom adapter to display the rows
            ListUserAdapter customRows = new ListUserAdapter(this, R.layout.user_list_item, currentUser, warehouseUsers);
            userList.setAdapter(customRows);
        }

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchList.clear();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binarySearch(searchBar.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        //Listener for Navbar
        NavBar.setOnItemSelectedListener(item ->
        {
            //Move to home
            if (item.getItemId() == R.id.home_navigation) {
                Bundle dataPass = new Bundle();
                dataPass.putString("User_Name", currentUser);
                dataPass.putString("Phone_Number", phoneNumber);
                dataPass.putString("Permission", permission);

                Intent startHomeActivity = new Intent(EditUserActivity.this, HomeActivity.class);
                startHomeActivity.putExtras(dataPass);
                startActivity(startHomeActivity);

                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                return true;
            }
            //Move to home
            if (item.getItemId() == R.id.add_item_navigation) {
                Bundle dataPass = new Bundle();
                dataPass.putString("User_Name", currentUser);
                dataPass.putString("Phone_Number", phoneNumber);
                dataPass.putString("Permission", permission);

                Intent startAddItemActivity = new Intent(EditUserActivity.this, AddItemActivity.class);
                startAddItemActivity.putExtras(dataPass);
                startActivity(startAddItemActivity);

                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                return true;
            }
            return true;
        });
    }

    public int findSearchStartIndex(int low, int start, int high, String itemName)
    {System.out.println("Searching for: " + itemName);
        if(low > high)
        {
            return start;
        }

        int mid = (int)Math.floor((low + high) / 2);
        String midItemName = warehouseUsers.get(mid).getUserName().toUpperCase();
        if(midItemName.startsWith(itemName) && itemName.length() <= midItemName.length())
        {
            System.out.println("Found start " + midItemName + " at " + mid);
            start = mid;
            high = mid - 1;
        }
        else if(itemName.compareTo(midItemName) < 0)
        {
            high = mid - 1;
        }
        else
        {
            low = mid + 1;
        }
        return findSearchStartIndex(low, start, high, itemName);
    }

    public int findSearchEndIndex(int low, int end, int high, String itemName)
    {
        System.out.println("Searching for: " + itemName);
        if(low > high)
        {
            return end;
        }

        int mid = (int)Math.floor((low + high) / 2);
        String midItemName = warehouseUsers.get(mid).getUserName().toUpperCase();
        if(midItemName.startsWith(itemName) && itemName.length() <= midItemName.length())
        {
            System.out.println("Found end " + itemName + " at " + mid);
            end = mid;
            low = mid + 1;

        }
        else if(itemName.compareTo(midItemName) < 0)
        {
            high = mid - 1;
        }
        else
        {
            low = mid + 1;
            System.out.println("End: looking at a high of: " + low);
        }
        return findSearchEndIndex(low, end, high, itemName);
    }

    public void binarySearch(String userName)
    {
        userName = userName.toUpperCase();
        if(userName.isEmpty())
        {
            if(userDB.getCount() > 0)
            {
                warehouseUsers = userDB.returnAllItems();

                //Call our custom adapter to display the rows
                ListUserAdapter customRows = new ListUserAdapter(this, R.layout.user_list_item, currentUser, warehouseUsers);
                userList.setAdapter(customRows);
                return;
            }
        }
        if(userDB.getCount() > 0)
        {
            //find the index range of the item
            int itemStart = findSearchStartIndex(0, -1, warehouseUsers.size() - 1, userName);
            int itemEnd = findSearchEndIndex(0, -1, warehouseUsers.size() - 1, userName);

            System.out.println(itemStart + " : " + itemEnd);
            //Check if the indexes are -1 or greater
            if(itemStart > -1 && itemEnd > -1)
            {
                if(itemStart == itemEnd)
                {
                    searchList.add(warehouseUsers.get(itemStart));
                }
                else {
                    for(int x = itemStart; x < itemEnd + 1; x++)
                    {
                        System.out.println(warehouseUsers.get(x).getUserName());
                        searchList.add(warehouseUsers.get(x));
                    }
                }
                //Call our custom adapter to display the rows
                ListUserAdapter customRows = new ListUserAdapter(this, R.layout.user_list_item, currentUser, searchList);
                userList.setAdapter(customRows);
            }
            else
            {
                searchList.clear();

                //Call our custom adapter to display the rows
                ListUserAdapter customRows = new ListUserAdapter(this, R.layout.user_list_item, currentUser, searchList);
                userList.setAdapter(customRows);
            }
        }
    }
}
