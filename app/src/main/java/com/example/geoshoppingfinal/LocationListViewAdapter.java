package com.example.geoshoppingfinal;

import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private int chosenShopID;

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
        // Display the data item's properties
        simpleCheckedTextView.setText(location.getName());
        // perform on Click Event Listener on CheckedTextView
        simpleCheckedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLocationEnabled(context)) {
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
                        if(!data.get(position).isGeofenced()) {
                            if  (shopID != 0) {
                                if (!dataBase.checkListIsGeofenced(shopID)) {
                                    dataBase.saveHistory(shopID, location.getLocationID());
                                    addGeofence(shopID, location, position);
                                } else {
                                    showError("Only 1 shop allowed to be geofenced to a shopping list at a time");
                                }
                            } else {
                                loadOpenShopList(location, position);
                            }
                        } else {
                            showError("Location already linked to another Shopping list");
                        }
                    }
                }
                else{
                    Toast.makeText(context, "Location services are disabled. Please enable location services.", Toast.LENGTH_LONG).show();
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

    public void loadOpenShopList(Location location, int position){
        DataBase dataBase = new DataBase(context);
        ArrayList<ShoppingList> shoppingListArrayList = dataBase.retrieveShopList();
        ArrayList<ShoppingList> filteredList = new ArrayList<>();
        filteredList.clear();

        for (ShoppingList shopList:shoppingListArrayList) {
            if(!dataBase.checkListIsGeofenced(shopList.getShoppingListID())){
                filteredList.add(shopList);
            }
        }

        if(filteredList.size() > 0) {
            displaySuggested(filteredList, location, position);
        }
        else{
            showError("No shopping list to link");
        }
    }

    public void displaySuggested(final ArrayList<ShoppingList> itemArrayList, final Location location, final int position){

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Shopping list to link");

        // add a checkbox list
        String[] items = new String[itemArrayList.size()];
        for (int i = 0; i < items.length; i++){
            items[i] = itemArrayList.get(i).getName();
        }

        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedShopList(which);
                // user checked an item
            }
        });

        // add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                geofenceShopList(itemArrayList, location, position);
                // user clicked OK

            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void selectedShopList(int which){
        chosenShopID = which;
    }

    public void geofenceShopList(ArrayList<ShoppingList> shoppingListArrayList, Location location, int position){
        DataBase dataBase = new DataBase(context);
        dataBase.saveHistory(shoppingListArrayList.get(chosenShopID).getShoppingListID(), location.getLocationID());
        addGeofence(shoppingListArrayList.get(chosenShopID).getShoppingListID(), location, position);
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
            dataBase.deleteHistoryLocationID(location.getLocationID());
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

    public static Boolean isLocationEnabled(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
// This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
// This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }
}