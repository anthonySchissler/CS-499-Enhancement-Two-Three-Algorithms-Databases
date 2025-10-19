package com.example.proj3_final;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.pdf.models.ListItem;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

    EditText searchBar;

    ListView itemList;

    ImageButton sortButton;

    public ListItemAdapter itemListAdapter;

    ArrayList<WarehouseItem> warehouseItems;

    ArrayList<WarehouseItem> searchList;

    com.google.android.material.bottomnavigation.BottomNavigationView NavBar;

    WarehouseItemsSQLDriver warehouseItemsDB;

    private static boolean smsPermission;

    public static String sortType = "nameAscending";

    private static String permission;

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
        searchBar = findViewById(R.id.searchBar);
        sortButton = findViewById(R.id.sortButton);
        warehouseItemsDB = new WarehouseItemsSQLDriver(this);
        searchList = new ArrayList<WarehouseItem>();

        //Use for removal of Database
        //this.deleteDatabase("WarehouseItems.DB");

        //Grab passed information from Login Activity to display current user information
        Bundle getData = getIntent().getExtras();
        assert getData != null;
        currentUser = getData.getString("User_Name");
        permission = getData.getString("Permission");
        userAndPermissionText.setText("Current User: " + currentUser + " | Permission: " + permission);

        //Apply permissions
        if(permission.equals(WarehouseUser.getPermission(WarehouseUser.EMPLOYEE)))
        {
            NavBar.getMenu().removeItem(R.id.add_item_navigation);
            NavBar.getMenu().removeItem(R.id.edit_users_navigation);
        }
        else if(permission.equals(WarehouseUser.getPermission(WarehouseUser.MANAGER)))
        {
            NavBar.getMenu().removeItem(R.id.edit_users_navigation);
        }

        //If our Warehouse Items database has more than one object
        if(warehouseItemsDB.getCount() > 0)
        {
            //Update UI with those items
            warehouseItems = warehouseItemsDB.returnAllItems(HomeActivity.sortType);

            //Call our custom adapter to display the rows
            ListItemAdapter customRows = new ListItemAdapter(this, R.layout.list_item, warehouseItems);
            itemList.setAdapter(customRows);
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

        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSortType(0);
                warehouseItems = warehouseItemsDB.returnAllItems(HomeActivity.sortType);

                //Call our custom adapter to display the rows
                ListItemAdapter customRows = new ListItemAdapter((Activity) v.getContext(), R.layout.list_item, warehouseItems);
                itemList.setAdapter(customRows);
            }
        });

        //Set listener for sort button
       sortButton.setOnClickListener(this::onSortingPopup);
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
                dataPass.putString("Permission", permission);

                //Move context to Add Item Activity
                Intent startAddItem  = new Intent(HomeActivity.this, AddItemActivity.class);
                startAddItem.putExtras(dataPass);
                startActivity(startAddItem);
                overridePendingTransition( R.anim.slide_in_other, R.anim.slide_out);
                return true;
            }
            else if(item.getItemId() == R.id.edit_users_navigation)
            {
                //Pass user login info
                Bundle dataPass = new Bundle();
                dataPass.putString("User_Name", currentUser);
                dataPass.putString("Phone_Number", phoneNumber);
                dataPass.putString("Permission", permission);

                //Move context to Edit Users Activity
                Intent startEditUsers  = new Intent(HomeActivity.this, EditUserActivity.class);
                startEditUsers.putExtras(dataPass);
                startActivity(startEditUsers);
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

    public static String getPermission()
    {
        return permission;
    }

    public static void setSMSPermission(boolean permission)
    {
        smsPermission = permission;
    }

    public static boolean getSMSPermission()
    {
        return smsPermission;
    }

    //Method to find the starting index for our potential string of items
    public int findSearchStartIndex(int low, int start, int high, String itemName)
    {
        System.out.println("Searching for: " + itemName);

        //if our search lateral limits reach, then our search is concluded
        if(low > high)
        {
            return start;
        }

        //Find the midpoint index between our lower and upper bound
        int mid = (int)Math.floor((low + high) / 2);
        String midItemName = warehouseItems.get(mid).getItemName().toUpperCase();

        //If the item at our mid index starts with our target name, search to the left
        if(midItemName.startsWith(itemName) && itemName.length() <= midItemName.length())
        {
            System.out.println("Found start " + midItemName + " at " + mid);
            start = mid;
            high = mid - 1;
        }

        //If we didn't find the item and it's value is lower than mid, search to the left to find the occurence
        else if(itemName.compareTo(midItemName) < 0)
        {
            high = mid - 1;
        }

        //If we didn't find the item and it's value is higher than mid, search to the right to find the occurence
        else
        {
            low = mid + 1;
        }

        //Recursively search to find the item's occurence
        return findSearchStartIndex(low, start, high, itemName);
    }

    //Method to find the ending index for our potential string of items
    public int findSearchEndIndex(int low, int end, int high, String itemName)
    {
        System.out.println("Searching for: " + itemName);

        //if our search lateral limits reach, then our search is concluded
        if(low > high)
        {
            return end;
        }

        //Find the midpoint index between our lower and upper bound
        int mid = (int)Math.floor((low + high) / 2);
        String midItemName = warehouseItems.get(mid).getItemName().toUpperCase();

        //If the item at our mid index starts with our target name, search to the right
        if(midItemName.startsWith(itemName) && itemName.length() <= midItemName.length())
        {
            System.out.println("Found end " + itemName + " at " + mid);
            end = mid;
            low = mid + 1;

        }

        //If we didn't find the item and it's value is lower than mid, search to the left to find the occurence
        else if(itemName.compareTo(midItemName) < 0)
        {
            high = mid - 1;
        }

        //If we didn't find the item and it's value is higher than mid, search to the right to find the occurence
        else
        {
            low = mid + 1;
            System.out.println("End: looking at a high of: " + low);
        }

        //Recursively search to find the item's occurence
        return findSearchEndIndex(low, end, high, itemName);
    }

    //Method to search our item list to find items that start with our input value
    public void binarySearch(String itemName)
    {
        itemName = itemName.toUpperCase();

        //If our parameter is empty, then set the list back to its original "name-Ascending" state
        if(itemName.isEmpty())
        {
            if(warehouseItemsDB.getCount() > 0)
            {
                warehouseItems = warehouseItemsDB.returnAllItems("nameAscending");

                //Call our custom adapter to display the rows
                ListItemAdapter customRows = new ListItemAdapter(this, R.layout.list_item, warehouseItems);
                itemList.setAdapter(customRows);
                return;
            }
        }

        //otherwise, conduct a binary search
        if(warehouseItemsDB.getCount() > 0)
        {
            //find the index range of the item
            int itemStart = findSearchStartIndex(0, -1, warehouseItems.size() - 1, itemName);
            int itemEnd = findSearchEndIndex(0, -1, warehouseItems.size() - 1, itemName);

            System.out.println(itemStart + " : " + itemEnd);
            //Check if the indexes are -1 or greater
            if(itemStart > -1 && itemEnd > -1)
            {
                //If both index equal, and are greater than -1, we only have one item
                if(itemStart == itemEnd)
                {
                    searchList.add(warehouseItems.get(itemStart));
                }

                //if indexes vary
                else {
                    for(int x = itemStart; x < itemEnd + 1; x++)
                    {
                        //at the elements between the indexes (inclusive) to the searchList
                        System.out.println(warehouseItems.get(x).getItemName());
                        searchList.add(warehouseItems.get(x));
                    }
                }

                //Call our custom adapter to display the rows
                ListItemAdapter customRows = new ListItemAdapter(this, R.layout.list_item, searchList);
                itemList.setAdapter(customRows);
            }
            else
            {
                searchList.clear();

                //Call our custom adapter to display the rows
                ListItemAdapter customRows = new ListItemAdapter(this, R.layout.list_item, searchList);
                itemList.setAdapter(customRows);
            }
        }
    }

    //Method to change the way the items are displayed in the Home page
    public void onSortingPopup(View view)
    {
        System.out.println("Opening popup");

        //Inflate popup with popup sorting
        View popup = LayoutInflater.from(view.getContext()).inflate(R.layout.popup_filter_and_sort, null);
        final PopupWindow popupWindow = new PopupWindow(popup, 850, 1050, true);

        //Assign variables
        RadioGroup sortBy = (RadioGroup)popup.findViewById(R.id.sortByRadioButtons);
        RadioButton sortByName = (RadioButton)popup.findViewById(R.id.sortNameRadioButton);
        RadioButton sortByQuantity = (RadioButton)popup.findViewById(R.id.sortQuantityRadioButton);
        RadioGroup orderBy = (RadioGroup)popup.findViewById(R.id.orderByRadioButtons);
        RadioButton orderByAscending = (RadioButton)popup.findViewById(R.id.orderAscendingRadioButton);
        RadioButton orderByDescending = (RadioButton)popup.findViewById(R.id.orderDescendingRadioButton);
        Button confirmSortingButton = (Button)popup.findViewById(R.id.confirmSortingButton);

        //Switch statement to ensure our options line up with previously selected options
        switch(HomeActivity.sortType)
        {
            case "nameAscending":
                sortByName.setChecked(true);
                orderByAscending.setChecked(true);
                break;
            case "nameDescending":
                sortByName.setChecked(true);
                orderByDescending.setChecked(true);
                break;
            case "quantityAscending":
                sortByQuantity.setChecked(true);
                orderByAscending.setChecked(true);
                break;
            case "quantityDescending":
                sortByQuantity.setChecked(true);
                orderByDescending.setChecked(true);
                break;
        }

        //Listeners to change sorting parameters using bubbles
            sortByName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(orderByAscending.isChecked())
                    {
                        changeSortType(0);
                    }
                    else
                    {
                        changeSortType(1);
                    }
                }
            });
            sortByQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(orderByAscending.isChecked())
                    {
                        changeSortType(2);
                    }
                    else
                    {
                        changeSortType(3);
                    }
                }
            });
            orderByAscending.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sortByName.isChecked())
                    {
                        changeSortType(0);
                    }
                    else
                    {
                        changeSortType(2);
                    }
                }
            });
            orderByDescending.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sortByQuantity.isChecked())
                    {
                        changeSortType(3);
                    }
                    else
                    {
                        changeSortType(1);
                    }
                }
            });

        //Listener for button to confirm sorting options
        confirmSortingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //return the items from the DB based off their sort type
                warehouseItems = warehouseItemsDB.returnAllItems(HomeActivity.sortType);
                ListItemAdapter customRows = new ListItemAdapter((Activity) v.getContext(), R.layout.list_item, warehouseItems);
                itemList.setAdapter(customRows);
                popupWindow.dismiss();
            }
        });

        popupWindow.showAtLocation(popup, Gravity.CENTER, 0 ,0);
    }


    //Helper method to change the current sort type context
    public void changeSortType(int sortType)
    {

        switch(sortType)
        {
            case 0:
                HomeActivity.sortType = "nameAscending";
                break;
            case 1:
                HomeActivity.sortType = "nameDescending";
                break;
            case 2:
                HomeActivity.sortType = "quantityAscending";
                break;
            case 3:
                HomeActivity.sortType = "quantityDescending";
                break;
        }

        System.out.println(HomeActivity.sortType);
    }


}
