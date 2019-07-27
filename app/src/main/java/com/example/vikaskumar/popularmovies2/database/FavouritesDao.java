package com.example.vikaskumar.popularmovies2.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;



@Dao
public interface FavouritesDao {

    @Query("SELECT * FROM favourites ORDER BY id")
    LiveData<List<FavouritesEntry>> loadAllFavourites();

    @Insert
    void insertFavourite(FavouritesEntry entry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFavourite(FavouritesEntry entry);

    @Delete
    void deleteFavourite(FavouritesEntry entry);

    @Query("SELECT * FROM favourites WHERE id = :id")
    LiveData<FavouritesEntry> loadFavouriteById(int id);
}
