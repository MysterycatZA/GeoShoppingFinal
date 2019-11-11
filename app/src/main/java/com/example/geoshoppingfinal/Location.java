package com.example.geoshoppingfinal;

public class Location {
    private String name;
    private double latitude;
    private double longitude;
    private boolean geofenced;
    private int locationID;
    private int shoppingListID;              //Shop tied to location for geofence

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
