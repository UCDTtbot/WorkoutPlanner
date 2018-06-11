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
    private int workoutID;
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

    @Ignore
    private List<Set> setList;
    @ColumnInfo(name = "sets")
    private String setListJSON;

    public Workout(){
    }
    public Workout(int type, String name) {
        this.name = name;
        this.workoutType = type;
        numOfRounds = 1;
        setList = new ArrayList<Set>();
        timeBetweenSets = 10000;
        timeBetweenRounds = 30000;
        noRestFlag = false;
        noBreakFlag = false;
        isFavorite = false;
    }
    public Workout(int type, String name, List<Set> sets) {
        this.name = name;
        this.workoutType = type;
        numOfRounds = 1;
        setList = sets;
        timeBetweenSets = 10000;
        timeBetweenRounds = 30000;
        noRestFlag = false;
        noBreakFlag = false;
        isFavorite = false;
    }
    public Workout(Workout workout){
        name = workout.getName();
        workoutType = workout.workoutType;
        numOfRounds = workout.getNumOfRounds();
        setList = workout.getSetList();
        timeBetweenSets = workout.getTimeBetweenSets();
        timeBetweenRounds = workout.getTimeBetweenSets();
        noRestFlag = false;
        noBreakFlag = false;
        isFavorite = false;
    }

    public int getWorkoutID(){
        return workoutID;
    }
    public void setWorkoutID(int id){workoutID = id;}

    public int getWorkoutType(){ return workoutType; }
    public void setWorkoutType(int type) { workoutType = type; }

    public int getWorkoutImageId(){
        return setList == null || setList.size() <= 0 ? R.drawable.ic_fitness_black_24dp : setList.get(0).getSetImageId();
    }
    public void setWorkoutImageId(int id) { workoutImageId = id;}

    public int getNumOfSets(){
        if(setList != null) {
            return setList.size();
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

    public String getSetListJSON(){
        Gson gson = new Gson();
        return gson.toJson(setList);
    }
    public void setSetListJSON(String json){
        setListJSON = json;
        Gson gson = new Gson();
        setList = (List<Set>) gson.fromJson(setListJSON, new TypeToken<List<Set>>() {}.getType());
    }

    public List<Set> getSetList(){
        return setList;
    }

    public void addSet(Set set){
        setList.add(set);
    }
    public void addSets(List<Set> sets){
        setList.addAll(sets);
    }
    public void swapSets(int from, int to){
        //Log.d("WORKOUT", "Swapping");
        Set temp = setList.get(to);
        setList.set(to, setList.get(from));
        setList.set(from, temp);
        //Log.d("WORKOUT", "Swapped");
    }
    public void updateSet(Set set, int index){
        setList.set(index, set);
    }
    public void removeSet(Set set){
        setList.remove(setList.indexOf(set));
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