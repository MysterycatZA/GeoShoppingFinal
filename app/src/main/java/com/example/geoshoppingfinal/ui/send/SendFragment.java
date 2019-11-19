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

import com.example.geoshoppingfinal.AddItemActivity;
import com.example.geoshoppingfinal.DataBase;
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
    private static final int REQUEST_CODE_ITEM = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBase = new DataBase(getContext());
        shopID = getArguments().getInt("shopID");
        String title = dataBase.getShopListName(shopID);
        mainViewModel =
                ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        fragmentTitle = title + " List";
        mainViewModel.addTitle(fragmentTitle);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send, container, false);
        listView = (ListView) root.findViewById(R.id.itemListView);
        listView.setEmptyView(root.findViewById(R.id.emptyElement));
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });
        loadData(0, shopID);
        SendViewModel model = ViewModelProviders.of(getActivity()).get(SendViewModel.class);

        ((MainActivity)getActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(0, shopID);
                // Refresh Your Fragment
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if(requestCode == REQUEST_CODE_ITEM){
            loadData(0, shopID);
            if(resultCode == Activity.RESULT_OK){
                int lastLocationID = dataBase.getShopList(shopID).getLastLocationID();
                if (list.size() == 1 && lastLocationID != 0 && !dataBase.checkListIsGeofenced(shopID) && (dataBase.getLocation(lastLocationID).getLocationID() != -1)) {
                    autoGeofenceHistory(dataBase.getLocation(lastLocationID));
                }
            }
        }
    }

    public void autoGeofenceHistory(final Location location){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Do you want to Geofence this previous Location?");
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
    public void addItem(){
        Intent intent = new Intent(getActivity(), AddItemActivity.class);
        intent.putExtra("shopListID", shopID);
        startActivityForResult(intent, REQUEST_CODE_ITEM);
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