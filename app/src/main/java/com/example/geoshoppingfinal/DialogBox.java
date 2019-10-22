package com.example.geoshoppingfinal;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Created by Luke Shaw 15000303
 */

public class DialogBox extends DialogFragment {

    //Declaration and Initialisation
    public DialogResultItem dialogResultItem;             //Dialog result code loosely based off URL: http://www.coderzheaven.com/2013/07/01/return-dialogfragment-dialogfragments-android/


    public interface DialogResultItem{             //Interface used to pass the dialog result back to activity code loosely based off URL: http://www.coderzheaven.com/2013/07/01/return-dialogfragment-dialogfragments-android/
        void sendDialogResultItem(Item tempItem);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {                   //Method that handles the creation of the dialog box
        //Declaration and Initialisation
        Bundle arguments = getArguments();                                      //The passed bundle that contains the message title and message. Not sure if this is the best method of doing this.
        String title = arguments.getString("title");                            //Message title
        AlertDialog.Builder builder;                                            //Alert dialog builder object

        builder = buildStandardDialog();
        return builder.create();
    }

    public AlertDialog.Builder buildStandardDialog(){   //Method that builds an alert dialog with 2 buttons
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_item, null);
        builder.setView(view)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {          //Yes button
                        EditText textBox1;
                        dialogResultItem  = (DialogResultItem) getActivity();                          //Getting activity
                        textBox1 = (EditText) view.findViewById(R.id.itemName);                        //Getting name from edit text
                        EditText textBox2 = (EditText) view.findViewById(R.id.itemQty);                 //Getting quantity from edit text
                        Item tempItem = new Item(textBox1.getText().toString(), Integer.parseInt(textBox2.getText().toString())); //Creating new text box object
                        dialogResultItem.sendDialogResultItem(tempItem);
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
