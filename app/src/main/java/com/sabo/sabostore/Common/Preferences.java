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
    public static void setPassword(Context context, String password) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(Common.SF_PASSWORD, password);
        editor.apply();
    }

    public static String getPassword(Context context) {
        return getSharedPreference(context).getString(Common.SF_PASSWORD, "");
    }

    /** Checkout Shipping */

    /**
     * Name
     */
    public static void setName(Context context, String name) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(Common.SF_NAME, name);
        editor.apply();
    }

    public static String getName(Context context) {
        return getSharedPreference(context).getString(Common.SF_NAME, "");
    }

    /**
     * Phone
     */
    public static void setPhone(Context context, String phone) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(Common.SF_PHONE, phone);
        editor.apply();
    }

    public static String getPhone(Context context) {
        return getSharedPreference(context).getString(Common.SF_PHONE, "");
    }

    /**
     * Address
     */
    public static void setAddress(Context context, String address) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(Common.SF_ADDRESS, address);
        editor.apply();
    }

    public static String getAddress(Context context) {
        return getSharedPreference(context).getString(Common.SF_ADDRESS, "");
    }


    /**
     * ZIP
     */
    public static void setZIP(Context context, String zip) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(Common.SF_ZIP, zip);
        editor.apply();
    }

    public static String getZip(Context context) {
        return getSharedPreference(context).getString(Common.SF_ZIP, "");
    }

    /**
     * City
     */
    public static void setCity(Context context, String city) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(Common.SF_CITY, city);
        editor.apply();
    }

    public static String getCity(Context context) {
        return getSharedPreference(context).getString(Common.SF_CITY, "");
    }

    /**
     * Save Shipping
     */
    public static void setSaveShipping(Context context, boolean save) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(Common.SF_SAVE_SHIPPING, save);
        editor.apply();
    }

    public static boolean getSaveShipping(Context context) {
        return getSharedPreference(context).getBoolean(Common.SF_SAVE_SHIPPING, false);
    }

    public static void clearShipping(Context context) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.remove(Common.SF_NAME);
        editor.remove(Common.SF_PHONE);
        editor.remove(Common.SF_ADDRESS);
        editor.remove(Common.SF_ZIP);
        editor.remove(Common.SF_CITY);
        editor.remove(Common.SF_SAVE_SHIPPING);
        editor.apply();
    }

    public static void clearPreferences(Context context) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.remove(Common.SF_EMAIL);
        editor.remove(Common.SF_PASSWORD);
        editor.remove(Common.SF_NAME);
        editor.remove(Common.SF_PHONE);
        editor.remove(Common.SF_ADDRESS);
        editor.remove(Common.SF_ZIP);
        editor.remove(Common.SF_CITY);
        editor.apply();
    }
}
