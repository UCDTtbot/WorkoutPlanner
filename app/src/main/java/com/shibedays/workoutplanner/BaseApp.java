package com.shibedays.workoutplanner;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.DebugUtils;
import android.util.Log;
import android.widget.Toast;

import com.shibedays.workoutplanner.db.AppDatabase;
import com.shibedays.workoutplanner.ui.MainActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;


public class BaseApp extends Application {


    private static Context context;
    private static boolean mIsDark;
    private static boolean mAdsDisabled;
    private static boolean mVibrateEnabled;
    private static boolean mThemeChanged;
    private static boolean mMainFirstRun = false;
    private static boolean mMyWorkFirstRun = false;
    private static boolean mFirstSetFragShown = true;



    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs != null) {
            mIsDark = prefs.getBoolean("dark_theme", false);
            mAdsDisabled = prefs.getBoolean("disable_ads", false);
            mVibrateEnabled = prefs.getBoolean("vibrate", true);

            if(mIsDark){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        context = getApplicationContext();

    }

    public static void setFirstRun(){
        mMainFirstRun = true;
        mMyWorkFirstRun = true;
        mFirstSetFragShown = false;
    }

    public static boolean isMainFirstRun(){ return mMainFirstRun; }
    public static boolean isMyWorkFirstRun(){ return mMyWorkFirstRun; }
    public static boolean isFirstSetFrag() { return mFirstSetFragShown; }

    public static void toggleFirstSetFrag() { mFirstSetFragShown = true; }
    public static void toggleMyWorkFirstRun() { mMyWorkFirstRun = false; }
    public static void toggleMainFirstRun() { mMainFirstRun = false; }


    public static boolean isDarkTheme(){
        return mIsDark;
    }
    public static void toggleTheme(boolean tog){
        mIsDark = tog;
        mThemeChanged = true;
    }

    public static boolean didThemeChange(){ return mThemeChanged; }
    public static void resetThemeBool(){ mThemeChanged = false;}

    public static boolean isVibrateEnabled(){ return mVibrateEnabled; }

    public static boolean areAdsDisabled(){ return mAdsDisabled; }
    public static void toggleAds(boolean tog){ mAdsDisabled = tog;}

    public AppDatabase getDatabase(){
        return AppDatabase.getDatabaseInstance(this);
    }

    public DataRepo getRepo(){
        return DataRepo.getInstance(getDatabase());
    }

    public static Context getAppContext () {
        return context;
    }


    //region CONVERTERS
    public static int convertToMillis(int[] time){
        return ((time[0] * 60) + time[1]) * 1000;
    }

    public static int convertToMillis(int min, int sec){
        return((min * 60) + sec) * 1000;
    }

    public static int[] convertFromMillis(int time){
        int[] newTime = {0, 0};
        newTime[0] = (int)(Math.floor(time/1000)/60);
        newTime[1] = ((time/1000) % 60);
        return newTime;
    }
    //endregion

    //region BOTTOM_SHEET_HELPERS
    // Result Types
    public static final int EDIT = 0;
    public static final int DELETE = 1;
    public static final int DUPLCIATE = 2;

    public static int getWrkBtmSheetRows(){
        return 2;
    }
    public static int getSetBtmSheetRows(){
        return 2;
    }

    public static ArrayList<String> getWrkBtmSheetNames(Context context){
        ArrayList<String> list = new ArrayList<>();
        list.add(context.getString(R.string.bottom_sheet_dup));
        list.add(context.getString(R.string.bottom_sheet_delete));
        return list;
    }
    public static ArrayList<String> getSetBtmSheetNames(Context context){
        ArrayList<String> list = new ArrayList<>();
        list.add(context.getString(R.string.bottom_sheet_edit));
        list.add(context.getString(R.string.bottom_sheet_delete));
        return list;
    }

    public static int[] getWrkBtmSheetICs(){
        int ics[] = new int[getWrkBtmSheetRows()];
        if(isDarkTheme()){
            ics[0] = R.drawable.ic_content_copy_white_24dp;
            ics[1] = R.drawable.ic_delete_white_24dp;
        }
        else {
            ics[0] = R.drawable.ic_content_copy_black_24dp;
            ics[1] = R.drawable.ic_delete_black_24dp;
        }
        return ics;
    }
    public static int[] getSetBtmSheetICs(){
        int ics[] = new int[getSetBtmSheetRows()];
        if(isDarkTheme()){
            ics[0] = R.drawable.ic_edit_white_24dp;
            ics[1] = R.drawable.ic_delete_white_24dp;
        } else {
            ics[0] = R.drawable.ic_edit_black_24dp;
            ics[1] = R.drawable.ic_delete_black_24dp;
        }
        return ics;
    }

    public static int[] getWrkBtmSheetResults(){
        int[] results= new int[getWrkBtmSheetRows()];
        results[0] = DUPLCIATE;
        results[1] = DELETE;
        return results;
    }

    public static int[] getSetBtmSheetResults(){
        int[] results= new int[getSetBtmSheetRows()];
        results[0] = EDIT;
        results[1] = DELETE;
        return results;
    }
    //endregion

    //region UTILITY

    public static String formatTime(int min, int sec){
        if(sec == 0){
            return String.format(Locale.US, "%d:%d%d", min, sec, 0);
        } else if ( sec < 10 ){
            return String.format(Locale.US, "%d:%d%d", min, 0, sec);
        } else {
            return String.format(Locale.US, "%d:%d", min, sec);
        }
    }

    public static String formatTime(int time){
        int[] t = convertFromMillis(time);
        int min = t[0], sec = t[1];
        if(sec == 0){
            return String.format(Locale.US, "%d:%d%d", min, sec, 0);
        } else if ( sec < 10 ){
            return String.format(Locale.US, "%d:%d%d", min, 0, sec);
        } else {
            return String.format(Locale.US, "%d:%d", min, sec);
        }
    }

    //endregion
}
