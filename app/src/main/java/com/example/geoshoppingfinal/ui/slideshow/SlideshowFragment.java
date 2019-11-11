package com.example.geoshoppingfinal.ui.slideshow;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.geoshoppingfinal.DataBase;
import com.example.geoshoppingfinal.Location;
import com.example.geoshoppingfinal.LocationListViewAdapter;
import com.example.geoshoppingfinal.MainActivity;
import com.example.geoshoppingfinal.MainViewModel;
import com.example.geoshoppingfinal.R;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rtchagas.pingplacepicker.PingPlacePicker;

import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

public class SlideshowFragment extends Fragment {

    private LocationListViewAdapter adapter;
    private ListView listView;
    private ArrayList<Location> list;
    private ArrayList<Location> searchList;
    private SlideshowViewModel slideshowViewModel;
    private int shopID;
    private String shopName;
    private MainViewModel mainViewModel;
    private static final int REQUEST_CODE_PLACE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
/*        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel.class);*/
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
/*        final TextView textView = root.findViewById(R.id.text_slideshow);
        slideshowViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlacePicker();
            }
        });
        listView = (ListView) root.findViewById(R.id.locationListView);
        loadData(false);
        return root;
    }

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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText != null && !newText.isEmpty()){                                  //If the search text is not null or empty
                    searchList = new ArrayList<Location>();                                    //Search array list
                    for(int i = 0; i < list.size(); i++){               //Looping through every anime object in the array list
                        if(list.get(i).getName().toLowerCase().contains(newText)){    //If the anime name contains the word of search text, add it to the search array list
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE_PLACE) && (resultCode == Activity.RESULT_OK)) {
            Place place = PingPlacePicker.getPlace(data);
            if (place != null) {
                DataBase dataBase = new DataBase(getContext());
                Location location = new Location(place.getName(), place.getLatLng().latitude, place.getLatLng().longitude);
                if(!dataBase.checkLocationExist(location)) {
                    //location.setShoppingListID(shopID);
                    int id = dataBase.saveLocation(location);
                    if (id != 0) {
                        Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                        loadData(false);
/*                        ((MainActivity)getActivity()).GeoFenceInterface(place.getLatLng(), id + "");
                        ((MainActivity)getActivity()).addGeofence();*/
                    } else {
                        Toast.makeText(getContext(), "Not Saved", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), "Place already geofenced!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public boolean checkLinked(ArrayList<Location> locations){
        boolean found = false;
        int index = 0;
        while (!found && index < locations.size()){
            if(locations.get(index).getShoppingListID() == shopID && locations.get(index).isGeofenced()){
                found = true;
            }
            index++;
        }
        return found;
    }

    private void loadData(boolean search) {

        if(search){
            list = searchList;
        }
        else {
            DataBase dataBase = new DataBase(getActivity());
            list = dataBase.retrieveLocations();
        }
        adapter = new LocationListViewAdapter(getActivity(), list, shopID, checkLinked(list));             //List view displaying items
        listView.setAdapter(adapter);
    }
}