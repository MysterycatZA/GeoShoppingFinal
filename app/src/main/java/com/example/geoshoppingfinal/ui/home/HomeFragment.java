package com.example.geoshoppingfinal.ui.home;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoshoppingfinal.DataBase;
import com.example.geoshoppingfinal.MainActivity;
import com.example.geoshoppingfinal.MainViewModel;
import com.example.geoshoppingfinal.R;
import com.example.geoshoppingfinal.ShoppingList;
import com.example.geoshoppingfinal.ShoppingListViewAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;                                  //Recycle view for card view
    private RecyclerView.Adapter adapter;                              //Adapter for card view
    private RecyclerView.LayoutManager mLayoutManager;                  //Layout manager for card view
    private ArrayList<ShoppingList> list;
    private View root;
    private MainViewModel mainViewModel;
    private TextView emptyView;
    private DataBase dataBase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = (RecyclerView) root.findViewById(R.id.shopping_recycler_view);
        emptyView = (TextView) root.findViewById(R.id.emptyElement);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        dataBase = new DataBase(getActivity());

        loadData(0);
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addShopList();
            }
        });
        HomeViewModel model = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);

        return root;
    }

    private void addShopList(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Shop List");
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View myView = inflater.inflate(R.layout.dialog_add_shop_list, null);
        builder.setView(myView);
        final EditText shopName = (EditText) myView.findViewById(R.id.shopName);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {           //Yes
                if(!shopName.getText().toString().isEmpty()){
                    if(!dataBase.checkShoppingListExist(shopName.getText().toString())) {
                        int itemID = dataBase.saveShopList(new ShoppingList(shopName.getText().toString()));
                        if (itemID > 0) {
                            loadData(0);
                            dialog.dismiss();
                        }
                    }
                    else{
                        Toast.makeText(getContext(), "Item already exists!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override   //On resume method that holds a click listener that opens item list of specific shopping list when card is clicked
    public void onResume() {
        super.onResume();
        ((ShoppingListViewAdapter) adapter).setOnItemClickListener(new ShoppingListViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {

                ((MainActivity) getActivity()).openShopListFragment(root, list.get(position).getShoppingListID());
            }
        });
    }

    private void loadData(int sort) {
        list = dataBase.retrieveShopList();
        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        adapter = new ShoppingListViewAdapter(getContext(), list);             //List view displaying items
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }

            void checkEmpty() {
                emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
        recyclerView.setAdapter(adapter);
    }
}