package com.shibedays.workoutplanner;

/**
 * Created by ttbot on 2/11/2018.
 */

public class Set {

    private String name;
    private String descrip;
    private int time;

    public Set(){
        name = "Default Set";
        descrip = "Default Descrip";
        time = 1000;
    }

    public Set(String name, String descrip, int time){
        this.name = name;
        this.descrip = descrip;
        this.time = time;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getDescrip(){
        return descrip;
    }
    public void setDescrip(String descrip){
        this.descrip = descrip;
    }

    public int getTime(){
        return time;
    }
    public void setTime(int time){
        this.time = time;
    }





}
