package com.shibedays.workoutplanner.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.List;


@Dao
public interface WorkoutDao {
    @Query("SELECT * FROM workouts")
    LiveData<List<Workout>> getAll();

    @Query("SELECT * FROM workouts WHERE workoutType=:type")
    LiveData<List<Workout>> getTypedWorkouts(int type);

    @Query("SELECT * FROM workouts WHERE id=:id")
    LiveData<Workout> getWorkout(int id);

    @Query("SELECT * FROM workouts where name LIKE :name")
    Workout findWorkoutByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Workout> workouts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Workout workout);

    @Update
    void update(Workout workout);

    @Delete
    void delete(Workout workout);

    @Query("DELETE FROM workouts")
    void dalateAll();
}
