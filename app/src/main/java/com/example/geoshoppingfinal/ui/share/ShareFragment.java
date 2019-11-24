package com.example.geoshoppingfinal.ui.share;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geoshoppingfinal.DataBase;
import com.example.geoshoppingfinal.Item;
import com.example.geoshoppingfinal.ItemList;
import com.example.geoshoppingfinal.ItemViewAdapter;
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

public class ShareFragment extends Fragment
        implements ItemViewAdapter.AddItem{

    //private ShareViewModel shareViewModel;
    private ArrayList<Item> list;
    private ArrayList<Item> searchList;
    private int shopID;
    private DataBase dataBase;
    private MainViewModel mainViewModel;
    private String fragmentTitle;
    private ListView listView;
    private SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBase = new DataBase(getContext());
        shopID = getArguments().getInt("shopID");
        mainViewModel =
                ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        fragmentTitle = "Add Item";
        mainViewModel.addTitle(fragmentTitle);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
/*        shareViewModel =
                ViewModelProviders.of(this).get(ShareViewModel.class);*/
        View root = inflater.inflate(R.layout.fragment_share, container, false);
/*        final TextView textView = root.findViewById(R.id.text_share);
        shareViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        listView = (ListView) root.findViewById(R.id.itemView);
        listView.setEmptyView(root.findViewById(R.id.emptyElement));
        sortView(false, "");
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });
        return root;
    }

    public void addItem(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflat = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
        View myView = inflat.inflate(R.layout.dialog_add_item, null);
        builder.setView(myView);
        TextView itemName = (TextView) myView.findViewById(R.id.itemName);
        final EditText itemNameEdit = myView.findViewById(R.id.itemNameEdit);
        final EditText quantity = (EditText) myView.findViewById(R.id.itemQty);

        itemName.setVisibility(View.GONE);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {           //Yes
                if(!itemNameEdit.getText().toString().isEmpty() && !quantity.getText().toString().isEmpty()){
                    if(!dataBase.checkItemExist(itemNameEdit.getText().toString())) {
                        int amount = Integer.parseInt(quantity.getText().toString());
                        int itemID = dataBase.saveItem(new Item(formatText(itemNameEdit.getText().toString())));
                        if (itemID > 0) {
                            sendItem(amount, itemID, itemNameEdit.getText().toString());
                            dialog.dismiss();
                        }
                    }
                    else{
                        Toast.makeText(getContext(), "Item already exists!", Toast.LENGTH_LONG).show();
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

    public String formatText(String name){
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint("Search item name here");
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        //searchView.clearFocus();
        searchView.requestFocus();//.requestFocusFromTouch();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText != null && !newText.isEmpty()){                                  //If the search text is not null or empty
                    searchList = new ArrayList<Item>();                                    //Search array list
                    for(int i = 0; i < list.size(); i++){               //Looping through every anime object in the array list
                        if(!list.get(i).isSeparator() && list.get(i).getName().toLowerCase().startsWith(newText.toLowerCase())){    //If the anime name contains the word of search text, add it to the search array list
                            searchList.add(list.get(i));
                        }
                    }
                    sortView(true, newText);                                           //Repopulate list with search array list
                }
                else{
                    sortView(false, "");                                           //If there is no text in the search view then reset view
                }
                return true;
            }
        });
    }

    public void deleteItem(){
        sortView(false, "");
    }

    //Method that opens link shop activity from the card adapter
    public void sendItem(int quantity, int id, String name){
        searchView.clearFocus();
        searchView.setQuery("", true);
        searchView.setIconified(true);
        ItemList item = new ItemList(quantity, id, shopID);
        int values[] = dataBase.checkItemListExist(item.getItemID(), item.getShoppingListID());
        if(values[0] == 0){
            if(!dataBase.saveListItem(item)){
                Toast.makeText(getContext(), name + " not added!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getContext(), name + " added!", Toast.LENGTH_SHORT).show();
                if(dataBase.getItemListCount(shopID) == 1){
                    checkLastLocation(id);
                }
            }
        }
        else {
            item.setItemListID(values[0]);
            item.setQuantity(item.getQuantity() + values[1]);
            if (dataBase.updateListItem(item)) {
                Toast.makeText(getContext(), name + " updated!", Toast.LENGTH_SHORT).show();
            }
        }
        //getActivity().getFragmentManager().popBackStack();
/*        Intent intent = new Intent();
        intent.putExtra("itemID", id);
        setResult(RESULT_OK, intent);
        finish();*/
    }

/*    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
*//*        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();*//*
    }*/

    public void checkLastLocation(int itemID){
        int lastLocationID = dataBase.getShopList(shopID).getLastLocationID();
        if (lastLocationID != 0) {
            if (!dataBase.checkListIsGeofenced(shopID) && (dataBase.getLocation(lastLocationID).getLocationID() != -1)) {
                autoGeofenceHistory(dataBase.getLocation(lastLocationID), true);
            }
        } else {
            if (!dataBase.checkListIsGeofenced(shopID)) {
                Location location = dataBase.getLocation(dataBase.getItemLocationHistory(itemID));
                if(location.getLocationID() != -1){
                    autoGeofenceHistory(location, false);
                }
            }
        }
    }


    public void autoGeofenceHistory(final Location location, boolean lastLocation){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String previous = lastLocation ? " previous" : "";
        builder.setTitle("Do you want to Geofence to this" + previous + " location?");
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

    public void sortView(boolean search, String searchText){

        if(search){
            list = searchList;

            if(list.size() < 1){
                Item item = new Item("Tap here to add");
                item.setAddItem(true);
                list.add(item);
            }
        }
        else{

            // Creating our custom adapter
            list = dataBase.retrieveItemsSorted();
        }

        ItemViewAdapter adapter = new ItemViewAdapter(getActivity(), list, searchText, this);

        // Create the list view and bind the adapter
        listView.setAdapter(adapter);
    }
}