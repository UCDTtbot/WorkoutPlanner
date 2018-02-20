package com.shibedays.workoutplanner;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by ttbot on 2/20/2018.
 */

@Dao
public interface WorkoutDao {
    @Query("SELECT * FROM workouts")
    List<Workout> getAll();

    @Query("SELECT * FROM workouts where name LIKE :name")
    Workout findWorkoutByName(String name);

    @Query("SELECT * FROM workouts where workoutID like :id")
    Workout findWorkoutByID(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Workout> workouts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Workout workout);

    @Update
    void update(Workout workout);

    @Delete
    void delete(Workout workout);
}
