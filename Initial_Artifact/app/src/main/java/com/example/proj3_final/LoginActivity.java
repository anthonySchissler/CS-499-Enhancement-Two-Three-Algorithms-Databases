package com.example.proj3_final;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
/*
    Written By: Anthony Schissler
    Date: 6/27/2025

    This class represents the LoginActivity users will first come upon
    when starting the app. This handles the user's ability to authenticate,
    as well as create a new user via the registration button
 */
public class LoginActivity extends AppCompatActivity {

    Activity activity;

    Button registerButton, LoginButton, ResetPasswordButton;

    EditText LoginField, PasswordField;

    UserSQLDriver userDB;

    //Method to build elements on the screen
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);


        //Assign all elements to variables
        activity = this;
        registerButton = findViewById(R.id.registerButton);
        LoginButton = findViewById(R.id.loginButton);
        LoginField = findViewById(R.id.usernameField);
        PasswordField = findViewById(R.id.passwordField);

        //Open up a connection to our user database
        userDB = new UserSQLDriver(this);

        //Set a listener to our Register button, which moves the context to the registration screen
        registerButton.setOnClickListener(View ->{
            Intent startRegister = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(startRegister);
        });

        //Set a listener to our Login button
        LoginButton.setOnClickListener(View ->
        {
            System.out.println(login());
        });

    }

    //Method to determine if a string is null or void of characters
    protected boolean textNotEmpty(String stringIn)
    {
        if(stringIn == null || stringIn.equals(""))
        {
            return false;
        }
        return true;
    }

    //Main login method
    protected boolean login()
    {
        //If all our fields are filled and valid
        if(textNotEmpty(LoginField.getText().toString()) && textNotEmpty(PasswordField.getText().toString()))
        {
            //Test authentication against the database
            if(userDB.loginUser(LoginField.getText().toString(), PasswordField.getText().toString()))
            {
                //Notify user of a successful login
                Toast.makeText(activity, "Login Successful, welcome " +  LoginField.getText().toString(),
                        Toast.LENGTH_LONG).show();

                //Pass the user's username into the application
                Bundle dataPass = new Bundle();
                dataPass.putString("User_Name", LoginField.getText().toString());
                dataPass.putString("Phone_Number", userDB.readUser(LoginField.getText().toString()).getPhoneNumber());

                //Change context to the Home activity
                Intent startHome  = new Intent(LoginActivity.this, HomeActivity.class);
                startHome.putExtras(dataPass);
                startActivity(startHome);

                return true;
            }
            //Notify user of a failed login
            else {
                Toast.makeText(activity, "Login failed, please try again", Toast.LENGTH_LONG).show();
            }

        }
        return false;
    }




}
