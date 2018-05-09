package com.shibedays.workoutplanner.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * Created by ttbot on 2/11/2018.
 */
@Entity(tableName = "sets")
public class Set {

    // TYPE CONSTANTS
    public static final int USER_CREATED = 0;
    public static final int ENDURANCE = 1;
    public static final int STRENGTH = 2;
    public static final int BALANCE = 3;
    public static final int FLEXIBILITY = 4;
    public static final int OTHER = 5;
    public static final String[] TYPES = {"User Created", "Endurance", "Strength", "Balance", "Flexibility", "Other"};
    //workout info coming from go4life.nia.nih.gov
    //make sure to source more info

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int setId;
    private int setType;
    private String name;
    private String descrip;
    private int time;

    public Set(){
        name = "Default Set";
        descrip = "Default Descrip";
        time = 1000;
    }

    public Set(String name, String descrip, int type, int time){
        this.name = name;
        this.setType = type;
        this.descrip = descrip;
        this.time = time;
    }

    public int getSetId(){ return setId; }
    public void setSetId(int id){ setId = id; }

    public int getSetType(){ return setType; }
    public void setSetType(int type){ setType = type; }

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

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof Set)) return false;

        Set set = (Set) obj;
        return set.setId == this.setId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.setId);
    }
}
