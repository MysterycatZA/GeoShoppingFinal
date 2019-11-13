package com.example.geoshoppingfinal;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;

public class LocationListViewAdapter extends BaseAdapter {
    //Declaration and Initialisation
    private ArrayList<Location> data;                       //Array list of items
    private Context context;                            //Context of passed activity
    private static LayoutInflater inflater = null;         //Layout inflater
    public GeoFenceInterface geoFenceInterface;                                     //Link shop interface
    private int shopID;

    public interface GeoFenceInterface {                                     //Interface for sending the position and id of the shopping list back to the main activity
        void createGeofenceData(LatLng latLng, int id);
        void removeGeofenceData(int id);
    }

    //Constructor
    public LocationListViewAdapter(Context context, ArrayList<Location> data, int shopID) {
        this.context = context;
        this.data = data;
        this.shopID = shopID;
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
    public void add(Location item){
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
            view = inflater.inflate(R.layout.location_list, null);
        }
        final CheckedTextView simpleCheckedTextView = (CheckedTextView) view.findViewById(R.id.nameLabel);
        final ImageView deleteImage = (ImageView) view.findViewById(R.id.delete_button);
        final TextView shopName = view.findViewById(R.id.shopNameLabel);
        // Get the data item
        final DataBase dataBase = new DataBase(context);
        final Location location = data.get(position);
        String shopNameText = "";
        if(location.isGeofenced()){
            simpleCheckedTextView.setCheckMarkDrawable(R.drawable.btn_check_on_holo);
            simpleCheckedTextView.setChecked(true);
            shopNameText = dataBase.getShopListName(location.getShoppingListID());
        }
        else {
            simpleCheckedTextView.setCheckMarkDrawable(R.drawable.btn_check_off_holo);
            simpleCheckedTextView.setChecked(false);
        }
        shopName.setText(shopNameText);
        //Toast.makeText(this, "You selected the place: " + place.getName(), Toast.LENGTH_SHORT).show();
        // Display the data item's properties
        simpleCheckedTextView.setText(location.getName());
        // perform on Click Event Listener on CheckedTextView
        simpleCheckedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (simpleCheckedTextView.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Are you sure you want to remove from Geofence?");

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {           //Yes
                            removeGeofence(location, position);
                            dialog.dismiss();
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
                } else {
                    if(shopID != 0){
                        if(!data.get(position).isGeofenced()) {
                            if(!dataBase.checkListIsGeofenced(location.getShoppingListID())) {
                                addGeofence(shopID, location, position);
                            }
                            else {
                                showError("Only 1 shop allowed to be geofenced at a time");
                            }
                        }
                        else {
                            showError("Location already linked to another Shopping list");
                        }
                    }
                    else{
                        showError("No shopping list to link");
                    }
                }
            }
        });

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                       //Delete image click listener that displays a dialog to delete a location
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Are you sure you want to delete?");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {           //Yes
                        if(removeLocation(location)) {
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



    public void addGeofence(int shopID, Location location, int position){
        DataBase dataBase = new DataBase(context);
        location.setGeofenced(true);
        location.setShoppingListID(shopID);
        ShoppingList shoppingList = dataBase.getShopList(shopID);
        shoppingList.setLastLocationID(location.getLocationID());
        dataBase.updateShopList(shoppingList);
        if (dataBase.updateLocation(location)) {
            data.get(position).setGeofenced(true);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            geoFenceInterface  = (GeoFenceInterface) context;
            geoFenceInterface.createGeofenceData(latLng, location.getLocationID());
            notifyDataSetChanged();
        }
    }

    public boolean removeLocation(Location location){
        DataBase dataBase = new DataBase(context);
        if(dataBase.deleteLocation(location)){
            geoFenceInterface  = (GeoFenceInterface) context;
            geoFenceInterface.removeGeofenceData(location.getLocationID());
            return true;
        }

        return false;
    }

    public void removeGeofence(Location location, int position){
        DataBase dataBase = new DataBase(context);
        location.setGeofenced(false);
        //location.setShoppingListID(-1);
        if (dataBase.updateLocation(location)) {
            data.get(position).setGeofenced(false);
            geoFenceInterface  = (GeoFenceInterface) context;
            geoFenceInterface.removeGeofenceData(location.getLocationID());
            notifyDataSetChanged();
        }
    }

    public void showError(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context); //Alerting user that the location is already linked

        builder.setMessage(message)
                .setTitle("Error");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}