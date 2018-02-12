package com.shibedays.workoutplanner;


import java.util.ArrayList;
import java.util.List;

public class Workout {
    private int mWorkoutID;
    private int mNumOfSets;
    private int mNumOfRounds;

    private int mTimeBetweenSets;
    private int mTimeBetweenRounds;

    private String mName;

    private List<Set> mSetList;

    /**
     *
     * @param name
     */
    public Workout(int id, String name) {
        mWorkoutID = id;
        mName = name;
        mNumOfSets = 0;
        mNumOfRounds = 1;
        mSetList = new ArrayList<Set>();
        mTimeBetweenSets = 10000;
        mTimeBetweenRounds = 30000;
    }

    //region GET
    public int getmWorkoutID(){
        return mWorkoutID;
    }

    public int getmNumOfSets(){
        return mNumOfSets;
    }

    public int getmNumOfRounds(){
        return mNumOfRounds;
    }

    public int getmTimeBetweenSets(){
        return mTimeBetweenSets;
    }

    public int getmTimeBetweenRounds(){
        return mTimeBetweenRounds;
    }

    public String getmName(){
        return mName;
    }
    //endregion

    //region SET
    public void setmWorkoutID(int id){
        mWorkoutID = id;
    }

    public void setmNumOfRounds(int numRounds){
        mNumOfRounds = numRounds;
    }

    public void setmTimeBetweenSets(int timeBetweenSets){
        mTimeBetweenSets = timeBetweenSets;
    }

    public void setmTimeBetweenRounds(int timeBetweenRounds){
        mTimeBetweenRounds = timeBetweenRounds;
    }

    public void setmName(String name){
        mName = name;
    }
    //endregion

    public void addSet(Set set){
        mSetList.add(set);
        mNumOfSets++;
    }

    public void removeSet(Set set){
        mSetList.remove(mSetList.indexOf(set));
        mNumOfSets--;
    }
}