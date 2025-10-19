package com.example.proj3_final;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

/*
    Written By: Anthony Schissler
    Date: 10/5/2025

    Class used as a custom implementation of the ArrayAdapter class.
    This is used to display our database users in a table on the Edit Users page,
    and allow Administrators to update these users within their row.
 */
public class ListUserAdapter extends ArrayAdapter<WarehouseUser> {
    ListUserAdapter adapter;
    private int resourceLayout;
    final Activity mContext;

    ArrayList<WarehouseUser> warehouseUsers;

    UserSQLDriver warehouseUsersDB;

    String currentUser;

    public ListUserAdapter(Activity context, int resource, String currentUser, ArrayList<WarehouseUser> warehouseUsers) {
        super(context, resource, warehouseUsers);
        this.resourceLayout = resource;
        this.mContext = context;
        this.warehouseUsers = warehouseUsers;
        this.currentUser = currentUser;
        this.adapter = this;
        warehouseUsersDB = new UserSQLDriver(this.getContext());
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        //Generate the view
        View thisView = convertView;
        if (thisView == null) {
            LayoutInflater layoutInflater;
            layoutInflater = LayoutInflater.from(mContext);
            thisView = layoutInflater.inflate(resourceLayout, null);
        }

        //Grab the item at the current index of the iterator
        WarehouseUser warehouseUser = getItem(pos);

        //If it's not null
        if (warehouseUser != null) {
            //Assign row elements to variables
            TextView userNameField = (TextView) thisView.findViewById(R.id.userListName);
            TextView userPermissionField = (TextView) thisView.findViewById(R.id.userListPermission);
            ImageButton editUserButton = (ImageButton) thisView.findViewById(R.id.editUserButton);
            ImageButton deleteUserButton = (ImageButton) thisView.findViewById(R.id.deleteUserButton);

            if (null != userNameField) {
                userNameField.setText("User: " + warehouseUser.getUserName());
            }

            if (null != userPermissionField) {
                userPermissionField.setText("Permission: " + warehouseUser.getPermission());
            }

            if (null != editUserButton) {
                if(currentUser.equals(warehouseUser.getUserName()))
                {
                    editUserButton.setColorFilter(mContext.getColor(R.color.teal_700));
                }
                editUserButton.setOnClickListener(item ->
                {
                    onEditUserWindow(item, warehouseUser);
                });
            }
            if(null != deleteUserButton)
            {
                if(currentUser.equals(warehouseUser.getUserName()))
                {
                    deleteUserButton.setColorFilter(mContext.getColor(R.color.red));
                }
                deleteUserButton.setOnClickListener(item ->
                {
                    if(currentUser.equals(warehouseUser.getUserName()))
                    {
                        //Move context to Edit Users Activity
                        Intent startLogin = new Intent(mContext, LoginActivity.class);
                        Toast.makeText(mContext, "User deleted their own account, returning to login screen", Toast.LENGTH_LONG).show();
                        mContext.startActivity(startLogin);
                    }
                    else
                    {
                        warehouseUsersDB.deleteUser(warehouseUser);
                        Toast.makeText(mContext, "Deleted user " + warehouseUser.getUserName(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        return thisView;
    }

    public void onEditUserWindow(View view, WarehouseUser user)
    {
        //Inflate popup window with the edit user popup
        View popup = LayoutInflater.from(view.getContext()).inflate(R.layout.popup_edit_user, null);
        final PopupWindow popupWindow = new PopupWindow(popup, 1000, 1200, true);

        //Initialize popup variables
        TextView editUserTitle = (TextView)popup.findViewById(R.id.editUserTitle);
        EditText editUserName = (EditText)popup.findViewById(R.id.editUserNameText);
        editUserName.setText(user.getUserName());
        Spinner editUserPermission = populatePermissionDropdown((Spinner)popup.findViewById(R.id.editUserPermissionDropdown));

        //Set the user's current permission level on the dropdown
        if(user.getPermissionInt() == WarehouseUser.ADMINISTRATOR)
        {
            editUserPermission.setSelection(WarehouseUser.ADMINISTRATOR);
        }
        else if(user.getPermissionInt() == WarehouseUser.MANAGER)
        {
            editUserPermission.setSelection(WarehouseUser.MANAGER);
        }
        else
        {
            editUserPermission.setSelection(WarehouseUser.EMPLOYEE);
        }

        EditText editUserPasswordText = (EditText)popup.findViewById(R.id.editUserPasswordText);
        editUserPasswordText.setText(user.getPassword());
        EditText editUserPhoneText = (EditText)popup.findViewById(R.id.editUserPhoneText);
        editUserPhoneText.setText(user.getPhoneNumber());
        Button editUserConfirmButton = (Button)popup.findViewById(R.id.editUserConfirmButton);

        editUserConfirmButton.setOnClickListener(viewPopup ->
        {
            //Update with parameters
            WarehouseUser userHolder = new WarehouseUser(editUserName.getText().toString(), editUserPasswordText.getText().toString(), editUserPhoneText.getText().toString(), editUserPermission.getSelectedItemPosition());
            warehouseUsersDB.updateUser(user.getUserName(), userHolder);

            //Update UI
            ArrayList<WarehouseUser> newList = warehouseUsersDB.returnAllItems();
            warehouseUsers.clear();
            warehouseUsers.addAll(newList);
            adapter.notifyDataSetChanged();

            //Notify current user
            Toast.makeText(mContext, user.getUserName() + " has been updated", Toast.LENGTH_LONG).show();

            //Dismiss popup
            popupWindow.dismiss();
        });
        popupWindow.showAtLocation(popup, Gravity.CENTER, 0 ,0);
    }

    public Spinner populatePermissionDropdown(Spinner dropdown)
    {
        String[] choices = new String[]{"Administrator", "Manager", "Employee"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.mContext, android.R.layout.simple_spinner_dropdown_item, choices);
        dropdown.setAdapter(adapter);
        return dropdown;
    }

}