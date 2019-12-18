package com.example.geoshoppingfinal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
/**
 * Created by Luke Shaw 17072613
 */
//Main sctivity that holds all the Geofence code as well as initialisation of the app
public class MainActivity extends AppCompatActivity
    implements ShoppingListViewAdapter.LinkShops,
        ShoppingListViewAdapter.HandleGeofence,
        LocationListViewAdapter.GeoFenceInterface {
    //Declaration and initialisation
    private AppBarConfiguration mAppBarConfiguration;       //App bar configuration
    private NavController navController;                    //Nav controller
    private DrawerLayout drawer;                            //Drawer
    private GeofencingClient geofencingClient;              //Geofencing client
    private ArrayList<Geofence> geofences;                  //Array of geofences
    private MainViewModel mainViewModel;                    //Main viewmodel
    private DataBase dataBase;                              //Database
    private SharedPreferences sharedpreferences;            //Sharedpreferences
    private static final float GEOFENCE_RADIUS = 300.0f;    //Geofence radius in meters
    private static final String CHANNEL_ID = "Geofence";    //Broadcast channel for Geofences

    //On create for initialising Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);                                 //Setting app theme
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);                           //Setting up toolbar
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);                              //Setting up drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        geofences = new ArrayList<>();
        createNotificationChannel();
        geofencingClient = LocationServices.getGeofencingClient(this);  //Setting up GEofence client based off https://developer.android.com/training/location/geofencing
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_slideshow,
                R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);                 //Setting up nav controller
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {                           //Start help activity when help button in drawer is tapped
                if(menuItem.getItemId() == R.id.nav_tools){
                    Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
                    startActivity(intent);
                }

                boolean result = NavigationUI.onNavDestinationSelected(menuItem, navController);
                drawer.closeDrawers();
                return result;
            }
        });
        final FloatingActionButton fab = this.findViewById(R.id.fab);
        dataBase = new DataBase(this);
        dataBase.setupItem();                                                               //Setup item list for database
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                getSupportActionBar().setTitle(s);
            }
        });
        String notification = getIntent().getStringExtra("notification");
        if(notification != null){                                                           //Code to check for if a notification was tapped when app is booting
            int shopID = Integer.parseInt(getIntent().getStringExtra("shopID"));
            Location location = dataBase.getLocation(shopID);
            int shopListID = location.getShoppingListID();
            location.setGeofenced(false);
            if(dataBase.updateLocation(location)) {
                removeGeofenceData(shopID);
                Bundle bundle = new Bundle();
                bundle.putInt("shopID", shopListID);
                navController.navigate(R.id.nav_send, bundle);                              //Opening shopping list linked to notification
            }
        }
        sharedpreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {         //Code to check if application first boot based off https://stackoverflow.com/questions/7217578/check-if-application-is-on-its-first-run
        super.onResume();

        if (sharedpreferences.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
            sharedpreferences.edit().putBoolean("firstrun", false).commit();
        }
    }

    //Method that opens link shop activity from the card adapter
    public void sendLinkShops(String name, int id){
        Bundle bundle = new Bundle();
        bundle.putString("shopListName", name);
        bundle.putInt("shopListID", id);
        navController.navigate(R.id.nav_slideshow, bundle);
    }
    //Method that creates geofence based off passed latlng and id
    public void createGeofenceData(LatLng latLng, int id){
        createGeofence(latLng, id + "");
        addGeofence(id);
    }
    //Method to remove geofence
    public void removeGeofenceData(int id){
        List<String> geofenceList = Arrays.asList(id + "");     //Concatenate ids to string of ids for removal
        removeGeofenceID(geofenceList);                         //Remove geofences based off id
        removeGeofencePending(id);                              //REmove any pending intents based off that id
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        PendingIntent.getBroadcast(this, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT).cancel();
    }
    //Method to open shopping list fragment
    public void openShopListFragment(View view, int shopID){
        Bundle bundle = new Bundle();
        bundle.putInt("shopID", shopID);
        Navigation.findNavController(view).navigate(R.id.nav_send, bundle);
    }
    //MEthod to open add item fragment
    public void openAddItemFragment(View view, int shopID){
        Bundle bundle = new Bundle();
        bundle.putInt("shopID", shopID);
        Navigation.findNavController(view).navigate(R.id.nav_share, bundle);
    }

    @Override       //MEthod to check for if notification was clicked when app is running in background/FOreground
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            String data = intent.getStringExtra("notification");
            if (data != null) {
                if(dataBase == null) {
                    dataBase = new DataBase(getApplicationContext());
                }
                int shopID = Integer.parseInt(intent.getStringExtra("shopID"));
                Location location = dataBase.getLocation(shopID);
                int shopListID = location.getShoppingListID();
                location.setGeofenced(false);
                if(dataBase.updateLocation(location)) {
                    removeGeofenceData(shopID);
                    Bundle bundle = new Bundle();
                    bundle.putInt("shopID", shopListID);
                    navController.navigate(R.id.nav_send, bundle);
                }
            }
        }
    }
    //Method for creating notification channel, based off https://developer.android.com/training/notify-user/build-notification
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
    //Method for checking for result from activity, used for PING.
    //Call needed here so that it can be used by fragments
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    //Method for creating geofences based off https://developer.android.com/training/location/geofencing
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
    //MEthod for creating geofence request, based off https://developer.android.com/training/location/geofencing
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }
    //Remove geofence pending intents by id based off https://developer.android.com/training/location/geofencing
    public void removeGeofencePending(int id){
        geofencingClient.removeGeofences(getGeofencePendingIntent(id))
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to remove geofences. Make sure location services are enabled.", Toast.LENGTH_SHORT).show();
                        // Failed to remove geofences
                        // ...
                    }
                });
    }
    //Remove geofences based off id based off https://developer.android.com/training/location/geofencing
    public void removeGeofenceID(List<String> geofences){
        geofencingClient.removeGeofences(geofences)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to remove geofences. Make sure location services are enabled.", Toast.LENGTH_SHORT).show();
                        // Failed to remove geofences
                        // ...
                    }
                });
    }
    //Adding geofence
    public void addGeofence(int geofenceID){
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(geofenceID))
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to add geofence. Make sure location services are enabled.", Toast.LENGTH_SHORT).show();
                        // Failed to add geofences
                        // ...
                    }
                });
    }
    //Method to get geofence pending intent based off https://developer.android.com/training/location/geofencing
    private PendingIntent getGeofencePendingIntent(int requestCode) {
        Context context = this;
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    //Method to handle backbutton for fragments
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
