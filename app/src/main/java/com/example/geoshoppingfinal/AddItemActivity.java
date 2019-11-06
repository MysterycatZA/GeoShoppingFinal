package com.example.geoshoppingfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

public class AddItemActivity extends AppCompatActivity
            implements ItemViewAdapter.AddItem{

    private ArrayList<Item> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        sortView();
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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                boolean search = true;
                int i = 0;
                while(search && i < list.size()){
                    if(!list.get(i).isSeperator() && list.get(i).getName().equalsIgnoreCase(newText)){
                        ItemViewAdapter adapter = new ItemViewAdapter(AddItemActivity.this, list);
                        ListView listView = (ListView) findViewById(R.id.itemView);
                        listView.setAdapter(adapter);
                        search = false;
                    }
                    i++;
                }
                return true;
            }
        });

        return true;
    }

    //Method that opens link shop activity from the card adapter
    public void sendItem(String name, int quantity){
        Intent intent = new Intent();
        intent.putExtra("name", name);
        intent.putExtra("quantity", quantity);
        setResult(RESULT_OK, intent);
        finish();
    }


    public void sortView(){

        DataBase db = new DataBase(this);

        // Creating our custom adapter
        list = db.retrieveItemsSorted();
        ItemViewAdapter adapter = new ItemViewAdapter(AddItemActivity.this, list);

        // Create the list view and bind the adapter
        ListView listView = (ListView) findViewById(R.id.itemView);
        listView.setAdapter(adapter);
    }
}
