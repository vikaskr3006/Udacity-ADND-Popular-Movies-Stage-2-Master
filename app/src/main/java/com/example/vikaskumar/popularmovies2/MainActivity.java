package com.example.vikaskumar.popularmovies2;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.vikaskumar.popularmovies2.database.AppDatabase;
import com.example.vikaskumar.popularmovies2.database.FavouritesEntry;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ImageAdapter imageAdapter;
    private TextView mActivityTitleTv;
    private Spinner mSortOptionSpinner;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;
    private GridView gridView;
    private ImageView errorIv;
    private Button retryButton;
    private ArrayList<Movie> movieArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_title));
        retryButton = findViewById(R.id.retry_button);
        gridView = findViewById(R.id.grid_view);
        errorIv = findViewById(R.id.error_iv);
        mActivityTitleTv = findViewById(R.id.main_activity_title_tv);
        mSortOptionSpinner = findViewById(R.id.sort_option_spinner);
        gridView.setOnItemClickListener(moviePosterClickListener);
        mSortOptionSpinner.setOnItemSelectedListener(sortOptionListener);
        mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sorting_options, android.R.layout.simple_spinner_item);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortOptionSpinner.setAdapter(mSpinnerAdapter);

        if(savedInstanceState == null || !savedInstanceState.containsKey(getString(R.string.parcel_movie))){
            String sortType = getSortType();
            if(sortType.equals(getString(R.string.sort_favourites))){
                setMainViewForFavourites();
            }else{
                getMoviesFromTMDB(sortType);
            }
        }else{
            mActivityTitleTv.setText(savedInstanceState.getString(getString(R.string.activity_title_text_key)));
            movieArrayList = savedInstanceState.getParcelableArrayList(getString(R.string.parcel_movie));
            gridView.setAdapter(new ImageAdapter(this, movieArrayList));
        }
    }

    private void setMainViewForFavourites() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getFavourites().observe(this, new Observer<List<FavouritesEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavouritesEntry> favouritesEntries) {
                if (favouritesEntries != null && favouritesEntries.size() != 0) {
                    mActivityTitleTv.setText(getString(R.string.activity_title_favourites));
                    updateSharedPrefs(getString(R.string.sort_favourites));

                    ArrayList<Movie> moviesList = new ArrayList<>();
                    for (int j = 0; j < favouritesEntries.size(); j++) {
                        Movie movie = new Movie();
                        FavouritesEntry favourite = favouritesEntries.get(j);
                        movie.setMovieId(favourite.getId());
                        movie.setOriginalTitle(favourite.getTitle());
                        movie.setReleaseDate(favourite.getRelease_date());
                        movie.setVoteAverage(favourite.getRating());
                        movie.setOverview(favourite.getSynopsis());
                        movie.setPosterPath(favourite.getPoster_path());
                        moviesList.add(movie);
                    }
                    imageAdapter = new ImageAdapter(MainActivity.this, moviesList);
                    gridView.setAdapter(imageAdapter);
                }
            }
        });
    }

    private final GridView.OnItemClickListener moviePosterClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intentToStartDetailActivity = new Intent(MainActivity.this, DetailActivity.class);
            Movie movie = (Movie) parent.getItemAtPosition(position);

            intentToStartDetailActivity.putExtra(getString(R.string.parcel_movie), movie);
            startActivity(intentToStartDetailActivity);
        }
    };

    private final AdapterView.OnItemSelectedListener sortOptionListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String itemAtPos = (String) parent.getItemAtPosition(position);
            performSortingOperation(itemAtPos);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        int numMovieObjects = gridView.getCount();
        if (numMovieObjects > 0) {
            ArrayList<Movie> movies = new ArrayList<>();
            for (int i = 0; i < numMovieObjects; i++) {
                movies.add((Movie) gridView.getItemAtPosition(i));
            }
            outState.putParcelableArrayList(getString(R.string.parcel_movie), movies);
        }
        outState.putString(getString(R.string.activity_title_text_key), String.valueOf(mActivityTitleTv.getText()));

        super.onSaveInstanceState(outState);
    }

    private void performSortingOperation(String itemAtPos) {
        FetchMovies fetchMovies = new FetchMovies();
        String[] array = getResources().getStringArray(R.array.sorting_options);
        int i = 0;
        if(itemAtPos.equals(array[1])){
            i = 1;
        }else if(itemAtPos.equals(array[2])){
            i = 2;
        }else if(itemAtPos.equals(array[3])){
            i = 3;
        }

        switch (i){
            case 1 :
                mActivityTitleTv.setText(getString(R.string.activity_title_popular));
                updateSharedPrefs(getString(R.string.sort_popularity));
                fetchMovies.execute(getSortType());
                break;
            case 2 :
                mActivityTitleTv.setText(getString(R.string.activity_title_top_rated));
                updateSharedPrefs(getString(R.string.sort_top_rated));
                fetchMovies.execute(getSortType());
                break;
            case 3 :
                setMainViewForFavourites();
                break;
            default :
                break;
        }
    }

    private void getMoviesFromTMDB(String sortType){
        setActivityTitleText(sortType);
        if(new MovieNetworkHelper().isNetworkAvailable(MainActivity.this)) {
            hideErrorView();
            FetchMovies fetchMovies = new FetchMovies();
            fetchMovies.execute(sortType);
        }else{
            showErrorView();
            retryButton.setOnClickListener(retryButtonClickListener(sortType));
        }
    }

    private View.OnClickListener retryButtonClickListener(final String sortType) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMoviesFromTMDB(sortType);
            }
        };
    }

    private void setActivityTitleText(String sortType) {
        if(sortType.equals(getString(R.string.sort_popularity))){
            mActivityTitleTv.setText(getString(R.string.activity_title_popular));
        }else if(sortType.equals(getString(R.string.sort_top_rated))){
            mActivityTitleTv.setText(getString(R.string.activity_title_top_rated));
        }
    }

    private void hideErrorView(){
        gridView.setVisibility(View.VISIBLE);
        errorIv.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
    }

    private void showErrorView() {
        gridView.setVisibility(View.GONE);
        errorIv.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.VISIBLE);
    }

    private String getSortType() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString(getString(R.string.pref_sort_type_key), getString(R.string.sort_popularity));
    }

    private void updateSharedPrefs(String sortType) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.pref_sort_type_key), sortType);
        editor.apply();
    }

    public class FetchMovies extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            String baseUrl = getString(R.string.movie_base_url);
            String apiKeyParam = "?api_key=";
            String sortType = strings[0];
            String urlString = baseUrl + sortType + apiKeyParam + BuildConfig.THE_MOVIE_DB_API_KEY;
            return new MovieNetworkHelper().getJsonResponseFromUrl(urlString);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {

            Movie movie = new Movie(MainActivity.this, jsonResponse);
            movieArrayList = movie.getMoviesList();

            imageAdapter = new ImageAdapter(MainActivity.this, movieArrayList);
            gridView.setAdapter(imageAdapter);
        }
    }
}
