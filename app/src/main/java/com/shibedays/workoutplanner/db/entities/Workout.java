package com.shibedays.workoutplanner.db.entities;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.DESedeKeySpec;

@Entity(tableName = "workouts")
public class Workout{
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private int workoutID;
    private int numOfRounds;

    private int timeBetweenSets;
    private int timeBetweenRounds;

    private String name;

    @Ignore
    private List<Set> setList;
    @ColumnInfo(name = "sets")
    private String setListJSON;

    public Workout(){
    }
    public Workout(@NonNull int id, String name) {
        workoutID = id;
        this.name = name;
        numOfRounds = 1;
        setList = new ArrayList<Set>();
        timeBetweenSets = 10000;
        timeBetweenRounds = 30000;
    }
    public Workout(Workout workout){
        workoutID = workout.getWorkoutID();
        name = workout.getName();
        numOfRounds = workout.getNumOfRounds();
        setList = workout.getSetList();
        timeBetweenSets = workout.getTimeBetweenSets();
        timeBetweenRounds = workout.getTimeBetweenSets();
    }

    public int getWorkoutID(){
        return workoutID;
    }
    public void setWorkoutID(int id){workoutID = id;}

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

    public String toJSON(){
        Gson json = new Gson();
        return json.toJson(this);
    }
}