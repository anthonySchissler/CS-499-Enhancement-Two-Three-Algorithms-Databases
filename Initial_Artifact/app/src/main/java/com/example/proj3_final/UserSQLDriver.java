package com.example.proj3_final;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Arrays;
import java.util.Objects;

/*
    Written By: Anthony Schissler
    Date: 6/27/2025

    Custom Class implementation of a SQLLite driver for our warehouse users. This allows us to
    interface with the backend database to create and modify users.
 */
public class UserSQLDriver extends SQLiteOpenHelper {

    /*
        Parameters representing our database columns, database version, database title,
        and our table title.
     */
    private String column0UserID = "ID";
    private String column1UserName = "User_Name";
    private String column2UserPassword = "Password";
    private String column3UserPhoneNumber = "Phone_Number";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TITLE = "Users.DB";
    private static final String TABLE_TITLE = "UsersTable";


    //Creates a Table if it's not found
    @Override
    public void onCreate(SQLiteDatabase db) {
        String creationCommand = "CREATE TABLE IF NOT EXISTS " +
                TABLE_TITLE + " (" +
                column0UserID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                column1UserName + " VARCHAR, " +
                column2UserPassword + " VARCHAR, " +

                column3UserPhoneNumber + " VARCHAR " +
                ");";

        db.execSQL(creationCommand);

    }

    //constructor for the object with a call to its parent (SQLiteOpenHelper)
    public UserSQLDriver(Context context)
    {
        super(context, DATABASE_TITLE, null, DATABASE_VERSION);
    }

    //non-implemented mandatory method
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //Serves as the Create functionality for our CRUD model
    public boolean createUser(WarehouseUser user)
    {

        try
        {
            //Check for user
            WarehouseUser checkUser = readUser(user.getUserName());

            //If our read returns back an item with the same name, return false
            if(!Objects.equals(checkUser.getUserName(), ""))
            {
                return false;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Otherwise, build a map with columns as keys and item parameters as values
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues inputMap = new ContentValues();
        inputMap.put(column1UserName, user.getUserName());
        inputMap.put(column2UserPassword, user.getPassword());
        inputMap.put(column3UserPhoneNumber, user.getPhoneNumber());

        //Insert the map into the table as a row
        try
        {
            database.insert(TABLE_TITLE, null, inputMap);
            return true;

        } catch (Exception e) {
            e.printStackTrace();

        }

        return false;
    }

    //Serves as the Read functionality for our CRUD model
    public WarehouseUser readUser(String userName)
    {
        //Create holder object
        WarehouseUser user;

        //Open readable connection
        SQLiteDatabase database = this.getReadableDatabase();

        //Specify columns
        String[] columns = new String[] {column0UserID, column1UserName, column2UserPassword, column3UserPhoneNumber};

        //Set search parameter for Name column
        String selection = column1UserName + " = ?";

        //Set object to search for
        String[] selectionArgs = new String[] {userName};

        try
        {
            //Query returns a pointer to the database from our parameters
            Cursor databasePointer = database.query(TABLE_TITLE, columns, selection, selectionArgs, null, null, null, null);

            //If the pointer does contains at least one item, set our placeholder to this item
            if(databasePointer.getCount() > 0)
            {
                databasePointer.moveToFirst();
                 user = new WarehouseUser(databasePointer.getString(1),
                        databasePointer.getString(2),  databasePointer.getString(3));

            }

            //If the pointer does not contain an item, set placeholder as an empty object
            else {
                 user = new WarehouseUser("",
                        "",  "");
            }

            //close and return
            databasePointer.close();
            return user;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    //Serves as the Update functionality for our CRUD model
    public boolean updateUser(WarehouseUser user)
    {
        //Open writable connection to our database
        SQLiteDatabase database = this.getWritableDatabase();

        //Build a map with values from our WarehouseUser parameter
        ContentValues inputMap = new ContentValues();
        inputMap.put(column1UserName, user.getUserName());
        inputMap.put(column2UserPassword, user.getPassword());
        inputMap.put(column3UserPhoneNumber, user.getPhoneNumber());

        //Perform a row update on the targeted object name with our map
        try
        {
            database.update(TABLE_TITLE, inputMap, column1UserName + " = ?", new String[] {user.getUserName()});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Serves as the Delete functionality for our CRUD model
    public boolean deleteUser(WarehouseUser user)
    {
        //Open writable connection to our database
        SQLiteDatabase database = this.getWritableDatabase();

        //Perform a row deletion on the targeted object using our WarehouseItem name
        try
        {
            database.delete(TABLE_TITLE, column1UserName + " = ?", new String[] { user.getUserName()});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Login method to provide authentication for users
    protected boolean loginUser(String username, String password)
    {
        //Store the search for a user in a temp object
        WarehouseUser user = readUser(username);

        //If that user has a password set that equals the password parameter, login successful
        if(user.getPassword().equals(password))
        {
            return true;
        }

        //Else, login failure
        return false;
    }
}
