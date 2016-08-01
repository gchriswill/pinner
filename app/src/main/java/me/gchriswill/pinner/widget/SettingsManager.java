package me.gchriswill.pinner.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsManager {

    public static final String PREF_SHORTCUTS_TYPES = "me.gchriswill.pinner.PREF_SHORTCUTS_TYPES";
    public static final String PREF_AVAILABLE_PROFILES = "me.gchriswill.pinner.PREF_AVAILABLE_PROFILES";

    public static String getShortcutType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(PREF_SHORTCUTS_TYPES, "INVALID_SHORTCUT");
    }

    public static String getSelectedProfile(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(PREF_AVAILABLE_PROFILES, "INVALID_PROFILE");
    }

    public static void setShortcutType(Context context, String type){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(PREF_SHORTCUTS_TYPES, type);
        editor.apply();
    }

    public static void setSelectedProfile(Context context, String profileId){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(PREF_AVAILABLE_PROFILES, profileId);
        editor.apply();
    }
}