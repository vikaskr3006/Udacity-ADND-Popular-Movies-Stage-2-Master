package com.example.vikaskumar.popularmovies2;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.vikaskumar.popularmovies2.database.AppDatabase;
import com.example.vikaskumar.popularmovies2.database.FavouritesEntry;

public class FavouriteViewModel extends ViewModel {

    private LiveData<FavouritesEntry> favourite;

    public FavouriteViewModel(AppDatabase database, int favouriteId) {
        favourite = database.favouritesDao().loadFavouriteById(favouriteId);
    }

    public LiveData<FavouritesEntry> getFavourite() {
        return favourite;
    }
}
