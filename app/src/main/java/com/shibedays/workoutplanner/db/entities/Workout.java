package com.shibedays.workoutplanner.db.entities;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shibedays.workoutplanner.R;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.DESedeKeySpec;

@Entity(tableName = "workouts")
public class Workout{

    //region TYPE_CONSTANTS
    public static final int CARDIO = 0;
    public static final int STRENGTH = 1;
    public static final int FLEXIBILITY = 2;
    public static final int USER_CREATED = 3;
    public static final int OTHER = 4;
    public static final String[] TYPES = { "Cardio Workouts", "Strength Training", "Flexibility", "Custom", "Other"};
    //endregion

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long workoutID;
    private int workoutType;
    private int workoutImageId;
    private int numOfRounds;

    private int timeBetweenSets;
    private int timeBetweenRounds;
    private boolean noRestFlag;
    private boolean noBreakFlag;
    @ColumnInfo(name = "favorite")
    private boolean isFavorite;

    private String name;

    private List<Long> setIds;

    public Workout(){
    }
    public Workout(int type, String name, int imageId) {
        this.name = name;
        this.workoutType = type;
        numOfRounds = 1;
        setIds = new ArrayList<Long>();
        workoutImageId = imageId;
        timeBetweenSets = 10000;
        timeBetweenRounds = 30000;
        noRestFlag = false;
        noBreakFlag = false;
        isFavorite = false;
    }
    public Workout(int type, String name, List<Long> sets, int imageId) {
        this.name = name;
        this.workoutType = type;
        numOfRounds = 1;
        setIds = sets;
        timeBetweenSets = 10000;
        timeBetweenRounds = 30000;
        workoutImageId = imageId;
        noRestFlag = false;
        noBreakFlag = false;
        isFavorite = false;
    }
    public Workout(Workout workout){
        name = workout.getName();
        workoutType = workout.workoutType;
        numOfRounds = workout.getNumOfRounds();
        setIds = workout.getSetList();
        workoutImageId = workout.getWorkoutImageId();
        timeBetweenSets = workout.getTimeBetweenSets();
        timeBetweenRounds = workout.getTimeBetweenSets();
        noRestFlag = false;
        noBreakFlag = false;
        isFavorite = false;
    }

    public long getWorkoutID(){
        return workoutID;
    }
    public void setWorkoutID(int id){workoutID = id;}

    public int getWorkoutType(){ return workoutType; }
    public void setWorkoutType(int type) { workoutType = type; }

    public int getWorkoutImageId(){
        return workoutImageId;
    }
    public void setWorkoutImageId(int id) { workoutImageId = id;}

    public int getNumOfSets(){
        if(setIds != null) {
            return setIds.size();
        } else {
            return 0;
        }
    }

    public int getNumOfRounds(){
        return numOfRounds;
    }
    public void setNumOfRounds(int numRounds){
        numOfRounds = numRounds;
    }

    public int getTimeBetweenSets(){
        return timeBetweenSets;
    }
    public void setTimeBetweenSets(int timeBetweenSets){
        this.timeBetweenSets = timeBetweenSets;
    }

    public int getTimeBetweenRounds(){
        return timeBetweenRounds;
    }
    public void setTimeBetweenRounds(int timeBetweenRounds){
        this.timeBetweenRounds = timeBetweenRounds;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public boolean getNoRestFlag(){
        return noRestFlag;
    }
    public void setNoRestFlag(boolean flag){
        noRestFlag = flag;
    }

    public boolean getNoBreakFlag(){
        return noBreakFlag;
    }
    public void setNoBreakFlag(boolean flag){
        noBreakFlag = flag;
    }

    public boolean getIsFavorite(){ return isFavorite; }
    public void setIsFavorite(boolean favorite) { isFavorite = favorite; }


    public List<Long> getSetList(){
        return setIds;
    }

    public void addSet(long setId){
        if(!setIds.contains(setId)) {
            setIds.add(setId);
        }
    }
    public void addSets(List<Long> sets){
        for(long i : sets){
            if(!setIds.contains(i)){
                setIds.add(i);
            }
        }
    }
    public void swapSets(int from, int to){
        //Log.d("WORKOUT", "Swapping");
        long temp = setIds.get(to);
        setIds.set(to, setIds.get(from));
        setIds.set(from, temp);
        //Log.d("WORKOUT", "Swapped");
    }
    public void updateSet(long id, int index){
        setIds.set(index, id);
    }
    public void removeSet(int id){
        setIds.remove(setIds.indexOf(id));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Workout){
            Workout workout = (Workout)obj;
            return (this.workoutID == workout.getWorkoutID());
        } else {
            return false;
        }
    }
}