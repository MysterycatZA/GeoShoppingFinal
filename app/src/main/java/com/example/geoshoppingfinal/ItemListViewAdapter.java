package com.example.geoshoppingfinal;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by Luke Shaw 17072613
 */
//Method for handling list view for displaying items
public class ItemListViewAdapter extends BaseAdapter {
    //Declaration and Initialisation
    private ArrayList<ItemList> data;                       //Array list of items
    private Context context;                                //Context of passed activity
    private static LayoutInflater inflater = null;          //Layout inflater
    private DataBase dataBase;                              //Database
    //Constructor
    public ItemListViewAdapter(Context context, ArrayList<ItemList> data) {
        this.context = context;
        this.data = data;
        this.dataBase = new DataBase(context);
        sortChecked();
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    //Method to sort via boolean based off https://stackoverflow.com/questions/28002342/sort-an-arraylist-by-primitive-boolean-type
    private void sortChecked(){
        Collections.sort(data, new Comparator<ItemList>() {
            @Override
            public int compare(ItemList item1, ItemList item2) {
                return Boolean.compare(item1.isBought(),item2.isBought());
            }
        });
        addSeparator();
    }
    //Method to reset view
    private void resetView(){
        removeSeparator();
        removeClear();
        sortChecked();
        notifyDataSetChanged();
    }
    //Method to remove clear bought item
    private void removeClear(){
        boolean found = false;
        int index = 0;
        while(!found && index < data.size()){
            if(data.get(index).isClearBought()){
                data.remove(index);
                found = true;
            }
            index++;
        }
    }
    //Method to remove seperator item
    private void removeSeparator(){
        boolean found = false;
        int index = 0;
        while(!found && index < data.size()){
            if(data.get(index).isSeparator()){
                data.remove(index);
                found = true;
            }
            index++;
        }
    }
    //Method to add separator item
    private void addSeparator(){
        boolean found = false;
        int index = 0;
        while(!found && index < data.size()){
            if(data.get(index).isBought()){
                ItemList itemList2 = new ItemList(true);
                itemList2.setSeparator(false);
                itemList2.setClearBought(true);
                data.add(index, itemList2);
                ItemList itemList = new ItemList(true);
                data.add(index, itemList);
                found = true;
            }
            index++;
        }
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

    @Override
    public boolean isEnabled(int position) {
        return !data.get(position).isSeparator();
    }

    private void deleteBought(){
        Iterator<ItemList> i = data.iterator();
        while (i.hasNext()) {
            ItemList itemList = i.next(); // must be called before you can call i.remove()
            if(itemList.isBought()){
                i.remove();
                data.remove(itemList);
                dataBase.deleteListItem(itemList);
            }
        }
    }

    //Method for handling of displaying the list view and on click listeneres
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // See if the view needs to be inflated
        View view = null;//convertView;

        if(data.get(position).isSeparator()){
            view = inflater.inflate(R.layout.seperator_item_list, null);
            TextView separatorView = (TextView) view.findViewById(R.id.separator);
            separatorView.setText("Bought");
        }
        else if(data.get(position).isClearBought()){
            view = inflater.inflate(R.layout.clear_item_list, null);
            TextView separatorView = (TextView) view.findViewById(R.id.clear);
            separatorView.setText("Tap here to clear bought items");
            separatorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Are you sure you want to delete all bought?");

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {           //Yes
                            deleteBought();
                            resetView();
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
        }
        else {
            view = inflater.inflate(R.layout.shopping_item_list, null);

            final CheckedTextView simpleCheckedTextView = (CheckedTextView) view.findViewById(R.id.nameLabel);
            final ImageView deleteImage = (ImageView) view.findViewById(R.id.delete_button);
            ImageView editImage = view.findViewById(R.id.edit_button);
            editImage.setVisibility(View.VISIBLE);

            editImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editItem(position);
                }
            });
            // Get the data item
            final ItemList item = data.get(position);
            if (item.isBought()) {
                simpleCheckedTextView.setCheckMarkDrawable(R.drawable.btn_check_on_holo);
                simpleCheckedTextView.setPaintFlags(simpleCheckedTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                simpleCheckedTextView.setChecked(true);
            } else {
                simpleCheckedTextView.setCheckMarkDrawable(R.drawable.btn_check_off_holo);
                simpleCheckedTextView.setPaintFlags(simpleCheckedTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                simpleCheckedTextView.setChecked(false);
            }
            // Display the data item's properties
            simpleCheckedTextView.setText(item.getName() + " x " + item.getQuantity());
            // perform on Click Event Listener on CheckedTextView
            simpleCheckedTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (simpleCheckedTextView.isChecked()) {
                        // set cheek mark drawable and set total property to false
                        item.setBought(false);
                        if (dataBase.updateListItem(item)) {
                            data.get(position).setBought(false);
                            resetView();
                        }
                    } else {
                        // set cheek mark drawable and set total property to true
                        item.setBought(true);
                        if (dataBase.updateListItem(item)) {
                            data.get(position).setBought(true);
                            resetView();

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
                            if (dataBase.deleteListItem(item)) {
                                data.remove(position);
                                resetView();
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
        }
        return view;
    }
    //MEthod to display dialog to edit item
    public void editItem(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflat = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
        View myView = inflat.inflate(R.layout.dialog_add_item, null);
        builder.setView(myView);
        TextView itemName = (TextView) myView.findViewById(R.id.itemName);
        TextView heading = myView.findViewById(R.id.heading);
        EditText itemNameEdit = myView.findViewById(R.id.itemNameEdit);
        final EditText quantity = (EditText) myView.findViewById(R.id.itemQty);
        heading.setText("Edit Item");
        itemNameEdit.setVisibility(View.GONE);
        itemName.setText(data.get(position).getName());
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {           //Yes
                if(!quantity.getText().toString().isEmpty()){
                    int amount = Integer.parseInt(quantity.getText().toString());
                    data.get(position).setQuantity(amount);
                    if (dataBase.updateListItem(data.get(position))) {
                        resetView();
                        Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
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
}
