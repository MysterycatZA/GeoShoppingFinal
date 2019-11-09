package com.example.geoshoppingfinal.ui;

import com.example.geoshoppingfinal.Item;

public class ItemList extends Item {

    private int quantity;
    private boolean bought;
    private int itemListID;

    public ItemList(){
        this.setSeparator(false);
        this.setAddItem(false);
    }

    public ItemList(int quantity){
        this.setBought(false);
        this.setQuantity(0);
        this.setItemListID(-1);
    }

    public ItemList(int quantity, int id){
        this.setBought(false);
        this.setQuantity(quantity);
        this.setItemID(id);
        this.setItemListID(-1);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public int getItemListID() {
        return itemListID;
    }

    public void setItemListID(int itemListID) {
        this.itemListID = itemListID;
    }
}
