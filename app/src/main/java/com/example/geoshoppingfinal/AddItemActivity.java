package com.example.geoshoppingfinal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLDisplay;

public class AddItemActivity extends AppCompatActivity
            implements ItemViewAdapter.AddItem{

    private ArrayList<Item> list;
    private ArrayList<Item> searchList;
    private int shopID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        shopID = getIntent().getIntExtra("shopListID", -1);
        setTitle("Add Item");
        sortView(false, "");
        FloatingActionButton fab = this.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });
    }

    public void addItem(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflat = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
        View myView = inflat.inflate(R.layout.dialog_add_item, null);
        builder.setView(myView);
        TextView itemName = (TextView) myView.findViewById(R.id.itemName);
        final EditText itemNameEdit = myView.findViewById(R.id.itemNameEdit);
        final EditText quantity = (EditText) myView.findViewById(R.id.itemQty);

        itemName.setVisibility(View.GONE);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {           //Yes
                if(!itemNameEdit.getText().toString().isEmpty() && !quantity.getText().toString().isEmpty()){
                    int amount = Integer.parseInt(quantity.getText().toString());
                    String name = itemNameEdit.getText().toString();
                    DataBase db = new DataBase(getBaseContext());
                    int itemID = db.saveItem(new Item(name));
                    if(itemID > 0){
                        sendItem(amount, itemID);
                        dialog.dismiss();
                    }

                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {       //No
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(this.getComponentName()));
        searchView.setQueryHint("Search item name here");
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.requestFocusFromTouch();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
/*                boolean search = true;
                int i = 0;
                while(search && i < list.size()){
                    if(!list.get(i).isSeparator() && list.get(i).getName().equalsIgnoreCase(newText)){
                        ItemViewAdapter adapter = new ItemViewAdapter(AddItemActivity.this, list);
                        ListView listView = (ListView) findViewById(R.id.itemView);
                        listView.setAdapter(adapter);
                        search = false;
                    }
                    i++;
                }*/
                if(newText != null && !newText.isEmpty()){                                  //If the search text is not null or empty
                    searchList = new ArrayList<Item>();                                    //Search array list
                    for(int i = 0; i < list.size(); i++){               //Looping through every anime object in the array list
                        if(!list.get(i).isSeparator() && list.get(i).getName().toLowerCase().startsWith(newText.toLowerCase())){    //If the anime name contains the word of search text, add it to the search array list
                            searchList.add(list.get(i));
                        }
                    }
                    sortView(true, newText);                                           //Repopulate list with search array list
                }
                else{
                    sortView(false, "");                                           //If there is no text in the search view then reset view
                }
                return true;
            }
        });

        return true;
    }

    public void deleteItem(){
        sortView(false, "");
    }

    //Method that opens link shop activity from the card adapter
    public void sendItem(int quantity, int id){
        //ItemList item = new ItemList(data.getIntExtra("quantity", -1), data.getIntExtra("id", -1), data.getIntExtra("shopID", -1));
        DataBase db = new DataBase(this);
        ItemList item = new ItemList(quantity, id, shopID);
        int values[] = db.checkItemListExist(item.getItemID(), item.getShoppingListID());
        if(values[0] == 0){
            if(db.saveListItem(item)){
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Not Saved", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            item.setItemListID(values[0]);
            item.setQuantity(item.getQuantity() + values[1]);
            if (db.updateListItem(item)) {
                Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
            }
        }
        Intent intent = new Intent();
/*        intent.putExtra("quantity", quantity);
        intent.putExtra("id", id);
        intent.putExtra("shopID", id);*/
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }


    public void sortView(boolean search, String searchText){

        if(search){
            list = searchList;

            if(list.size() < 1){
                Item item = new Item("Click here to add");
                item.setAddItem(true);
                list.add(item);
            }
        }
        else{
            DataBase db = new DataBase(this);

            // Creating our custom adapter
            list = db.retrieveItemsSorted();
        }

        ItemViewAdapter adapter = new ItemViewAdapter(AddItemActivity.this, list, searchText);

        // Create the list view and bind the adapter
        ListView listView = (ListView) findViewById(R.id.itemView);
        listView.setEmptyView(findViewById(R.id.emptyElement));
        listView.setAdapter(adapter);
    }
}
