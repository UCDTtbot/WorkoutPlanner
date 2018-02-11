package com.shibedays.workoutplanner;

/**
 * Created by ttbot on 2/11/2018.
 */

public class Set {

    private String mName;
    private String mDescrip;
    private int mTime;

    public Set(){
        mName = "Default Set";
        mDescrip = "Default Descrip";
        mTime = 1000;
    }

    public Set(String name, String descrip, int time){
        mName = name;
        mDescrip = descrip;
        mTime = time;
    }

    //region GET
    public String getmName(){
        return mName;
    }

    public String getmDescrip(){
        return mDescrip;
    }

    public int getmTime(){
        return mTime;
    }
    //endregion

    //region SET
    public void setmName(String name){
        mName = name;
    }

    public void setmDescrip(String descrip){
        mDescrip = descrip;
    }

    public void setmTime(int time){
        mTime = time;
    }
    //endregion
}
