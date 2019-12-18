package com.example.geoshoppingfinal;

/**
 * Created by Luke Shaw 17072613
 */
//Class to handle item list in a shopping list
public class ItemList extends Item {
    //Declaration and Initialisation
    private int quantity;               //Quantity
    private boolean bought;             //Is item bought
    private int itemListID;             //Item list id
    private boolean clearBought;        //Is clear bough
    private int shoppingListID;         //Shopping list id

    public ItemList(){
        this.setSeparator(false);
        this.setAddItem(false);
        this.setClearBought(false);
    }

    public ItemList(boolean separator){
        this.setBought(false);
        this.setQuantity(0);
        this.setItemListID(-1);
        this.setItemID(-1);
        this.setSeparator(separator);
        this.setName("Crossed Off");
        this.setAddItem(false);
        this.setClearBought(false);
    }

    public ItemList(int quantity, int id, int shoppingListID){
        this.setSeparator(false);
        this.setBought(false);
        this.setQuantity(quantity);
        this.setItemID(id);
        this.setItemListID(-1);
        this.setClearBought(false);
        this.setShoppingListID(shoppingListID);
    }
    //GEtters and setters
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

    public boolean isClearBought() {
        return clearBought;
    }

    public void setClearBought(boolean clearBought) {
        this.clearBought = clearBought;
    }

    public int getShoppingListID() {
        return shoppingListID;
    }

    public void setShoppingListID(int shoppingListID) {
        this.shoppingListID = shoppingListID;
    }
}
