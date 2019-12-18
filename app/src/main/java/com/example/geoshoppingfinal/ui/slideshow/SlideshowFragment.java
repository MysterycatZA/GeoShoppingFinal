package com.example.geoshoppingfinal.ui.slideshow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.geoshoppingfinal.DataBase;
import com.example.geoshoppingfinal.Location;
import com.example.geoshoppingfinal.LocationListViewAdapter;
import com.example.geoshoppingfinal.MainActivity;
import com.example.geoshoppingfinal.MainViewModel;
import com.example.geoshoppingfinal.R;
import com.example.geoshoppingfinal.ShoppingList;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rtchagas.pingplacepicker.PingPlacePicker;

import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
/**
 * Created by Luke Shaw 17072613
 */
//Fragment for locations
public class SlideshowFragment extends Fragment {
    //Declaration and Initialisation
    private LocationListViewAdapter adapter;                //Adapter
    private ListView listView;                              //Listview
    private ArrayList<Location> list;                       //Location array
    private ArrayList<Location> searchList;                 //Search location array
    private int shopID;                                     //Shop id
    private String shopName;                                //Shop name
    private MainViewModel mainViewModel;                    //Viewmodel
    private ProgressDialog progressDialog;                  //Progress dialog
    private DataBase dataBase;                              //Database
    private static final int REQUEST_CODE_PLACE = 1;        //Request code for PING

    @Override   //Method for creating fragment/ Code initialises progress dialog based off http://www.41post.com/4588/programming/android-coding-a-loading-screen-part-1
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Loading place picker, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setInverseBackgroundForced(false);
        if(getArguments() != null && getArguments().getInt("shopListID", -1) != -1){
            shopID = getArguments().getInt("shopListID");
            shopName = getArguments().getString("shopListName");
        }
        else {
            shopID = 0;
        }
        mainViewModel =
                ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        String title = "Locations ";
        if(shopName != null){
            title += shopName;
        }
        mainViewModel.addTitle(title);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
        dataBase = new DataBase(getContext());
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);                    //Fab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLocationEnabled(getContext())) {
                    progressDialog.show();
                    showPlacePicker();
                }
                else{
                    Toast.makeText(getActivity(), "Location services are disabled. Please enable location services.", Toast.LENGTH_LONG).show();
                }
            }
        });
        listView = (ListView) root.findViewById(R.id.locationListView);
        listView.setEmptyView(root.findViewById(R.id.emptyElement));
        loadData(false);
        return root;
    }
    //Code to check if location services are enabled based off https://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
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
    //Method for PING place picker. Code based off tutorial from PING github https://github.com/rtchagas/pingplacepicker
    private void showPlacePicker() {
        PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
        builder.setAndroidApiKey(getString(R.string.ANDROID_API_KEY))
                .setMapsApiKey(getString(R.string.MAPS_API_KEY));

        // If you want to set a initial location rather then the current device location.
        // NOTE: enable_nearby_search MUST be true.
        // builder.setLatLng(new LatLng(37.4219999, -122.0862462))

        try {
            Intent placeIntent = builder.build(getActivity());
            startActivityForResult(placeIntent, REQUEST_CODE_PLACE);
        }
        catch (Exception ex) {
            Toast.makeText(getActivity(), "Google Play services is not available...", Toast.LENGTH_SHORT).show();
        }
    }
    //Method for handling the search view based off https://developer.android.com/training/search/setup
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint("Search location name here");
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText != null && !newText.isEmpty()){                                  //If the search text is not null or empty
                    searchList = new ArrayList<Location>();                                    //Search array list
                    for(int i = 0; i < list.size(); i++){               //Looping through every location object in the array list
                        if(list.get(i).getName().toLowerCase().contains(newText)){    //If the location name contains the word of search text, add it to the search array list
                            searchList.add(list.get(i));
                        }
                    }
                    loadData(true);                                           //Repopulate list with search array list
                }
                else{
                    loadData(false);                                           //If there is no text in the search view then reset view
                }
                return true;
            }
        });
    }

    @Override   //On resume method
    public void onResume() {
        super.onResume();
        if(progressDialog != null) {
            progressDialog.hide();
        }
    }
    //Method to get results back from PING place picker
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE_PLACE) && (resultCode == Activity.RESULT_OK)) {
            Place place = PingPlacePicker.getPlace(data);
            if (place != null) {
                Location location = new Location(place.getName(), place.getLatLng().latitude, place.getLatLng().longitude);
                if(!dataBase.checkLocationExist(location)) {
                    int id = dataBase.saveLocation(location);
                    if (id != 0) {
                        if(shopID > 0) {
                            geofenceLocation(place.getLatLng(), id);
                        }
                        loadData(false);

                    } else {
                        Toast.makeText(getContext(), "Not Saved", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), "Place already added!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    //Method to geofence a location
    public void geofenceLocation(final LatLng latLng, final int locationID){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Do you want to geofence this location?");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Location location = dataBase.getLocation(locationID);
                if(dataBase.checkListIsGeofenced(shopID)){
                    showError("Shopping List already geofenced!", getContext());
                }
                else{
                    ShoppingList shoppingList = dataBase.getShopList(shopID);
                    shoppingList.setLastLocationID(location.getLocationID());
                    dataBase.updateShopList(shoppingList);
                    location.setGeofenced(true);
                    location.setShoppingListID(shopID);
                    if (dataBase.updateLocation(location)) {
                        ((MainActivity) getActivity()).createGeofence(latLng, locationID + "");
                        ((MainActivity) getActivity()).addGeofence(locationID);
                        loadData(false);
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
    //Method that displays error message dialog
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
    //Method to load data for adapter to display locations
    private void loadData(boolean search) {

        if(search){
            list = searchList;
        }
        else {
            list = dataBase.retrieveLocations();
        }
        adapter = new LocationListViewAdapter(getActivity(), list, shopID);             //List view displaying items
        listView.setAdapter(adapter);
    }
}