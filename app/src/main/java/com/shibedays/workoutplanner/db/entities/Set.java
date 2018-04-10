package com.shibedays.workoutplanner.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by ttbot on 2/11/2018.
 */

@Entity(tableName = "sets")
public class Set {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int setId;
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

    public int getSetId(){ return setId; }
    public void setSetId(int id){ setId = id; }


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
