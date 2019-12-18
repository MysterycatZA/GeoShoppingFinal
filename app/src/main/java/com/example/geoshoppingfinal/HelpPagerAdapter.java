package com.example.geoshoppingfinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.PagerAdapter;
/**
 * Created by Luke Shaw 17072613
 */
//ADapter to display help pages in a carousel code is based off https://developer.android.com/training/animation/screen-slide
public class HelpPagerAdapter extends PagerAdapter {
    //Declaration and Initialisation
    private Context mContext;                   //Context
    private LayoutInflater mLayoutInflater;     //layout inflater
    private int[] mResources;                   //Page resources

    public HelpPagerAdapter(Context context, int[] resources) {
        mContext = context;
        mResources = resources;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    //Get page count
    @Override
    public int getCount() {
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }
    //Display items
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        imageView.setImageResource(mResources[position]);

        container.addView(itemView);

        return itemView;
    }
    //REmove item
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

}