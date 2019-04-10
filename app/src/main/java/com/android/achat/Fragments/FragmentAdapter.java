package com.android.achat.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.android.achat.Fragments.ChatsFragment;
import com.android.achat.Fragments.FriendsFragment;
import com.android.achat.Fragments.RequestsFragment;

/**
 * Created by Gaurav on 20-02-2018.
 */

public class FragmentAdapter extends FragmentPagerAdapter {
    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0 : RequestsFragment requestsFragment =  new RequestsFragment();
                     return requestsFragment;

            case 1 : ChatsFragment chatsFragment =  new ChatsFragment();
                return chatsFragment;

            case 2 : FriendsFragment friendsFragment =  new FriendsFragment();
                return friendsFragment;

            default: return null;
        }

    }
}
