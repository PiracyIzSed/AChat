package com.android.achat;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Gaurav on 27-01-2018.
 */

public  class SharedPrefs {

    SharedPreferences shPrefs ;

    public SharedPrefs(Context ctx,String currentUser) {
        shPrefs = ctx.getSharedPreferences(currentUser,Context.MODE_PRIVATE);
    }

    public void setNightMode(boolean b ){
        SharedPreferences.Editor ed = shPrefs.edit();
        ed.putBoolean("night_mode",b);
        ed.commit();
    }

    public boolean isNightModeEnabled(){
        return shPrefs.getBoolean("night_mode",false);
    }


}
