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

public class SendFragment extends Fragment {

    private ItemListViewAdapter adapter;
    private ListView listView;
    private ArrayList<ItemList> list;
    private int shopID;
    private String fragmentTitle;
    private MainViewModel mainViewModel;
    private DataBase dataBase;
    private ArrayList<Item> itemToAddSuggestedList;
    private static final int REQUEST_CODE_ITEM = 2;

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
/*        SendViewModel model = ViewModelProviders.of(getActivity()).get(SendViewModel.class);

        ((MainActivity)getActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(0, shopID);
                // Refresh Your Fragment
            }
        });*/
        return root;
    }

    public void loadSuggested(){
        ArrayList<Item> itemArrayList = dataBase.getSuggestedItems(shopID);
        if(itemArrayList.size() > 0) {
            itemToAddSuggestedList = new ArrayList<>();
            itemToAddSuggestedList.clear();
            displaySuggested(itemArrayList);
        }
    }

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

    public void suggestedItemChecked(int which, boolean isChecked, ArrayList<Item> itemArrayList){
        if(isChecked) {
            itemToAddSuggestedList.add(itemArrayList.get(which));
        }
        else{
            itemToAddSuggestedList.remove(which);
        }
    }

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



/*    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if(requestCode == REQUEST_CODE_ITEM){
            loadData(0, shopID);
            if(resultCode == Activity.RESULT_OK){
                if(list.size() == 1) {
                    int lastLocationID = dataBase.getShopList(shopID).getLastLocationID();
                    if (lastLocationID != 0) {
                        if (!dataBase.checkListIsGeofenced(shopID) && (dataBase.getLocation(lastLocationID).getLocationID() != -1)) {
                            autoGeofenceHistory(dataBase.getLocation(lastLocationID));
                        }
                    } else {
                        if (!dataBase.checkListIsGeofenced(shopID)) {
                            Location location = dataBase.getLocation(dataBase.getItemLocationHistory(data.getIntExtra("itemID", 0)));
                            if(location.getLocationID() != -1){
                                autoGeofenceHistory(location);
                            }
                        }
                    }
                }
            }
        }
    }*/

    public void checkLastLocation(int itemID){
        if(list.size() == 1) {
            int lastLocationID = dataBase.getShopList(shopID).getLastLocationID();
            if (lastLocationID != 0) {
                if (!dataBase.checkListIsGeofenced(shopID) && (dataBase.getLocation(lastLocationID).getLocationID() != -1)) {
                    autoGeofenceHistory(dataBase.getLocation(lastLocationID));
                }
            } else {
                if (!dataBase.checkListIsGeofenced(shopID)) {
                    Location location = dataBase.getLocation(dataBase.getItemLocationHistory(itemID));
                    if(location.getLocationID() != -1){
                        autoGeofenceHistory(location);
                    }
                }
            }
        }
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

    //Method that opens link shop activity from the card adapter
    public void addItem(View root){
        ((MainActivity) getActivity()).openAddItemFragment(root, shopID);
/*        Intent intent = new Intent(getActivity(), AddItemActivity.class);
        intent.putExtra("shopListID", shopID);
        startActivityForResult(intent, REQUEST_CODE_ITEM);*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sort, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

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

    private void loadData(int sort, int shopID) {
        list = dataBase.retrieveListItems(sort, shopID);
        adapter = new ItemListViewAdapter(getActivity(), list);             //List view displaying items
        listView.setAdapter(adapter);
    }
}