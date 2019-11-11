package com.example.geoshoppingfinal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements ShoppingListViewAdapter.LinkShops,
        LocationListViewAdapter.GeoFenceInterface {
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private DrawerLayout drawer;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private ArrayList<Geofence> geofences;
    private MainViewModel mainViewModel;
    //Constants
    private static final float GEOFENCE_RADIUS = 250.0f; // in meters
    private static final String CHANNEL_ID = "Geofence";
    private static final int REQUEST_CODE_PLACE = 1;
    private static final int REQUEST_CODE_ITEM = 2;

    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    private FragmentRefreshListener fragmentRefreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        geofences = new ArrayList<>();
        createNotificationChannel();
        geofencingClient = LocationServices.getGeofencingClient(this);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
/*        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.nav_gallery){
                    showPlacePicker();
                }

                boolean result = NavigationUI.onNavDestinationSelected(menuItem, navController);
                drawer.closeDrawers();
                return result;
            }
        });*/
        final FloatingActionButton fab = this.findViewById(R.id.fab);
/*        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });*/
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.nav_home || destination.getId() == R.id.nav_send || destination.getId() == R.id.nav_slideshow) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.GONE);
                }
            }
        });
        //checkForNotification();
        DataBase dataBase = new DataBase(this);
        dataBase.setupItem();

        //removeGeofences();
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                getSupportActionBar().setTitle(s);
            }
        });
        String notification = getIntent().getStringExtra("notification");
        if(notification != null){
            int shopID = Integer.parseInt(getIntent().getStringExtra("shopID"));
            Location location = dataBase.getLocation(shopID);
            int shopListID = location.getShoppingListID();
            location.setGeofenced(false);
            location.setShoppingListID(-1);
            if(dataBase.updateLocation(location)) {
                removeGeofenceData(shopID);
                Bundle bundle = new Bundle();
                bundle.putInt("shopID", shopListID);
                navController.navigate(R.id.nav_send, bundle);
            }
        }
    }

    //Method that opens link shop activity from the card adapter
    public void sendLinkShops(String name, int id){
        Bundle bundle = new Bundle();
        bundle.putString("shopListName", name);
        bundle.putInt("shopListID", id);
        navController.navigate(R.id.nav_slideshow, bundle);
    }

    public void createGeofenceData(LatLng latLng, int id){
        createGeofence(latLng, id + "");
        addGeofence();
    }
    public void removeGeofenceData(int id){
        List<String> geofenceList = Arrays.asList(id + "");
        removeGeofence(geofenceList);
    }

    public void openShopListFragment(View view, int shopID){
        Bundle bundle = new Bundle();
        bundle.putInt("shopID", shopID);
        Navigation.findNavController(view).navigate(R.id.nav_send, bundle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            String data = intent.getStringExtra("notification");
            if (data != null) {
                DataBase dataBase = new DataBase(getApplicationContext());
                int shopID = Integer.parseInt(intent.getStringExtra("shopID"));
                Location location = dataBase.getLocation(shopID);
                int shopListID = location.getShoppingListID();
                location.setGeofenced(false);
                location.setShoppingListID(-1);
                if(dataBase.updateLocation(location)) {
                    removeGeofenceData(shopID);
                    Bundle bundle = new Bundle();
                    bundle.putInt("shopID", shopListID);
                    navController.navigate(R.id.nav_send, bundle);
                }
            }
        }
    }

    public interface FragmentRefreshListener{
        void onRefresh();
    }

/*    @Override   //On resume method that holds a click listener that opens item list of specific shopping list when card is clicked
    protected void onResume() {
        super.onResume();
        ((ShoppingListViewAdapter) mAdapter).setOnItemClickListener(new ShoppingListViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Fragment fragment = new SendFragment();
                Bundle args = new Bundle();
                args.putInt(KEY_PRODUCT_ID, productId);
                fragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(ProductDetailsFragment.TAG)
                        .replace(R.id.fragment_container, fragment, ProductDetailsFragment.TAG)
                        .commit();
            }
        });
    }*/

    //Method that opens link shop activity from the card adapter
    public void addItem(){
        Intent intent = new Intent(this, AddItemActivity.class);
        //intent.putExtra("shopListID", id);
        startActivityForResult(intent, REQUEST_CODE_ITEM);
    }

/*    private void checkForNotification(){
        String intent = getIntent().getStringExtra("geofenceID");
        if(intent != null){
            removeGeofence();
        }
    }*/

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

/*    private void showPlacePicker() {
        PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
        builder.setAndroidApiKey(getString(R.string.ANDROID_API_KEY))
                .setMapsApiKey(getString(R.string.MAPS_API_KEY));

        // If you want to set a initial location rather then the current device location.
        // NOTE: enable_nearby_search MUST be true.
        // builder.setLatLng(new LatLng(37.4219999, -122.0862462))

        try {
            Intent placeIntent = builder.build(this);
            startActivityForResult(placeIntent, REQUEST_CODE_PLACE);
        }
        catch (Exception ex) {
            Toast.makeText(this, "Google Play services is not available...", Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*        if ((requestCode == REQUEST_CODE_PLACE) && (resultCode == RESULT_OK)) {
            Place place = PingPlacePicker.getPlace(data);
            if (place != null) {
                DataBase dataBase = new DataBase(this);
                Location location = new Location(place.getName(), place.getLatLng().latitude, place.getLatLng().longitude);
                if(!dataBase.checkLocationExist(location)) {
                    int id = dataBase.saveLocation(location);
                    if (id != 0) {
                        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
                        GeoFenceInterface(place.getLatLng(), id + "");
                        addGeofence();
                    } else {
                        Toast.makeText(this, "Not Saved", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(this, "Place already geofenced!", Toast.LENGTH_SHORT).show();
                }
            }
        }*/
/*        else if((requestCode == REQUEST_CODE_ITEM) && (resultCode == RESULT_OK)){
            ItemList item = new ItemList(data.getIntExtra("quantity", -1), data.getIntExtra("id", -1), data.getIntExtra("shopID", -1));
            DataBase db = new DataBase(this);
            int values[] = db.checkItemListExist(item.getItemID(), item.getShoppingListID());
            if(values[0] == 0){
                if(db.saveListItem(item)){
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                    if(getFragmentRefreshListener()!= null){
                        getFragmentRefreshListener().onRefresh();
                    }
                }
                else {
                    Toast.makeText(this, "Not Saved", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                item.setItemListID(values[0]);
                item.setQuantity(item.getQuantity() + values[1]);
                if (db.updateListItem(item)) {
                    Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
                    if(getFragmentRefreshListener()!= null){
                        getFragmentRefreshListener().onRefresh();
                    }
                }
            }
        }*/
    }

    public void createGeofence(LatLng latLng, String id){
        geofences.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(id)

                .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    public void removeGeofences(){
        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Geofences removed", Toast.LENGTH_SHORT).show();
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to remove geofences", Toast.LENGTH_SHORT).show();
                        // Failed to remove geofences
                        // ...
                    }
                });
    }

    public void removeGeofence(List<String> geofences){
        geofencingClient.removeGeofences(geofences)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Geofences removed", Toast.LENGTH_SHORT).show();
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to remove geofences", Toast.LENGTH_SHORT).show();
                        // Failed to remove geofences
                        // ...
                    }
                });
    }

    public void addGeofence(){
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Geofence added!", Toast.LENGTH_SHORT).show();
                        // Geofences added
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to add geofence!", Toast.LENGTH_SHORT).show();
                        // Failed to add geofences
                        // ...
                    }
                });
    }



    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items  to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sort, menu);
        return true;
    }*/

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
