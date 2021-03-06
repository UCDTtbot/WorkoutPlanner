package com.shibedays.workoutplanner.ui.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.BuildConfig;
import com.shibedays.workoutplanner.R;

import de.psdev.licensesdialog.LicensesDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference test = findPreference("rate_me");
        if(test != null){
            Log.d("Settings", "Got the correct pref");
        } else
            Log.d("Settings", "Couldn't find pref");

        SwitchPreference theme = (SwitchPreference)findPreference("dark_theme");
        theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference instanceof SwitchPreference) {
                    if (TextUtils.equals(preference.getKey(), "dark_theme")) {
                        Log.d("Settings", "Got switched for dark theme");
                        if((boolean)newValue){
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            BaseApp.toggleTheme(true);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            BaseApp.toggleTheme(false);
                        }
                    }
                    getActivity().recreate();
                }
                return true;
            }
        });

        SwitchPreference vibrate = (SwitchPreference)findPreference("vibrate");
        vibrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference instanceof SwitchPreference) {
                    if (TextUtils.equals(preference.getKey(), "vibrate")) {
                        if((boolean)newValue){
                            BaseApp.toggleVibration(true);
                        } else {
                            BaseApp.toggleVibration(false);
                        }
                    }
                }
                return true;
            }
        });

        final SwitchPreference ads = (SwitchPreference)findPreference("disable_ads");
        ads.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(ads.isChecked()){   // Enable Ads
                    BaseApp.toggleAds(false);
                    return true;
                } else {
                    if (getActivity() != null) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Disable Ads")
                                .setMessage("Ads support the development of this app. However, I understand ads are annoying. Please consider donating if you enjoy the app, or leave ads enabled. Click Ok to disable ads.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { // Disable Ads
                                        ads.setChecked(true);
                                        BaseApp.toggleAds(true);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { // Enable Ads
                                        ads.setChecked(false);
                                        BaseApp.toggleAds(false);
                                    }
                                })
                                .show();
                    }
                }
                return false;
            }
        });

        Preference licenses = findPreference("libraries");
        licenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(getActivity() != null) {
                    new LicensesDialog.Builder(getActivity())
                            .setNotices(R.raw.notices)
                            .setTitle("Open Sourced Libraries")
                            .setIncludeOwnLicense(true)
                            .build()
                            .show();
                }
                return false;
            }
        });

        Preference about = findPreference("about");
        about.setSummary("Version: " + BuildConfig.VERSION_NAME);

        Preference legal = findPreference("legal");
        legal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(getActivity() != null) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Credits")
                            .setMessage(R.string.credits)
                            .setPositiveButton("Close", null)
                            .show();
                }
                return false;
            }
        });


        /*
        Preference donate = findPreference("donate");
        donate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(getActivity() != null) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Donate a coffee!")
                            .setMessage("Clicking ok will open your web browser, allowing you to donate.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri uri = Uri.parse("https://www.paypal.me/shibedays");
                                    Intent openBrowser = new Intent(Intent.ACTION_VIEW, uri);
                                    // To count with Play market backstack, After pressing back button,
                                    // to taken back to our application, we need to add following flags to intent.
                                    openBrowser.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                    try {
                                        startActivity(openBrowser);
                                    } catch (ActivityNotFoundException e) {
                                        Log.e("SETTING_FRAGMENT", e.toString());
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
                return false;            }
        });
        */

        Preference rate = findPreference("rate_me");
        rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(getActivity() != null) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Rate The App")
                            .setMessage("Clicking ok will open the Google Play Store allowing you to leave a rating and feedback.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri uri = Uri.parse("market://details?id=" + getContext().getPackageName());
                                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                    // To count with Play market backstack, After pressing back button,
                                    // to taken back to our application, we need to add following flags to intent.
                                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                    try {
                                        startActivity(goToMarket);
                                    } catch (ActivityNotFoundException e) {
                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("http://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
                return false;
            }
        });

       // bindPreferenceSummaryToValue(findPreference("voice_type"));
    }





    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }



}
