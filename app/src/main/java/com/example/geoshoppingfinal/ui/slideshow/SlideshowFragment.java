package com.example.geoshoppingfinal.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.geoshoppingfinal.DataBase;
import com.example.geoshoppingfinal.LocationListViewAdapter;
import com.example.geoshoppingfinal.R;

public class SlideshowFragment extends Fragment {

    private LocationListViewAdapter adapter;
    private ListView listView;
    private SlideshowViewModel slideshowViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
/*        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel.class);*/
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
/*        final TextView textView = root.findViewById(R.id.text_slideshow);
        slideshowViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        listView = (ListView) root.findViewById(R.id.locationListView);
        loadData();
        return root;
    }

    private void loadData() {
        DataBase dataBase = new DataBase(getActivity());
        adapter = new LocationListViewAdapter(getActivity(), dataBase.retrieveLocations());             //List view displaying items
        listView.setAdapter(adapter);
    }
}