package com.shibedays.workoutplanner;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.shibedays.workoutplanner.db.AppDatabase;

import java.lang.reflect.Method;
import java.util.ArrayList;


public class BaseApp extends Application {

    public AppDatabase getDatabase(){
        return AppDatabase.getDatabaseInstance(this);
    }

    public DataRepo getRepo(){
        return DataRepo.getInstance(getDatabase());
    }

    public static void showDebugDBAddressLogToast(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
                Method getAddressLog = debugDB.getMethod("getAddressLog");
                Object value = getAddressLog.invoke(null);
                Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
            } catch (Exception ignore) {

            }
        }
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
        ics[0] = R.drawable.ic_content_copy_black_24dp;
        ics[1] = R.drawable.ic_delete_black_24dp;
        return ics;
    }
    public static int[] getSetBtmSheetICs(){
        int ics[] = new int[getSetBtmSheetRows()];
        ics[0] = R.drawable.ic_edit_black_24dp;
        ics[1] = R.drawable.ic_delete_black_24dp;
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

}
