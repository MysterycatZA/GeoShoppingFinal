package com.example.geoshoppingfinal;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.example.geoshoppingfinal.ui.ItemList;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by Luke Shaw 17072613
 */
//Method for handling list view for displaying items
public class ItemListViewAdapter extends BaseAdapter {
    //Declaration and Initialisation
    private ArrayList<ItemList> data;                       //Array list of items
    private Context context;                            //Context of passed activity
    private static LayoutInflater inflater = null;         //Layout inflater
    //Constructor
    public ItemListViewAdapter(Context context, ArrayList<ItemList> data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override //Return array list size
    public int getCount() {
        return data.size();
    }

    @Override       //Get item
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override       //Get item id
    public long getItemId(int position) {
        return position;
    }

    //Method for adding an item to the list view
    public void add(ItemList item){
        data.add(item);
        notifyDataSetChanged();
    }
    //Method for deleting an item in the list view
    public void delete(int position){
        data.remove(position);
        notifyDataSetChanged();
    }
    //Method for handling of displaying the list view and on click listeneres
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // See if the view needs to be inflated
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.shopping_item_list, null);
        }
        final CheckedTextView simpleCheckedTextView = (CheckedTextView) view.findViewById(R.id.nameLabel);
        final ImageView deleteImage = (ImageView) view.findViewById(R.id.delete_button);
        // Get the data item
        final ItemList item = data.get(position);
        if(item.isBought()){
            simpleCheckedTextView.setCheckMarkDrawable(R.drawable.btn_check_on_holo);
            simpleCheckedTextView.setChecked(true);
        }
        else {
            simpleCheckedTextView.setCheckMarkDrawable(R.drawable.btn_check_off_holo);
            simpleCheckedTextView.setChecked(false);
        }
        // Display the data item's properties
        simpleCheckedTextView.setText(item.getName() + " x " + item.getQuantity());
        // perform on Click Event Listener on CheckedTextView
        simpleCheckedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (simpleCheckedTextView.isChecked()) {
        // set cheek mark drawable and set checked property to false
                    DataBase dataBase = new DataBase(context);
                    item.setBought(false);
                    if(dataBase.updateListItem(item)){
                        simpleCheckedTextView.setCheckMarkDrawable(R.drawable.btn_check_off_holo);
                        simpleCheckedTextView.setChecked(false);
                        data.get(position).setBought(false);
                    }
                } else {
            // set cheek mark drawable and set checked property to true
                    item.setBought(true);
                    DataBase dataBase = new DataBase(context);
                    if(dataBase.updateListItem(item)){
                        simpleCheckedTextView.setCheckMarkDrawable(R.drawable.btn_check_on_holo);
                        simpleCheckedTextView.setChecked(true);
                        data.get(position).setBought(true);
                    }
                }
            }
        });

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                       //Delete image click listener that displays a dialog to delete an item
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Are you sure you want to delete?");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {           //Yes
                        DataBase dataBase = new DataBase(context);
                        if(dataBase.deleteListItem(item)){
                            delete(position);
                            dialog.dismiss();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {       //No
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        return view;
    }
}
