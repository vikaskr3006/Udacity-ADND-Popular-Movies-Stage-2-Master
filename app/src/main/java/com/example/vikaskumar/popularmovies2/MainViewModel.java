package com.example.vikaskumar.popularmovies2;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.example.vikaskumar.popularmovies2.database.AppDatabase;
import com.example.vikaskumar.popularmovies2.database.FavouritesEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<FavouritesEntry>> favourites;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        favourites = database.favouritesDao().loadAllFavourites();
    }

    public LiveData<List<FavouritesEntry>> getFavourites() {
        return favourites;
    }
}