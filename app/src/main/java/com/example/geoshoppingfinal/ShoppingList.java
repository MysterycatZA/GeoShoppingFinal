package com.example.geoshoppingfinal;

import android.content.Context;
/**
 * Created by Luke Shaw 17072613
 */
//Class to hold shopping list information
public class ShoppingList {
    //Declaration and initialisation
    private String name;                //Name of shopping list
    private int shoppingListID;         //Shopping list id
    private int lastLocationID;         //Last linked location id
    //Empty constructor
    public ShoppingList(){
    }
    //Main constructor
    public ShoppingList(String name){
        this.name = name;
        this.setShoppingListID(-1);
    }
    //Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getShoppingListID() {
        return shoppingListID;
    }

    public void setShoppingListID(int shoppingListID) {
        this.shoppingListID = shoppingListID;
    }

    public int getLastLocationID() {
        return lastLocationID;
    }

    public void setLastLocationID(int lastLocationID) {
        this.lastLocationID = lastLocationID;
    }
    //Method to check if shopping list is geofenced
    public boolean checkIfGeofenced(Context context){
        return new DataBase(context).checkListIsGeofenced(getShoppingListID());
    }
}
