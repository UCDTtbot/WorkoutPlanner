package com.shibedays.workoutplanner.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import android.text.TextUtils;

import com.shibedays.workoutplanner.BaseApp;

import java.util.Objects;

/**
 * Created by ttbot on 2/11/2018.
 */
@Entity(tableName = "sets")
public class Set {

    // TYPE CONSTANTS
    public static final int CARDIO = 0;
    public static final int UPPER_BODY = 1;
    public static final int LOWER_BODY = 2;
    public static final int CORE = 3;
    public static final int FLEXIBILITY = 4;
    public static final int USER_CREATED = 5;
    public static final int OTHER = 6;
    public static final String[] TYPES = {"Cardio",  "Upper Body" , "Lower Body",  "Core" ,"Flexibility",  "User Created",  "Other"};
    //workout info coming from go4life.nia.nih.gov
    //https://lifehacker.com/5839197/how-to-get-a-full-body-workout-with-nothing-but-your-body
    //make sure to source more info

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int setId;
    private int setType;
    @Ignore
    private int setImageId;
    private String setImageName;
    private String name;
    private String descrip;
    private int time;
    private String URL;

    public Set(){
        name = "Default Set";
        descrip = "Default Descrip";
        time = 1000;
    }

    public Set(int id, String name, String descrip, int type, int time, String image){
        this.setId = id;
        this.name = name;
        this.descrip = descrip;
        this.setType = type;
        this.time = time;
        this.setImageName = image;
    }

    public Set(String name, String descrip, int type, int time, String image){
        this.name = name;
        this.setType = type;
        this.descrip = descrip;
        this.time = time;
        setImageName = image;
    }

    public Set(String name, String descrip, int type, int time, String image, String url){
        this.name = name;
        this.setType = type;
        this.descrip = descrip;
        this.time = time;
        this.setImageName = image;
        this.URL = url;
    }

    public Set(int id, String name, String descrip, int type, int time, int image){
        this.setId = id;
        this.name = name;
        this.descrip = descrip;
        this.setType = type;
        this.time = time;
        setImageById(image);
    }

    public Set(int id, String name, String descrip, int type, int time, int image, String url){
        this.setId = id;
        this.name = name;
        this.descrip = descrip;
        this.setType = type;
        this.time = time;
        this.URL = url;
        setImageById(image);
    }

    public Set(String name, String descrip, int type, int time, int image){
        this.name = name;
        this.setType = type;
        this.descrip = descrip;
        this.time = time;
        setImageById(image);
    }

    public int getSetId(){ return setId; }
    public void setSetId(int id){ setId = id; }

    public int getSetType(){ return setType; }
    public void setSetType(int type){ setType = type; }

    public int getSetImageId() {
        return BaseApp.getAppContext().getResources().getIdentifier(
                setImageName,
                "drawable",
                BaseApp.getAppContext().getPackageName());
    }
    public void setImageById(int id){
        setImageName = BaseApp.getAppContext().getResources().getResourceEntryName(id);
    }
    public String getSetImageName() { return setImageName; }
    public void setSetImageName(String name) { setImageName = name; }

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

    public String getURL(){
        if(TextUtils.isEmpty(URL)){
            return "https://www.google.com/search?q=" + name;
        }
        else
            return URL;
    }
    public void setURL(String url){
        URL = url;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof Set)) return false;

        Set set = (Set) obj;
        return set.setId == this.setId &&
                set.setImageId == this.setImageId &&
                set.name.equals(this.name) &&
                set.descrip.equals(this.descrip) &&
                set.time == this.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.setId);
    }
}
