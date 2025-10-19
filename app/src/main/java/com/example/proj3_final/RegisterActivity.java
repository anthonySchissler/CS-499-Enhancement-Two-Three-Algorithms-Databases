package com.example.proj3_final;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/*
    Written By: Anthony Schissler
    Date: 6/27/2025

    A class to perform user registration, reachable via the login screen.
 */
public class RegisterActivity extends AppCompatActivity {
    EditText usernameField, passwordField, phoneField;

    Button registerButton;
    Activity activity;

    UserSQLDriver userDB;

    //Method to build elements on the screen
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        //Assign all elements to variables
        activity = this;
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        phoneField = findViewById(R.id.phoneField);
        registerButton = findViewById(R.id.registerButton);

        //Open up a new database connection using our user driver
        userDB = new UserSQLDriver(this);

        //Set a listener for our registration button when clicked
        registerButton.setOnClickListener(View ->{
            register();
        });
    }

    //Method to check user fields and create a new registration for our user datbase
    protected boolean register()
    {
        //Check if all fields have values
        if(textNotEmpty(usernameField.getText().toString()) && textNotEmpty(passwordField.getText().toString()) && textNotEmpty(phoneField.getText().toString()))
        {
            //Assign a new user object with the values from the registration fields
            WarehouseUser newUser = new WarehouseUser(usernameField.getText().toString(), passwordField.getText().toString(), phoneField.getText().toString(), WarehouseUser.EMPLOYEE);

            try {
                    //Attempt to create the user in the database
                    boolean check = userDB.createUser(newUser);

                    //If user created
                    if(check == true)
                    {
                        //Notify user
                        Toast.makeText(activity, "Registration complete. Please login with your new account.", Toast.LENGTH_LONG).show();

                        //Move screen back to login
                        Intent startRegister = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(startRegister);
                        return true;
                    }

                    //Another user was found with this username
                    else
                    {
                        //Notify user
                        Toast.makeText(activity, "Username already taken. Please choose another.", Toast.LENGTH_LONG).show();
                        return false;
                    }


            } catch (Exception e) {
                return false;
            }
        }

        //All fields weren't filled out, notify user
        else
        {
            Toast.makeText(activity, "Registration failed. Please ensure all fields are filled out correctly", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    //Method to determine if a string is either null or void of characters
    protected boolean textNotEmpty(String stringIn)
    {
        if(stringIn == null || stringIn.equals(""))
        {
            return false;
        }
        return true;
    }
}
