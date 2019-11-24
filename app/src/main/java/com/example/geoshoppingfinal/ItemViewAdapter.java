package com.example.geoshoppingfinal;

import android.content.Context;
import android.content.DialogInterface;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by Luke Shaw 17072613
 */
//Method for handling list view for displaying items
public class ItemViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Item> data;
    public AddItem addItem;                                     //Link shop interface
    private String searchText;
    private DataBase dataBase;

    // View Type for Separators
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 0;
    // View Type for Regular rows
    private static final int ITEM_VIEW_TYPE_REGULAR = 1;
    // View Type for Regular rows
    private static final int ITEM_VIEW_TYPE_ADD = 2;
    // Types of Views that need to be handled
    // -- Separators and Regular rows --
    private static final int ITEM_VIEW_TYPE_COUNT = 3;

    public interface AddItem{                                     //Interface for sending the position and id of the shopping list back to the main activity
        void sendItem(int quantity, int id, String name);
        void deleteItem();
    }

    public ItemViewAdapter(Context context, ArrayList<Item> list, String searchText, AddItem addItem) {
        this.context = context;
        this.data = list;
        this.searchText = searchText;
        this.dataBase = new DataBase(context);
        this.addItem = addItem;
    }

    @Override //Return array list size
    public int getCount() {
        return data.size();
    }

    @Override       //Get item
    public Item getItem(int position) {
        return data.get(position);
    }

    @Override       //Get item id
    public long getItemId(int position) {
        return position;
    }

    //Method for adding an item to the list view
    public void add(Item item){
        data.add(item);
        notifyDataSetChanged();
    }
    //Method for deleting an item in the list view
    public void delete(int position){
        data.remove(position);
        notifyDataSetChanged();
    }

 /*   @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }*/

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        boolean isSection = data.get(position).isSeparator();
        boolean isAdd = data.get(position).isAddItem();

        if (isSection) {
            return ITEM_VIEW_TYPE_SEPARATOR;
        }
        else if(isAdd){
            return ITEM_VIEW_TYPE_ADD;
        }
        else {
            return ITEM_VIEW_TYPE_REGULAR;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View view;
        final LayoutInflater inflat = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;

        final Item item = data.get(position);
        int itemViewType = getItemViewType(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (itemViewType == ITEM_VIEW_TYPE_SEPARATOR) {
                // If its a section ?
                view = inflater.inflate(R.layout.seperator_item_list, null);
            }
            else if(itemViewType == ITEM_VIEW_TYPE_ADD){
                view = inflater.inflate(R.layout.add_item_list, null);
            }
            else {
                // Regular row
                view = inflater.inflate(R.layout.shopping_item_list, null);
            }
        }
        else {
            view = convertView;
        }


        if (itemViewType == ITEM_VIEW_TYPE_SEPARATOR) {
            // If separator

            TextView separatorView = (TextView) view.findViewById(R.id.separator);
            separatorView.setText(item.getName());
        }
        else if(itemViewType == ITEM_VIEW_TYPE_ADD){
            TextView addView = (TextView) view.findViewById(R.id.addItem);
            addView.setText(item.getName());
            addView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {                       //Delete image click listener that displays a dialog to delete an item
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    final View myView = inflat.inflate(R.layout.dialog_add_item, null);
                    builder.setView(myView);
                    final TextView itemName = (TextView) myView.findViewById(R.id.itemName);
                    EditText itemNameEdit = myView.findViewById(R.id.itemNameEdit);
                    final EditText quantity = (EditText) myView.findViewById(R.id.itemQty);

                    itemNameEdit.setVisibility(View.GONE);

                    itemName.setText(searchText);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {           //Yes
                            if(!quantity.getText().toString().isEmpty()){
                                if(!dataBase.checkItemExist(formatText(searchText))) {
                                    int amount = Integer.parseInt(quantity.getText().toString());
                                    int itemID = dataBase.saveItem(new Item(formatText(searchText)));
                                    if (itemID > 0) {
                                        addItem.sendItem(amount, itemID, formatText(searchText));
                                        dialog.dismiss();
                                    }
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
            });
        }
        else {
            // If regular

            // Set contact name and number
            TextView linkedShopList = view.findViewById(R.id.linkedList);
            ImageView deleteImage = (ImageView) view.findViewById(R.id.delete_button);
            linkedShopList.setText(dataBase.getLinkedItemListNames(item.getItemID()));
            if(!linkedShopList.getText().toString().isEmpty()) {
                linkedShopList.setVisibility(View.VISIBLE);
                deleteImage.setPaddingRelative(0, 24, 0 , 0);
            }
            TextView itemNameView = (TextView) view.findViewById(R.id.nameLabel);
            itemNameView.setText( item.getName() );

            itemNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {                       //Delete image click listener that displays a dialog to delete an item
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    final View myView = inflat.inflate(R.layout.dialog_add_item, null);
                    builder.setView(myView);
                    final TextView itemName = (TextView) myView.findViewById(R.id.itemName);
                    EditText itemNameEdit = myView.findViewById(R.id.itemNameEdit);
                    final EditText quantity = (EditText) myView.findViewById(R.id.itemQty);

                    itemNameEdit.setVisibility(View.GONE);

                    itemName.setText(item.getName());

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {           //Yes
                            if(!quantity.getText().toString().isEmpty()){
                                int amount = Integer.parseInt(quantity.getText().toString());
                                addItem.sendItem(amount, item.getItemID(), item.getName());
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

            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {                       //Delete image click listener that displays a dialog to delete an item
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Are you sure you want to delete?");

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {           //Yes
                            if (dataBase.deleteItem(item)) {
                                dataBase.deleteLinkedItemToList(item.getItemID());
                                dataBase.deleteHistoryItemID(item.getItemID());
                                //addItem = (AddItem)  parent.getContext();
                                delete(position);
                                addItem.deleteItem();
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

    public String formatText(String name){
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
