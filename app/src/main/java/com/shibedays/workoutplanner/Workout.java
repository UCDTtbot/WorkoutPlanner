package com.shibedays.workoutplanner;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "workouts")
public class Workout{
    @PrimaryKey
    private int workoutID;
    private int numOfSets;
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
    public Workout(int id, String name) {
        workoutID = id;
        this.name = name;
        numOfSets = 0;
        numOfRounds = 1;
        setList = new ArrayList<Set>();
        timeBetweenSets = 10000;
        timeBetweenRounds = 30000;
    }
    public Workout(Workout workout){
        workoutID = workout.getWorkoutID();
        name = workout.getName();
        numOfSets = workout.getNumOfSets();
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
        return numOfSets;
    }
    public void setNumOfSets(int sets){
        numOfSets = sets;
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
    public void setSetListJSON(String json){setListJSON = json;}

    public List<Set> getSetList(){
        return setList;
    }
    public void setSetList(List<Set> sets){setList = sets;}

    public void addSet(Set set){
        setList.add(set);
        numOfSets++;
    }
    public void removeSet(Set set){
        setList.remove(setList.indexOf(set));
        numOfSets--;
    }
}