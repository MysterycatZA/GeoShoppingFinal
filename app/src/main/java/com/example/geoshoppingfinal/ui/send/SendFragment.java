package com.example.geoshoppingfinal.ui.send;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.geoshoppingfinal.DataBase;
import com.example.geoshoppingfinal.Item;
import com.example.geoshoppingfinal.ItemList;
import com.example.geoshoppingfinal.ItemListViewAdapter;
import com.example.geoshoppingfinal.Location;
import com.example.geoshoppingfinal.MainActivity;
import com.example.geoshoppingfinal.MainViewModel;
import com.example.geoshoppingfinal.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
/**
 * Created by Luke Shaw 17072613
 */
//Fragment for the shopping list item list
public class SendFragment extends Fragment {
    //Declaration and Initialisation
    private ItemListViewAdapter adapter;                //Adapter
    private ListView listView;                          //List view
    private ArrayList<ItemList> list;                   //Array of item list
    private int shopID;                                 //Shopping list id
    private String fragmentTitle;                       //Title
    private MainViewModel mainViewModel;                //View model
    private DataBase dataBase;                          //Database
    private ArrayList<Item> itemToAddSuggestedList;     //Suggested items to add array

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBase = new DataBase(getContext());
        shopID = getArguments().getInt("shopID");
        String title = dataBase.getShopListName(shopID);
        mainViewModel =
                ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        fragmentTitle = title + " list";
        mainViewModel.addTitle(fragmentTitle);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_send, container, false);
        listView = (ListView) root.findViewById(R.id.itemListView);
        listView.setEmptyView(root.findViewById(R.id.emptyElement));
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem(root);
            }
        });
        list = dataBase.retrieveListItems(0, shopID);
        if(list.size() > 0){
            loadData(0, shopID);
        }
        else {
            loadSuggested();
        }
        return root;
    }
    //Loading suggested items
    public void loadSuggested(){
        ArrayList<Item> itemArrayList = dataBase.getSuggestedItems(shopID);
        if(itemArrayList.size() > 0) {
            itemToAddSuggestedList = new ArrayList<>();
            itemToAddSuggestedList.clear();
            displaySuggested(itemArrayList);
        }
    }
    //Displaying suggested items in a dialog based off https://stackoverflow.com/questions/15762905/how-can-i-display-a-list-view-in-an-android-alert-dialog
    public void displaySuggested(final ArrayList<Item> itemArrayList){

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Suggested Items");

        // add a checkbox list
        String[] items = new String[itemArrayList.size()];
        for (int i = 0; i < items.length; i++){
            items[i] = itemArrayList.get(i).getName();
        }
        builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // user checked or unchecked a box
                suggestedItemChecked(which, isChecked, itemArrayList);
            }
        });

        // add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addSuggested();
                // user clicked OK

            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //Method to add item to suggested item array when item is selected
    public void suggestedItemChecked(int which, boolean isChecked, ArrayList<Item> itemArrayList){
        if(isChecked) {
            itemToAddSuggestedList.add(itemArrayList.get(which));
        }
        else{
            itemToAddSuggestedList.remove(which);
        }
    }
    //Method to add suggested items to shopping list
    public void addSuggested(){
        for (Item item: itemToAddSuggestedList) {
            ItemList itemList = new ItemList(1, item.getItemID(), shopID);
            dataBase.saveListItem(itemList);
        }
        if(itemToAddSuggestedList.size() > 0){
            int lastLocationID = dataBase.getShopList(shopID).getLastLocationID();
            if (lastLocationID != 0 && !dataBase.checkListIsGeofenced(shopID) && (dataBase.getLocation(lastLocationID).getLocationID() != -1)) {
                autoGeofenceHistory(dataBase.getLocation(lastLocationID));
            }
        }
        loadData(0, shopID);
    }

    public void autoGeofenceHistory(final Location location){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Do you want to Geofence to this previous Location?");
        builder.setMessage(location.getName());
        // Add the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(location.isGeofenced()){
                    showError("Location already geofenced!", getContext());
                }
                else{
                    location.setGeofenced(true);
                    location.setShoppingListID(shopID);
                    if (dataBase.updateLocation(location)) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        ((MainActivity) getActivity()).createGeofence(latLng, location.getLocationID() + "");
                        ((MainActivity) getActivity()).addGeofence(location.getLocationID());
                        loadData(0,shopID);
                    }
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //Method to display error message dialog
    public void showError(String message, Context context){
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

    //Method that adds item
    public void addItem(View root){
        ((MainActivity) getActivity()).openAddItemFragment(root, shopID);
    }
    //Method to create options menu for asc/desc
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sort, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    //Method for handling the ordering of items asc/desc
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_asc:
                loadData(1, shopID);
                return true;
            case R.id.action_desc:
                loadData(2, shopID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //Method to load array to adapter to be displayed
    private void loadData(int sort, int shopID) {
        list = dataBase.retrieveListItems(sort, shopID);
        adapter = new ItemListViewAdapter(getActivity(), list);             //List view displaying items
        listView.setAdapter(adapter);
    }
}