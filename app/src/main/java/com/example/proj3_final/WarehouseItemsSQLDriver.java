package com.example.proj3_final;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

/*
    Written By: Anthony Schissler
    Date: 6/27/2025

    Custom Class implementation of a SQLLite driver for our warehouse items. This allows us to
    interface with the backend database to create and manipulate objects in the warehouse
 */
public class WarehouseItemsSQLDriver extends SQLiteOpenHelper {


    /*
        Parameters representing our database columns, database version, database title,
        and our table title.
     */
    private String column0UserID = "ID";
    private String column1ItemName = "Item_Name";
    private String column2ItemQuantity = "Item_Quantity";

    private String column3AlertStatus = "Alert_Status";

    private String column4AlertThreshold = "Alert_Threshold";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TITLE = "WarehouseItems.DB";
    private static final String TABLE_TITLE = "WarehouseItemsTable";



    //Creates a Table if it's not found
    @Override
    public void onCreate(SQLiteDatabase db) {
        //These set our columns within our database
        String creationCommand = "CREATE TABLE IF NOT EXISTS " +
                TABLE_TITLE + " (" +
                column0UserID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                column1ItemName + " VARCHAR," +
                column2ItemQuantity + " INTEGER, " +
                column3AlertStatus + " VARCHAR, " +
                column4AlertThreshold + " VARCHAR" +
                ");";

        //execute
        db.execSQL(creationCommand);

    }

    //constructor for the object with a call to its parent (SQLiteOpenHelper)
    public WarehouseItemsSQLDriver(Context context)
    {
        super(context, DATABASE_TITLE, null, DATABASE_VERSION);
    }

    //non-implemented mandatory method
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //Serves as the Create functionality for our CRUD model
    public boolean createWarehouseItem(WarehouseItem item)
    {
        try
        {
            //Check for item
            WarehouseItem checkItem = readWarehouseItem(item.getItemName());

            //If our read returns back an item with the same name, return false
            if(!Objects.equals(checkItem.getItemName(), ""))
            {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Otherwise, build a map with columns as keys and item parameters as values
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues inputMap = new ContentValues();
        inputMap.put(column1ItemName, item.getItemName());
        inputMap.put(column2ItemQuantity, item.getQuantity());
        inputMap.put(column3AlertStatus, item.getAlertOn());
        inputMap.put(column4AlertThreshold, item.getAlertThreshold());

        //Insert the map into the table as a row
        try
        {
            database.insert(TABLE_TITLE, null, inputMap);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Serves as the Read functionality for our CRUD model
    public WarehouseItem readWarehouseItem(String itemName)
    {
        //create a holder object
        WarehouseItem item;

        //Open up a readable connection to the database
        SQLiteDatabase database = this.getReadableDatabase();

        //Specify columns
        String[] columns = new String[] {column0UserID, column1ItemName, column2ItemQuantity, column3AlertStatus, column4AlertThreshold};

        //Set a search parameter on our name column
        String selection = column1ItemName + " = ?";

        //Set the object to search for
        String[] selectionArgs = new String[] {itemName};

        //Query returns a pointer to the database from our parameters
        Cursor databasePointer = database.query(TABLE_TITLE, columns, selection, selectionArgs, null, null, null, null);

        //If the pointer does contains at least one item, set our placeholder to this item
        if(databasePointer.getCount() > 0)
        {
            databasePointer.moveToFirst();
            item = new WarehouseItem(databasePointer.getString(1), databasePointer.getInt(2), databasePointer.getInt(3), databasePointer.getInt(4));
        }

        //If the pointer does not contain an item, set placeholder as an empty object
        else {
            item = new WarehouseItem("", -1, 0, -1);
        }

        //close and return
        databasePointer.close();
        return item;
    }

    //Serves as the Update functionality for our CRUD model
    public boolean updateItem(WarehouseItem item)
    {
        //Open writable connection to our databse
        SQLiteDatabase database = this.getWritableDatabase();

        //Build a map with values from our WarehouseItem parameter
        ContentValues inputMap = new ContentValues();
        inputMap.put(column1ItemName, item.getItemName());
        inputMap.put(column2ItemQuantity, item.getQuantity());
        inputMap.put(column3AlertStatus, item.getAlertOn());
        inputMap.put(column4AlertThreshold, item.getAlertThreshold());

        //Perform a row update on the targeted object name with our map
        try
        {
            database.update(TABLE_TITLE, inputMap, column1ItemName + " = ?", new String[] {item.getItemName()});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Serves as the Delete functionality for our CRUD model
    public boolean deleteItem(WarehouseItem item)
    {
        //Open writable connection to our database
        SQLiteDatabase database = this.getWritableDatabase();

        //Perform a row deletion on the targeted object using our WarehouseItem name
        try
        {
            database.delete(TABLE_TITLE, column1ItemName + " = ?", new String[] { item.getItemName()});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Method to return all records in our database as a list
    public ArrayList<WarehouseItem> returnAllItems(String columnType)
    {
        String columnSelection = "";
        String order = "";

        switch(columnType)
        {
            case "nameAscending":
                columnSelection = column1ItemName + " ";
                order = " ASC";
                break;
            case "nameDescending":
                columnSelection = column1ItemName + " ";
                order = " DESC";
                break;
            case "quantityAscending":
                columnSelection = column2ItemQuantity + " ";
                order = " ASC";
                break;
            case "quantityDescending":
                columnSelection = column2ItemQuantity + " ";
                order = " DESC";
                break;

        }
        //Instantiate a new list
        ArrayList<WarehouseItem> itemList = new ArrayList<>();

        //Open a readable connection
        SQLiteDatabase db = this.getReadableDatabase();
    
        String orderBy = " ORDER BY ";

        //Perform a query to return all objects from our table
        Cursor databasePointer = db.rawQuery("SELECT * FROM " + TABLE_TITLE + orderBy + columnSelection + order, null);

        //Loop while the pointer has more records
        while (databasePointer.moveToNext()) {
            String itemName = databasePointer.getString(1);
            int itemQuantity = Integer.parseInt(databasePointer.getString(2));

            //Build our object based off of this row
            WarehouseItem item = new WarehouseItem(itemName, itemQuantity, databasePointer.getInt(3), databasePointer.getInt(4));

            //Add the object to our list
            itemList.add(item);
        }

        //close and return the list
        databasePointer.close();
        return itemList;
    }

    //Method to subtract a certain quantity from a certain item in the warehouse
    public boolean subtractItemQuantity(int num, WarehouseItem warehouseItem)
    {
        //Create a temp object
        WarehouseItem newItem = new WarehouseItem(warehouseItem.getItemName(), warehouseItem.getQuantity(), warehouseItem.getAlertOn(), warehouseItem.getAlertThreshold());

        //Check what the resultant value would be by decreasing num from quantity
        int check = newItem.decreaseCount(num);

        //If the operation is allowed
        if(check > -1)
        {
            //If the quantity reaches 0, remove our object
            if(check == 0)
            {
                deleteItem(newItem);
                return true;
            }

            //Else, set the temp object with the new quantity, perform update
            else
            {
                newItem.setQuantity(check);
                updateItem(newItem);
                return true;
            }

        }
        return false;
    }

    //Method to subtract a singular quantity from a certain item in the warehouse
    public boolean subtractItemQuantity(WarehouseItem warehouseItem)
    {
        //Store temp item
        WarehouseItem newItem = new WarehouseItem(warehouseItem.getItemName(), warehouseItem.getQuantity(), warehouseItem.getAlertOn(), warehouseItem.getAlertThreshold());

        //Check the resultant quantity
        int check = newItem.decreaseCount(1);

        //If the result is allowed
        if(check > -1)
        {
            //If the quantity reaches 0, remove our object
            if(check == 0)
            {
                deleteItem(newItem);
                return true;
            }

            //Else, set the temp object with the new quantity, perform update
            else
            {
                newItem.setQuantity(check);
                updateItem(newItem);
                return true;
            }
        }
        return false;
    }

    //Method to add a certain quantity to a certain item in the warehouse
    public boolean addItemQuantity(int num, WarehouseItem warehouseItem)
    {
        //Store temp item
        WarehouseItem newItem = new WarehouseItem(warehouseItem.getItemName(), warehouseItem.getQuantity(), warehouseItem.getAlertOn(), warehouseItem.getAlertThreshold());

        //Check the resultant quantity
        int check = newItem.increaseCount(num);

        //If the result is allowed set the temp object with the new quantity, perform update
        if(check > -1 && check < Integer.MAX_VALUE)
        {
            newItem.setQuantity(check);
            updateItem(newItem);
            return true;
        }
        return false;
    }

    //Method to add a singular quantity to a certain item in the warehouse
    public boolean addItemQuantity(WarehouseItem warehouseItem)
    {
        //Store temp item
        WarehouseItem newItem = new WarehouseItem(warehouseItem.getItemName(), warehouseItem.getQuantity(), warehouseItem.getAlertOn(), warehouseItem.getAlertThreshold());

        //Check the resultant quantity
        int check = newItem.increaseCount(1);

        //If the result is allowed set the temp object with the new quantity, perform update
        if(check > -1 && check < Integer.MAX_VALUE)
        {
            newItem.setQuantity(check);
            updateItem(newItem);
            return true;
        }
        return false;
    }

    //Method to set the threshold of an alert for an object
    public boolean setAlertThreshold(int num, WarehouseItem warehouseItem)
    {
        if(num >= warehouseItem.getQuantity())
        {
            return false;
        }
        WarehouseItem newItem = new WarehouseItem(warehouseItem.getItemName(), warehouseItem.getQuantity(), 1, num);
        //System.out.println(newItem.getAlertThreshold());
        updateItem(newItem);
        return true;
    }

    //Method to clear the "alertOn" flag as well as reset the threshold
    public void clearAlerts(WarehouseItem warehouseItem)
    {
        WarehouseItem newItem = new WarehouseItem(warehouseItem.getItemName(), warehouseItem.getQuantity(), 0, -1);
        updateItem(newItem);
    }

    //return the size of the database in number of rows
    public int getCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor databasePointer = db.rawQuery("SELECT * FROM " + TABLE_TITLE, null);
        int total = databasePointer.getCount();
        databasePointer.close();
        return total;

    }

    //Nuke the database
    public void clearDatabase(String TABLE_NAME) {
        SQLiteDatabase db = this.getReadableDatabase();
        String clearDBQuery = "DELETE FROM " + TABLE_NAME;
        db.execSQL(clearDBQuery);
    }


}
