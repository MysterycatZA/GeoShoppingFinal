package com.example.geoshoppingfinal;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.geoshoppingfinal.ui.home.HomeViewModel;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by Luke Shaw 17072613
 */

public class DialogBox extends DialogFragment {

    //Declaration and Initialisation
    private String[] quantity = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private HomeViewModel model;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {                   //Method that handles the creation of the dialog box
        AlertDialog.Builder builder;                                            //Alert dialog builder object
        builder = buildStandardDialog();
        model = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        return builder.create();
    }

    public AlertDialog.Builder buildStandardDialog(){   //Method that builds an alert dialog with 2 buttons
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_item, null);
        final Spinner spinner = (Spinner) view.findViewById(R.id.itemQty);                 //Getting quantity from edit text

        spinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, quantity));
        builder.setView(view)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {          //Yes button
                        EditText textBox = (EditText) view.findViewById(R.id.itemName);                        //Getting name from edit text
                        Item tempItem = new Item(textBox.getText().toString(), Integer.parseInt(spinner.getSelectedItem().toString())); //Creating new text box object
                        model.addItem(tempItem);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {           //No button
                        dialog.dismiss();
                    }
                });
        return builder;
    }
}
