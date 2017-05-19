package com.draw.tales.main;

import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.draw.tales.R;
import com.draw.tales.classes.Constants;

public class MainActivity extends AppCompatActivity {
    private ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vp = (ViewPager)findViewById(R.id.home_viewpager);
        MainPagerAdapter pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        vp.setAdapter(pagerAdapter);
        vp.setOffscreenPageLimit(3);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.home_tabs);
        tabLayout.setupWithViewPager(vp);

        View iconView = getLayoutInflater().inflate(R.layout.pager_logo_icon,null);
        iconView.findViewById(R.id.logo_icon).setBackgroundResource(R.drawable.dtlogoonly);
        tabLayout.getTabAt(1).setCustomView(iconView);

        View groupView = getLayoutInflater().inflate(R.layout.pager_groups_text,null);
        groupView.findViewById(R.id.vp_groups).setBackgroundResource(R.drawable.logogroups);

//        Typeface typeface = Typeface.createFromAsset(getAssets(),Constants.FONT);
//        txt.setTypeface(typeface);
        tabLayout.getTabAt(0).setCustomView(groupView);

        View userView = getLayoutInflater().inflate(R.layout.pager_user_text,null);
        userView.findViewById(R.id.vp_user).setBackgroundResource(R.drawable.logouser);
//        usertxt.setTypeface(typeface);
        tabLayout.getTabAt(2).setCustomView(userView);

        if(getIntent().getBooleanExtra(Constants.GROUP_TO_MAIN_INTENT,false)){
            vp.setCurrentItem(0);
        } else if(getIntent().getBooleanExtra(Constants.USER,false)){
            vp.setCurrentItem(2);
        }
        else {
            vp.setCurrentItem(1);
        }
    }

    @Override
    public void onBackPressed() {
        if(vp.getCurrentItem()==0 || vp.getCurrentItem()==2){
            vp.setCurrentItem(1);
        } else {
            super.onBackPressed();
        }
    }
}
