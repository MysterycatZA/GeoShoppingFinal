package com.example.geoshoppingfinal;
/**
 * Created by Luke Shaw 17072613
 */
public class Item {
    //Declaration and initialisation
    private String name;                //Name of item
    private int itemID;                 //Item id
    private boolean separator;          //Separator
    private boolean addItem;            //ADd item

    public Item(){
        this.setItemID(-1);
        this.setAddItem(false);
        this.setSeparator(false);
    }

    public Item(String name){
        this.setName(name);
        this.setItemID(-1);
        this.setAddItem(false);
    }

    public Item(String name, int id, boolean separator){
        this.setName(name);
        this.setItemID(id);
        this.setSeparator(separator);
        this.setAddItem(false);
    }
    //Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    public boolean isAddItem() {
        return addItem;
    }

    public void setAddItem(boolean addItem) {
        this.addItem = addItem;
    }
}
