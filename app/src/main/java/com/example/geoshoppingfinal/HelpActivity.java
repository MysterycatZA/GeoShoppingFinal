package com.example.geoshoppingfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
/**
 * Created by Luke Shaw 17072613
 */
//Activity to display help  based off https://developer.android.com/training/animation/screen-slide
public class HelpActivity extends AppCompatActivity {
    //Declaration and Initialisation
    ViewPager mViewPager;                   //View pager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_help);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        // This is just an example. You can use whatever collection of images.
        int[] mResources = {
                R.drawable.welcome_screen,
                R.drawable.shop_screen,
                R.drawable.geofencing_screen,
                R.drawable.end_screen
        };

        HelpPagerAdapter mCustomPagerAdapter = new HelpPagerAdapter(this, mResources);

        mViewPager.setAdapter(mCustomPagerAdapter);
    }

    public void close(View v) {
        finish();
    }
}
