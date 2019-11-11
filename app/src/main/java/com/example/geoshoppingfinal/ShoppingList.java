package com.example.geoshoppingfinal;

public class ShoppingList {

    private String name;
    private int shoppingListID;

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
}
