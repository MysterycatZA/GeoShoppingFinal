package com.example.geoshoppingfinal;

import android.content.Context;

public class ShoppingList {

    private String name;
    private int shoppingListID;
    private int lastLocationID;

    public ShoppingList(){
    }

    public ShoppingList(String name){
        this.name = name;
        this.setShoppingListID(-1);
    }

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

    public boolean checkIfGeofenced(Context context){
        return new DataBase(context).checkListIsGeofenced(getShoppingListID());
    }
}
