package com.example.geoshoppingfinal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity
    implements ShoppingListViewAdapter.LinkShops,
        ShoppingListViewAdapter.HandleGeofence,
        LocationListViewAdapter.GeoFenceInterface {
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private DrawerLayout drawer;
    private GeofencingClient geofencingClient;
    private ArrayList<Geofence> geofences;
    private MainViewModel mainViewModel;
    private DataBase dataBase;
    //Constants
    private static final float GEOFENCE_RADIUS = 300.0f; // in meters
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
                R.id.nav_home, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        final FloatingActionButton fab = this.findViewById(R.id.fab);
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
        dataBase = new DataBase(this);
        dataBase.setupItem();
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
        addGeofence(id);
    }
    public void removeGeofenceData(int id){
        List<String> geofenceList = Arrays.asList(id + "");
        removeGeofenceID(geofenceList);
        removeGeofencePending(id);
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        PendingIntent.getBroadcast(this, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT).cancel();
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

    public interface FragmentRefreshListener{
        void onRefresh();
    }

    //Method that opens link shop activity from the card adapter
    public void addItem(){
        Intent intent = new Intent(this, AddItemActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ITEM);
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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



    private PendingIntent getGeofencePendingIntent(int requestCode) {
        Context context = this;
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
