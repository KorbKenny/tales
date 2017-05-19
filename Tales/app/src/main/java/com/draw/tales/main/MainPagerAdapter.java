package com.draw.tales.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.draw.tales.groups.GroupsFragment;
import com.draw.tales.user.UserFragment;

/**
 * Created by KorbBookProReturns on 2/14/17.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {
    public MainPagerAdapter(FragmentManager fm){super(fm);}

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new GroupsFragment();
            case 1:
                return new MainFragment();
            case 2:
                return new UserFragment();
            default:
                return null;
        }
    }



    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "Groups";
            case 1:
                return null;
            case 2:
                return "User";
            default:
                return null;
        }
    }
}
