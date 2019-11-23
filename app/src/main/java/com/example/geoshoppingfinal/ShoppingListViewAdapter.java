package com.example.geoshoppingfinal;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Luke Shaw 17072613
 */
//Card adapter to display shopping list item on the card view from the shopping list array list
public class ShoppingListViewAdapter extends RecyclerView
        .Adapter<ShoppingListViewAdapter
        .DataObjectHolder>{
    private ArrayList<ShoppingList> mDataset;                       //Data set
    private static MyClickListener myClickListener;                //Card listener
    public LinkShops linkShops;                                     //Link shop interface
    public HandleGeofence handleGeofence;
    private Context context;
    private DataBase dataBase;

    public interface HandleGeofence{                                     //Interface for sending the position and id of the shopping list back to the main activity
        void removeGeofenceData(int id);
    }

    public interface LinkShops{                                     //Interface for sending the position and id of the shopping list back to the main activity
        void sendLinkShops(String name, int id);
    }
    //Data object holder for the card view
    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView label;
        TextView total;
        TextView bought;
        ImageView deleteImage;
        ImageView linkShops;
        CardView cardView;
        public DataObjectHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.name);
            total = (TextView) itemView.findViewById(R.id.total);
            bought = (TextView) itemView.findViewById(R.id.bought);
            deleteImage = (ImageView) itemView.findViewById(R.id.delete_button);
            linkShops = (ImageView) itemView.findViewById(R.id.link_button);
            cardView = itemView.findViewById(R.id.card_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }
    //On item click card listener
    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }
    //Constructor getting the passed array list
    public ShoppingListViewAdapter(Context context, ArrayList<ShoppingList> myDataset)
    {
        this.mDataset = myDataset;
        this.context = context;
        this.dataBase = new DataBase(context);
    }

    @Override       //Creating view holder from the assigned card view
    public DataObjectHolder onCreateViewHolder(final ViewGroup parent,
                                               int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_shopping_list, parent, false);
        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        ImageView deleteImage = (ImageView) view.findViewById(R.id.delete_button);
        ImageView linkImage = (ImageView) view.findViewById(R.id.link_button);
        linkImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkShops  = (LinkShops) parent.getContext();
                int id = (int)view.findViewById(R.id.delete_button).getTag();
                String name = (String) view.findViewById(R.id.name).getTag();
                linkShops.sendLinkShops(name, id);
            }
        });

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                builder.setTitle("Do you want to delete this shopping list?");
// Add the buttons
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        int position = (int) view.findViewById(R.id.link_button).getTag();
                        int shopID = (int) view.findViewById(R.id.delete_button).getTag();
                        deleteItem(position, shopID);
                        dialog.dismiss();
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
        });
        return dataObjectHolder;
    }

    @Override       //Binding view objects to card
    public void onBindViewHolder(final DataObjectHolder holder, int position) {
        holder.label.setText(mDataset.get(position).getName());             //Setting name to card view
        holder.bought.setText(dataBase.getBoughtCount(mDataset.get(position).getShoppingListID()) + "");
        holder.total.setText( "/" + dataBase.getTotalListItems(mDataset.get(position).getShoppingListID()));                                //Setting the item total
        holder.deleteImage.setTag(mDataset.get(position).getShoppingListID());  //Setting id in tag
        holder.linkShops.setTag(position);                                      //Setting current posistion in card view in tag
        holder.label.setTag(mDataset.get(position).getName());
        if(mDataset.get(position).checkIfGeofenced(context)){
            holder.label.setTextColor(Color.parseColor("#FFFFFF"));
            holder.bought.setTextColor(Color.parseColor("#FFFFFF"));
            holder.total.setTextColor(Color.parseColor("#FFFFFF"));
            holder.linkShops.setImageResource(R.drawable.baseline_link_white_18dp);
            holder.deleteImage.setImageResource(R.drawable.baseline_delete_outline_white_18dp);
            holder.cardView.setBackgroundColor(Color.parseColor("#4CAF50"));
        }
    }
    //Method to add a card item
    public void addItem(ShoppingList dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }
    //Method to remove a card item
    public void deleteItem(int index, int shopID) {
        if(index < mDataset.size()) {
            if(dataBase.deleteShopList(mDataset.get(index))){
                dataBase.deleteLinkedShopToList(shopID);
                int locationID = dataBase.checkForlinkShoppingList(shopID);
                if(locationID != 0){
                    Location location = dataBase.getLocation(locationID);
                    location.setGeofenced(false);
                    dataBase.updateLocation(location);
                    handleGeofence  = (HandleGeofence) context;
                    handleGeofence.removeGeofenceData(locationID);
                }
                mDataset.remove(index);
                notifyItemRemoved(index);
                notifyItemRangeChanged(index, mDataset.size());
            }
        }
    }

    @Override       //Method that returns the amount of cards
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {      //Click listener
        void onItemClick(int position, View v);
    }
}