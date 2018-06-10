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
    LiveData<List<Set>> getAll();

    @Query("SELECT * FROM sets WHERE setType=:type")
    LiveData<List<Set>> getTypedSets(int type);

    @Query("SELECT * FROM sets WHERE id=:id")
    LiveData<Set> getSet(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Set set);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<Set> sets);

    @Update
    void update(Set set);

    @Delete
    void delete(Set set);
}
