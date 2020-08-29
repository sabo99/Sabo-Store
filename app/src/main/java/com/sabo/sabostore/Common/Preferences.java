package com.sabo.sabostore.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

    private static SharedPreferences getSharedPreference(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /** Email */
    public static void setEmail(Context context, String email){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(Common.SF_EMAIL, email);
        editor.apply();
    }

    public static String getEmail(Context context){
        return getSharedPreference(context).getString(Common.SF_EMAIL, "");
    }

    /** Password */
    public static void setPassword(Context context, String password){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(Common.SF_PASSWORD, password);
        editor.apply();
    }

    public static String getPassword(Context context){
        return getSharedPreference(context).getString(Common.SF_PASSWORD, "");
    }

    public static void clearPreferences(Context context){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.remove(Common.SF_EMAIL);
        editor.remove(Common.SF_PASSWORD);
        editor.apply();
    }
}
