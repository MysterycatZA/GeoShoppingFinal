package com.example.geoshoppingfinal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DataBase {

    private Context context;
    private SQLiteDatabase db;
    public DataBaseHelper helper;


    // INITIALIZE DB HELPER AND PASS IT A CONTEXT
    public DataBase(Context context) {
        this.context = context;
        this.helper = new DataBaseHelper(context);
    }

    //DELETE FROM DATABASE
    public boolean deleteHistoryItemID(int itemID){
        try{
            db = helper.getWritableDatabase();
            int result = db.delete(context.getString(R.string.HISTORY_TABLE), "itemID = ?", new String[] { String.valueOf(itemID) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    //DELETE FROM DATABASE
    public boolean deleteHistoryLocationID(int locationID){
        try{
            db = helper.getWritableDatabase();
            int result = db.delete(context.getString(R.string.HISTORY_TABLE), "locationID = ?", new String[] { String.valueOf(locationID) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    //SAVE Location TO DB
    public boolean saveHistory(int shopID, int locationID) {
        try {

            ArrayList<ItemList> itemListArrayList = retrieveListItems(1, shopID);

            if(itemListArrayList.size() > 0) {

                db = helper.getWritableDatabase();

                for (ItemList itemList:itemListArrayList) {
                    ContentValues values = new ContentValues();
                    values.put("itemID", itemList.getItemID());
                    values.put("locationID", locationID);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
                    Date date = new Date();
                    values.put("date", dateFormat.format(date));
                    values.put("count", getItemHistoryCount(itemList.getItemID(), locationID) + 1);
                    int update = db.update(context.getString(R.string.HISTORY_TABLE), values, "itemID = ? AND locationID = ?", new String[]{String.valueOf(itemList.getItemID()), String.valueOf(locationID)});
                    if (update == 0) {
                        db.insertWithOnConflict(context.getString(R.string.HISTORY_TABLE), null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }
                }

                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return false;
    }

    public int getItemLocationHistory(int itemID){
        int locationID = - 1;
        boolean found = false;
        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT locationID FROM History where itemID = " + itemID + " ORDER BY count, date DESC",null);

            while (cursor.moveToNext() && !found)
            {
                locationID = cursor.getInt(0);
                found = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return locationID;
    }

    public int getItemHistoryCount(int itemID, int locationID){
        int count = 0;
        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT count FROM History where itemID = " + itemID + " AND locationID = " + locationID,null);

            while (cursor.moveToNext())
            {
                count = cursor.getInt(0);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            helper.close();
        }

        return count;
    }

    public void setupItem(){
        try {
            db = helper.getWritableDatabase();
            if (db.rawQuery("SELECT * FROM Item", null).getCount() < 1) {

                String[] items = new String[]{
                        "Apple",
                        "Banana",
                        "Beans",
                        "Milk",
                        "Cereal",
                        "Cheese",
                        "Rice",
                        "Fish",
                        "Pasta",
                        "Chicken",
                        "Nut Butter",
                        "Spinach",
                        "Bread",
                        "Flour",
                        "Eggs"

                };

                ContentValues insertValues = new ContentValues();

                for (String item : items) {

                    insertValues.put("name", item);
                    db.insert(context.getString(R.string.ITEM_TABLE), null, insertValues);
                }
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        } finally{
            helper.close();
        }
    }

    //UPDATE Location
    public boolean updateLocation(Location location){
        try{
            db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("name", location.getName());
            values.put("latitude", location.getLatitude());
            values.put("longitude", location.getLongitude());
            values.put("geofenced", location.isGeofenced());
            values.put("shopID", location.getShoppingListID());

            int result = db.update(context.getString(R.string.LOCATION_TABLE), values, "id = ?", new String[] { String.valueOf(location.getLocationID()) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    public boolean checkListIsGeofenced(int shopID){
        boolean geofenced = false;
        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT geofenced FROM Location where geofenced = 1 AND shopID = " + shopID,null);

            if(cursor.getCount() > 0){
                geofenced = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return geofenced;
    }

    public ShoppingList getShopList(int shopID){
        ShoppingList shoppingList = new ShoppingList();

        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM Shopping WHERE id = " + shopID,null);

            while (cursor.moveToNext())
            {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int lastLocation = cursor.getInt(2);

                shoppingList.setName(name);
                shoppingList.setShoppingListID(id);
                shoppingList.setLastLocationID(lastLocation);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return shoppingList;
    }

    public int getShopListID(int locationID){
        int shopID = 0;
        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT shopID FROM Location where id = " + locationID,null);

            while (cursor.moveToNext())
            {
                shopID = cursor.getInt(0);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return shopID;
    }

    public String getLocationName(int locationID){
        String name = "";
        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT name FROM Location where id = " + locationID,null);

            while (cursor.moveToNext())
            {
                name = cursor.getString(0);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return name;
    }

    //DELETE FROM DATABASE
    public boolean deleteLocation(Location location){
        try{
            db = helper.getWritableDatabase();
            int result = db.delete(context.getString(R.string.LOCATION_TABLE), "id = ?", new String[] { String.valueOf(location.getLocationID()) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    //SAVE Location TO DB
    public int saveLocation(Location location) {
        try {
            db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("name", location.getName());
            values.put("latitude", location.getLatitude());
            values.put("longitude", location.getLongitude());
            values.put("geofenced", location.isGeofenced());
            values.put("shopID", location.getShoppingListID());

            long result = db.insert(context.getString(R.string.LOCATION_TABLE), "id", values);
            if (result > 0) {
                return (int)result;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return 0;
    }

    public boolean checkLocationExist(Location location){
        try {
            db = helper.getWritableDatabase();
            String []columns = {"id", "name"};
            String []selectionArgs = {location.getName() + "%"};
            //db.query("Location", columns,"name LIKE ?",selectionArgs,null,null,null);
            //Cursor cursor = db.rawQuery("SELECT * FROM Location Where name like '" + location.getName() + "'",null);
            //if(db.query("Location", new String[] {"id","name"},"name LIKE '?'", new String[]{location.getName()+"%"}, null, null, null).getCount() > 0){
            if(db.query(context.getString(R.string.LOCATION_TABLE), columns,"name LIKE ?",selectionArgs,null,null,null).getCount() > 0){
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return false;
    }

    public int retrieveLocationID(Location location){
        int id = 0;
        try {
            db = helper.getWritableDatabase();
            String []columns = {"id", "name"};
            String []selectionArgs = {location.getName() + "%"};
            Cursor cursor = db.query(context.getString(R.string.LOCATION_TABLE), columns,"name LIKE ?",selectionArgs,null,null,null);
            //Cursor cursor = db.query("Location", new String[] {"id","name"},"name LIKE '?'", new String[]{location.getName()+"%"}, null, null, null);
            while (cursor.moveToNext())
            {
                id = cursor.getInt(0);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return id;
    }

    //Relieving locations from the SQLlite Database
    public Location getLocation(int id) {
        Location location = new Location();

        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM Location WHERE id = " + id,null);

            while (cursor.moveToNext())
            {
                int locationID = cursor.getInt(0);
                String name = cursor.getString(1);
                double latitude = cursor.getDouble(2);
                double longitude = cursor.getDouble(3);
                boolean geofenced = (cursor.getInt(4) != 0);
                int shopListID = cursor.getInt(5);

                location.setName(name);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setGeofenced(geofenced);
                location.setLocationID(locationID);
                location.setShoppingListID(shopListID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return location;
    }

    //Relieving locations from the SQLlite Database
    public ArrayList<Location> retrieveLocations() {
        ArrayList<Location> arrayList = new ArrayList<>();

        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM Location",null);

            Location location;
            arrayList.clear();

            while (cursor.moveToNext())
            {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double latitude = cursor.getDouble(2);
                double longitude = cursor.getDouble(3);
                boolean geofenced = (cursor.getInt(4) != 0);
                int shopListID = cursor.getInt(5);

                location = new Location();
                location.setName(name);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setGeofenced(geofenced);
                location.setLocationID(id);
                location.setShoppingListID(shopListID);

                arrayList.add(location);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return arrayList;
    }

    //UPDATE Item
    public boolean updateItem(Item item){
        try{
            db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("name", item.getName());

            int result = db.update(context.getString(R.string.ITEM_TABLE), values, "id = ?", new String[] { String.valueOf(item.getItemID()) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    //DELETE FROM DATABASE
    public boolean deleteItem(Item item){
        try{
            db = helper.getWritableDatabase();
            int result = db.delete(context.getString(R.string.ITEM_TABLE), "id = ?", new String[] { String.valueOf(item.getItemID()) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    //SAVE DATA TO DB
    public int saveItem(Item item) {
        try {
            db = helper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("name", item.getName());

            long result = db.insert(context.getString(R.string.ITEM_TABLE), "id", contentValues);
            if (result > 0) {
                return (int)result;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return 0;
    }

    public boolean checkItemExist(String name){
        try {
            db = helper.getWritableDatabase();
            String []columns = {"id", "name"};
            String []selectionArgs = {name + "%"};
            if(db.query(context.getString(R.string.ITEM_TABLE), columns,"name LIKE ?",selectionArgs,null,null,null).getCount() > 0){
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return false;
    }

    public boolean checkShoppingListExist(String name){
        try {
            db = helper.getWritableDatabase();
            String []columns = {"id", "name"};
            String []selectionArgs = {name + "%"};
            if(db.query(context.getString(R.string.SHOP_LIST_TABLE), columns,"name LIKE ?",selectionArgs,null,null,null).getCount() > 0){
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return false;
    }

    //Relieving items from the SQLlite Database
    public ArrayList<Item> retrieveItemsSorted() {

        ArrayList<Item> items = new ArrayList<>();

        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM Item Order by name ASC",null);

            int position = 0;
            boolean isSeparator;

            while(cursor.moveToNext()) {
                isSeparator = false;

                String name = cursor.getString(1);
                int id = cursor.getInt(0);

                char[] nameArray;

                // If it is the first item then need a separator
                if (position == 0) {
                    isSeparator = true;
                    nameArray = name.toCharArray();
                }
                else {
                    // Move to previous
                    cursor.moveToPrevious();

                    // Get the previous contact's name
                    String previousName = cursor.getString(1);

                    // Convert the previous and current contact names
                    // into char arrays
                    char[] previousNameArray = previousName.toCharArray();
                    nameArray = name.toCharArray();

                    // Compare the first character of previous and current contact names
                    if (nameArray[0] != previousNameArray[0]) {
                        isSeparator = true;
                    }

                    // Don't forget to move to next
                    // which is basically the current item
                    cursor.moveToNext();
                }

                // Need a separator? Then create a Contact
                // object and save it's name as the section
                // header while pass null as the phone number
                if (isSeparator) {
                    Item item = new Item(String.valueOf(nameArray[0]), id, true);
                    items.add( item );
                }

                // Create a Contact object to store the name/number details
                Item item = new Item(name, id, false);
                items.add( item );

                position++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            helper.close();
        }

        return items;
    }

    //Relieving items from the SQLlite Database
    public ArrayList<Item> retrieveItems() {
        ArrayList<Item> arrayList = new ArrayList<>();

        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM Item",null);

            Item item;
            arrayList.clear();

            while (cursor.moveToNext())
            {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);

                item = new Item();
                item.setName(name);
                item.setItemID(id);

                arrayList.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return arrayList;
    }

    //UPDATE Item
    public boolean updateShopList(ShoppingList item){
        try{
            db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("name", item.getName());
            values.put("lastLocation", item.getLastLocationID());

            int result = db.update(context.getString(R.string.SHOP_LIST_TABLE), values, "id = ?", new String[] { String.valueOf(item.getShoppingListID()) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    //DELETE FROM DATABASE
    public boolean deleteShopList(ShoppingList item){
        try{
            db = helper.getWritableDatabase();
            int result = db.delete(context.getString(R.string.SHOP_LIST_TABLE), "id = ?", new String[] { String.valueOf(item.getShoppingListID()) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    //SAVE DATA TO DB
    public int saveShopList(ShoppingList item) {
        try {
            db = helper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("name", item.getName());
            contentValues.put("lastLocation", item.getLastLocationID());

            long result = db.insert(context.getString(R.string.SHOP_LIST_TABLE), "id", contentValues);
            if (result > 0) {
                return (int)result;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return 0;
    }

    //Relieving items from the SQLlite Database
    public String getShopListName(int shopListID) {
        String shopName = "";
        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT name FROM Shopping WHERE id = " + shopListID,null);

            while (cursor.moveToNext())
            {
                shopName = cursor.getString(0);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return shopName;
    }

    //Relieving items from the SQLlite Database
    public ArrayList<ShoppingList> retrieveShopList() {
        ArrayList<ShoppingList> arrayList = new ArrayList<>();

        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM Shopping",null);

            ShoppingList item;
            arrayList.clear();

            while (cursor.moveToNext())
            {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int lastLocation = cursor.getInt(2);

                item = new ShoppingList();
                item.setName(name);
                item.setShoppingListID(id);
                item.setLastLocationID(lastLocation);

                arrayList.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return arrayList;
    }

    public int[] checkItemListExist(int itemID, int shopID){
        int[] values = new int[2];
        try {
            db = helper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, quantity FROM List Where deleted = 0 AND itemID = " + itemID + " AND shopID = " + shopID,null);

            if(cursor.getCount() > 0){
                while (cursor.moveToNext()) {
                    values[0] = cursor.getInt(0);
                    values[1] = cursor.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return values;
    }

    //UPDATE Item
    public boolean updateListItem(ItemList item){
        try{
            db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("quantity", item.getQuantity());
            values.put("bought", item.isBought());

            int result = db.update(context.getString(R.string.LIST_TABLE), values, "id = ?", new String[] { String.valueOf(item.getItemListID()) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    //DELETE FROM DATABASE
    public boolean deleteLinkedItemToList(int itemID){
        try{
            db = helper.getWritableDatabase();
            int result = db.delete(context.getString(R.string.LIST_TABLE), "itemID = ?", new String[] { String.valueOf(itemID) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    //DELETE FROM DATABASE
    public boolean deleteLinkedShopToList(int shopID){
        try{
            db = helper.getWritableDatabase();
            int result = db.delete(context.getString(R.string.LIST_TABLE), "shopID = ?", new String[] { String.valueOf(shopID) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    public int checkForlinkShoppingList(int shopID){
        int locationID = 0;
        try{
            db = helper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT id FROM Location where geofenced = 1 AND shopid = " + shopID,null);

            while (cursor.moveToNext())
            {
                locationID = (cursor.getInt(0));
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return locationID;
    }

    public int getTotalListItems(int shopID){
        int count = 0;
        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT COUNT(id) FROM List WHERE deleted = 0 AND shopid = " + shopID,null);

            while (cursor.moveToNext())
            {
                count = cursor.getInt(0);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return count;

    }

    public int getBoughtCount(int shopID){
        int count = 0;
        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT bought, COUNT(bought) FROM List where deleted = 0 AND shopid = " + shopID + " GROUP BY bought",null);

            while (cursor.moveToNext())
            {
                boolean bought = (cursor.getInt(0) != 0);
                if(bought){
                    count = cursor.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return count;
    }

    public ArrayList<Item> getSuggestedItems(int shopID){
        ArrayList<Item> items = new ArrayList<>();
        Item item;
        try {
            db = helper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT List.itemid, Item.name, count(List.id), List.shopID from List JOIN Item On List.itemID = Item.id WHERE List.shopid = " + shopID + " group by List.itemID, Item.name ORDER by COUNT(List.itemid) DESC Limit 10",null);
            items.clear();

            while (cursor.moveToNext())
            {
                int itemID = (cursor.getInt(0));
                String name = (cursor.getString(1));
                item = new Item();
                item.setName(name);
                item.setItemID(itemID);
                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return items;
    }


    //DELETE FROM DATABASE
    public boolean deleteListItem(ItemList item){
        try{
            db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("deleted", 1);

            int result = db.update(context.getString(R.string.LIST_TABLE), values, "id = ?", new String[] { String.valueOf(item.getItemListID()) });

            if (result > 0) {
                return true;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }
        return false;
    }

    //SAVE DATA TO DB
    public boolean saveListItem(ItemList item) {
        try {
            db = helper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("itemID", item.getItemID());
            contentValues.put("quantity", item.getQuantity());
            contentValues.put("bought", item.isBought());
            contentValues.put("shopID", item.getShoppingListID());

            long result = db.insert(context.getString(R.string.LIST_TABLE), "id", contentValues);
            if (result > 0) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return false;
    }

    //Relieving items from the SQLlite Database
    public ArrayList<ItemList> retrieveListItems(int sort, int shopID) {
        ArrayList<ItemList> arrayList = new ArrayList<>();

        try {
            db = helper.getWritableDatabase();
            String extra = "";
            switch (sort){
                case 1:
                    extra = " ORDER by Item.name ASC";
                    break;
                case 2:
                    extra = " ORDER by Item.name DESC";
                    break;
            }
            //Cursor cursor = db.rawQuery("SELECT List.id, List.itemID, List.quantity, List.bought, List.shopID, Item.name FROM List JOIN Item on List.itemID = Item.id" + extra,null);
            Cursor cursor = db.rawQuery("SELECT List.id, List.itemID, List.quantity, List.bought, List.shopID, Item.name FROM List JOIN Item on List.itemID = Item.id GROUP by LIst.id HAVING List.deleted = 0 AND List.shopid = " + shopID + extra,null);

            ItemList item;
            arrayList.clear();

            while (cursor.moveToNext())
            {
                int id = cursor.getInt(0);
                int itemID = cursor.getInt(1);
                int quantity = cursor.getInt(2);
                boolean bought = (cursor.getInt(3) != 0);
                shopID = cursor.getInt(4);
                String name = cursor.getString(5);

                item = new ItemList();
                item.setItemID(itemID);
                item.setQuantity(quantity);
                item.setBought(bought);
                item.setItemListID(id);
                item.setShoppingListID(shopID);
                item.setName(name);

                arrayList.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            helper.close();
        }

        return arrayList;
    }

}