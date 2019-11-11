package com.example.geoshoppingfinal.ui.send;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.geoshoppingfinal.AddItemActivity;
import com.example.geoshoppingfinal.DataBase;
import com.example.geoshoppingfinal.ItemList;
import com.example.geoshoppingfinal.ItemListViewAdapter;
import com.example.geoshoppingfinal.MainActivity;
import com.example.geoshoppingfinal.MainViewModel;
import com.example.geoshoppingfinal.R;
import com.example.geoshoppingfinal.ui.home.HomeViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class SendFragment extends Fragment {

    private ItemListViewAdapter adapter;
    private ListView listView;
    private ArrayList<ItemList> list;
    private int shopID;
    private MainViewModel mainViewModel;
    //SendFragmentArgs args;
    private static final int REQUEST_CODE_ITEM = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //args = SendFragmentArgs.fromBundle(requireArguments());
        DataBase dataBase = new DataBase(getContext());
        shopID = getArguments().getInt("shopID");
        String title = dataBase.getShopListName(shopID);
        mainViewModel =
                ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mainViewModel.addTitle(title + " List");
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send, container, false);
        listView = (ListView) root.findViewById(R.id.itemListView);
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });
/*        if(shopID != 0){
            shopID = args.getShopID();
        }*/
        loadData(0, shopID);
/*        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogBox newFragment = new DialogBox();
                newFragment.show(getActivity().getSupportFragmentManager(), "DIALOG");
            }
        });*/
        SendViewModel model = ViewModelProviders.of(getActivity()).get(SendViewModel.class);
/*        model.getSelected().observe(this, new Observer<Item>() {
            @Override
            public void onChanged(@Nullable Item item) {
                if (new DataBase(getActivity()).saveListItem(item)) {
                    Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Not Saved", Toast.LENGTH_SHORT).show();
                }
                loadData();
            }
        });*/

        ((MainActivity)getActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(0, shopID);
                // Refresh Your Fragment
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if((requestCode == REQUEST_CODE_ITEM) && (resultCode == Activity.RESULT_OK)){
            loadData(0, shopID);
/*            ItemList item = new ItemList(data.getIntExtra("quantity", -1), data.getIntExtra("id", -1), data.getIntExtra("shopID", -1));
            DataBase db = new DataBase(getContext());
            int values[] = db.checkItemListExist(item.getItemID(), item.getShoppingListID());
            if(values[0] == 0){
                if(db.saveListItem(item)){
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                    loadData(0, shopID);
                }
                else {
                    Toast.makeText(getContext(), "Not Saved", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                item.setItemListID(values[0]);
                item.setQuantity(item.getQuantity() + values[1]);
                if (db.updateListItem(item)) {
                    Toast.makeText(getContext(), "Updated!", Toast.LENGTH_SHORT).show();
                    loadData(0, shopID);
                }
            }*/
        }
/*        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            //some code
        }*/
    }

    //Method that opens link shop activity from the card adapter
    public void addItem(){
        Intent intent = new Intent(getActivity(), AddItemActivity.class);
        intent.putExtra("shopListID", shopID);
        startActivityForResult(intent, REQUEST_CODE_ITEM);
    }

/*    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        sendViewModel =
                ViewModelProviders.of(this).get(SendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_send, container, false);
        final TextView textView = root.findViewById(R.id.text_send);
        sendViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sort, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_asc:
                loadData(1, shopID);
                return true;
            case R.id.action_desc:
                loadData(2, shopID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadData(int sort, int shopID) {
        DataBase dataBase = new DataBase(getActivity());
        list = dataBase.retrieveListItems(sort, shopID);
        adapter = new ItemListViewAdapter(getActivity(), list);             //List view displaying items
        listView.setAdapter(adapter);
    }
}