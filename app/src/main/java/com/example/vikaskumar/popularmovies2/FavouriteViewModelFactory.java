package com.example.vikaskumar.popularmovies2;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.vikaskumar.popularmovies2.database.AppDatabase;

public class FavouriteViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int mFavouriteId;

    public FavouriteViewModelFactory(AppDatabase database, int favouriteId) {
        mDb = database;
        mFavouriteId = favouriteId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {

        return (T) new FavouriteViewModel(mDb, mFavouriteId);
    }
}