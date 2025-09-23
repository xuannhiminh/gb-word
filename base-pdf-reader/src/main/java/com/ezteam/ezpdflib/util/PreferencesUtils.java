package com.ezteam.ezpdflib.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.text.TextUtils;

import com.ezteam.ezpdflib.model.AnnotationValue;
import com.google.gson.Gson;

public class PreferencesUtils {

    private static SharedPreferences sharedPreferences;

    /**
     * call in application class
     */
    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor editor() {
        return sharedPreferences.edit();
    }

    /**
     * Boolean
     */
    public static void putBoolean(String key, boolean value) {
        editor().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defaultvalue) {
        return sharedPreferences.getBoolean(key, defaultvalue);
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }


    /**
     * String
     */
    public static void putString(String key, String value) {
        editor().putString(key, value).apply();
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Integer
     */
    public static void putInteger(String key, int value) {
        editor().putInt(key, value).apply();
    }

    public static int getInteger(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public static int getInteger(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Long
     */
    public static void putLong(String key, long value) {
        editor().putLong(key, value).apply();
    }

    public static long getLong(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    public static long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public static void setAnnotation(AnnotationValue inkAnnotation, String key) {
        String gson = new Gson().toJson(inkAnnotation);
        putString(key, gson);
    }

    public static AnnotationValue getAnnotation(String key) {
        String data = getString(key);
        if (TextUtils.isEmpty(data)) {
            if (Config.PreferencesKey.inkValue.equals(key))
                return new AnnotationValue("#FF0000", 10, 0);
            if (Config.PreferencesKey.unlineValue.equals(key))
                return new AnnotationValue("#2b51bc", 1, 0);
            if (Config.PreferencesKey.highLighValue.equals(key))
                return new AnnotationValue("#ffee58", 1, 0);
            if (Config.PreferencesKey.strikeValue.equals(key))
                return new AnnotationValue("#FF0000", 1, 0);
        }
        return new Gson().fromJson(data, AnnotationValue.class);
    }
}
