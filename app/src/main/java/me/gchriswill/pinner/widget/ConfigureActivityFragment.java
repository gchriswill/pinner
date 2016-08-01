package me.gchriswill.pinner.widget;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.util.ArrayList;

import me.gchriswill.pinner.R;
import me.gchriswill.pinner.User;

public class ConfigureActivityFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "ConfigActivityFragment";

    ConfigureActivityFragmentInterface configureActivityFragmentInterface;
    Preference widgetTypePrefs;
    ListPreference widgetProfilesPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_fragment);
    }

    public interface ConfigureActivityFragmentInterface{
        ArrayList<User> getProfileList();
        void setWidgetType(String type);
        void setWidgetProfile(String id);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof ConfigureActivityFragmentInterface){
            configureActivityFragmentInterface = (ConfigureActivityFragmentInterface) context;
        }else {
            throw new IllegalStateException("ConfigureActivityFragmentInterface not implemented...");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        widgetTypePrefs = findPreference(SettingsManager.PREF_SHORTCUTS_TYPES);
        widgetTypePrefs.setOnPreferenceChangeListener(this);

        widgetProfilesPrefs = (ListPreference) findPreference(SettingsManager.PREF_AVAILABLE_PROFILES);
        widgetProfilesPrefs.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference.getKey().equals(SettingsManager.PREF_SHORTCUTS_TYPES) ){
            Log.e(TAG, "onPreferenceChange: PREF_SHORTCUTS_TYPES --> " + newValue);
            //SettingsManager.setShortcutType(getContext(), (String) newValue );
            configureActivityFragmentInterface.setWidgetType((String) newValue);

            return true;
        }

        if (preference.getKey().equals(SettingsManager.PREF_AVAILABLE_PROFILES) ){
            Log.e(TAG, "onPreferenceChange: PREF_AVAILABLE_PROFILES --> " + newValue);
            //SettingsManager.setSelectedProfile(getContext(), (String) newValue );
            configureActivityFragmentInterface.setWidgetProfile((String) newValue);

            return true;
        }

        return false;
    }

    void getProfiles(){
        ArrayList<User> profiles = configureActivityFragmentInterface.getProfileList();
        ArrayList<CharSequence> usernames = new ArrayList<>();
        ArrayList<CharSequence> userIds = new ArrayList<>();

        for (User p : profiles){ usernames.add( p.displayName ); userIds.add(p.userId); }

        CharSequence[] arrayNames = new CharSequence[]{};
        CharSequence[] arrayIds = new CharSequence[]{};

        widgetProfilesPrefs.setEntries( usernames.toArray(arrayNames) );
        widgetProfilesPrefs.setEntryValues( userIds.toArray(arrayIds) );
    }
}
