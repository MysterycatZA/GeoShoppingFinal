package com.example.geoshoppingfinal;
/**
 * Created by Luke Shaw 17072613
 */
//Location class to store location information
public class Location {
    //Declaration and Initialisation
    private String name;                    //Location name
    private double latitude;                //Latitude
    private double longitude;               //Longitude
    private boolean geofenced;              //Location is geofenced
    private int locationID;                 //Location id
    private int shoppingListID;             //Shop tied to location for geofence
    //Default constructor
    public Location(){
        this.setGeofenced(false);
        this.setLocationID(-1);
    }

    public Location(String name, double latitude, double longitude){
        this.setName(name);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setGeofenced(false);
        this.setLocationID(-1);
    }
    //Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isGeofenced() {
        return geofenced;
    }

    public void setGeofenced(boolean geofenced) {
        this.geofenced = geofenced;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public int getShoppingListID() {
        return shoppingListID;
    }

    public void setShoppingListID(int shoppingListID) {
        this.shoppingListID = shoppingListID;
    }
}
