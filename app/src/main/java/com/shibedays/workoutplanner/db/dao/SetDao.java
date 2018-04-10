package com.shibedays.workoutplanner.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.shibedays.workoutplanner.db.entities.Set;

import java.util.List;

@Dao
public interface SetDao {
    @Query("SELECT * FROM sets")
    LiveData<List<Set>> getll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Set set);

    @Update
    void update(Set set);

    @Delete
    void delete(Set set);
}
