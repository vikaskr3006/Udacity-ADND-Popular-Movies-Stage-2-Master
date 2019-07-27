package com.example.vikaskumar.popularmovies2;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.vikaskumar.popularmovies2.database.AppDatabase;
import com.example.vikaskumar.popularmovies2.database.FavouritesEntry;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class DetailActivity extends AppCompatActivity {

    private final String LOG_TAG = DetailActivity.class.getSimpleName();
    private ImageView mPosterIv;
    private TextView mRatingLabelTv, mReleaseDateLabelTv;
    private TextView mDetailActivityTitleTv, mMovieTitleTv, mRatingTv, mReleaseDateTv, mSynopsisTv;
    private Button mFavouriteButton;

    private LinearLayout ll;

    private AppDatabase mDb;
    private Movie movie;

    private int trailer_count, review_count;
    private String[] youtube_ids;
    private JSONArray reviews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_detail);
        setTitle(getString(R.string.app_title));
        ll = (LinearLayout) findViewById(R.id.ll);
        mDetailActivityTitleTv = findViewById(R.id.detail_activity_title_tv);
        mPosterIv = findViewById(R.id.poster_iv);
        mMovieTitleTv = findViewById(R.id.movie_title_tv);
        mRatingLabelTv = findViewById(R.id.rating_label_tv);
        mRatingTv = findViewById(R.id.rating_tv);
        mReleaseDateLabelTv = findViewById(R.id.release_date_label_tv);
        mReleaseDateTv = findViewById(R.id.release_date_tv);
        mSynopsisTv = findViewById(R.id.synopsis_tv);
        mFavouriteButton = findViewById(R.id.favourite_button);
        mFavouriteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                String buttonText = button.getText().toString();
                if(buttonText.equals("Favourite")){
                    addToFavorites();
                }else if(buttonText.equals("Unfavourite")){
                    removeFromFavourite();
                }

            }
        });

        mDb = AppDatabase.getInstance(getApplicationContext());


        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.parcel_movie))){
            movie = intent.getParcelableExtra(getString(R.string.parcel_movie));
            String movieTitle = movie.getOriginalTitle();
            mDetailActivityTitleTv.setText(getString(R.string.detail_activity_title));
            Picasso.with(getBaseContext()).load(movie.getPosterPath())
                    .resize(getResources().getInteger(R.integer.movie_poster_w185_width),getResources().getInteger(R.integer.movie_poster_w185_height))
                    .error(R.drawable.network_error).placeholder(R.drawable.loading_image).into(mPosterIv);
            mRatingTv.setText(movie.getVoteAverage().toString() + "/10");
            mReleaseDateTv.setText(movie.getReleaseDate());
            mMovieTitleTv.setText(movieTitle);
            mSynopsisTv.setText(movie.getOverview());
        }

        FavouriteViewModelFactory viewModelFactory = new FavouriteViewModelFactory(mDb, movie.getMovieId());
        final FavouriteViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(FavouriteViewModel.class);
        viewModel.getFavourite().observe(this, new Observer<FavouritesEntry>() {
            @Override
            public void onChanged(@Nullable FavouritesEntry favouritesEntry) {
                viewModel.getFavourite().removeObserver(this);
                if(favouritesEntry != null){
                    mFavouriteButton.setText("Unfavourite");
                }else{
                    mFavouriteButton.setText("Favourite");
                }

            }
        });




        FetchTrailersAndReviews trailersAndReviews = new FetchTrailersAndReviews();
        trailersAndReviews.execute();
    }

    private void addToFavorites(){
        mFavouriteButton.setText("Unfavourite");

        final FavouritesEntry favouriteEntry = new FavouritesEntry(movie.getMovieId(), movie.getOriginalTitle(), movie.getOverview()
                , movie.getVoteAverage(), movie.getReleaseDate(), movie.getPosterPath());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.favouritesDao().insertFavourite(favouriteEntry);
            }
        });
        Log.d(LOG_TAG, "Exiting from addToFavourite's method.");
    }

    private void removeFromFavourite(){
        mFavouriteButton.setText("Favourite");

        final FavouritesEntry favouriteEntry = new FavouritesEntry(movie.getMovieId(), movie.getOriginalTitle(), movie.getOverview()
                , movie.getVoteAverage(), movie.getReleaseDate(), movie.getPosterPath());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.favouritesDao().deleteFavourite(favouriteEntry);
            }
        });
    }

    public class FetchTrailersAndReviews extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {

            String baseUrl = getString(R.string.movie_base_url);
            String videosParam = "/videos";
            String reviewsParam = "/reviews";
            String apiKeyParam = "?api_key=";
            String trailerUrlString = baseUrl + Integer.toString(movie.getMovieId())
                    + videosParam + apiKeyParam + BuildConfig.THE_MOVIE_DB_API_KEY;
            String trailerJsonResponse = new MovieNetworkHelper().getJsonResponseFromUrl(trailerUrlString);

            String reviewsUrlString = baseUrl + Integer.toString(movie.getMovieId())
                    + reviewsParam + apiKeyParam + BuildConfig.THE_MOVIE_DB_API_KEY;
            String reviewsJsonResponse = new MovieNetworkHelper().getJsonResponseFromUrl(reviewsUrlString);

            String[] jsonResponse = {trailerJsonResponse,reviewsJsonResponse};

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String[] jsonResponse) {

            getVideosFromJsonStr(jsonResponse[0]);
            getReviewsFromJsonStr(jsonResponse[1]);

            setTrailersView();
            setReviewsView();
        }

        private void getVideosFromJsonStr(String trailersJsonStr) {
            if(trailersJsonStr != null) {
                try {
                    JSONObject main = new JSONObject(trailersJsonStr);
                    String results = main.getString("results");
                    JSONArray trailers = new JSONArray(results);
                    trailer_count = trailers.length();
                    Log.d(LOG_TAG, "Number of Trailers:" + trailer_count);

                    //Ensure there is at least one trailer
                    if (trailer_count != 0) {
                        youtube_ids = new String[trailer_count];
                        for (int i = 0; i < trailer_count; i++) {
                            JSONObject obj = trailers.getJSONObject(i);
                            youtube_ids[i] = obj.getString("key");
                        }
                    }
                }catch (JSONException e) {
                    Log.e(LOG_TAG, "Error occurred", e);
                }
            }
        }
    }

    private void getReviewsFromJsonStr(String reviewsJsonStr) {
        if(reviewsJsonStr != null){
            try {
                JSONObject main = new JSONObject(reviewsJsonStr);
                String results = main.getString("results");
                reviews = new JSONArray(results);
                review_count = main.getInt("total_results");
                Log.d(LOG_TAG, "Number of Reviews:" + review_count);
            }catch(JSONException e){
                Log.e(LOG_TAG, "Error occurred", e);
            }
        }
    }

    private void setTrailersView(){
        //Ensure there is at least one trailer
        if (trailer_count != 0) {

            View v = createLineView();
            ll.addView(v);

            for (int i = 0; i < trailer_count; i++) {
                Button b = new Button(this);
                LinearLayout.LayoutParams b_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                b_params.setMargins(30, 10, 20, 20);
                b.setLayoutParams(b_params);
                b.setText("Watch Trailer " + Integer.toString(i + 1));
                b.setId(i);
                b.setBackgroundColor(getResources().getColor(R.color.indigo));
                b.setTextColor(getResources().getColor(R.color.white));
                b.setTextSize(18);
                b.setPadding(20, 10, 20, 10);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String youtube_id = youtube_ids[view.getId()];
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + youtube_id));
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + youtube_id));
                            String title = "Watch video via";
                            Intent chooser = Intent.createChooser(intent, title);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(chooser);
                            }
                        }

                    }
                });
                ll.addView(b);
            }
        }
    }

    private void setReviewsView(){
        //Ensure there is at least one review
        if (review_count != 0) {

            ll.addView(createLineView());

            TextView header = new TextView(this);
            LinearLayout.LayoutParams header_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            header_params.setMargins(30, 10, 20, 20);
            header.setLayoutParams(header_params);
            header.setText(R.string.reviews);
            header.setTextSize(25);
            header.setTextColor(getResources().getColor(R.color.colorPrimary));
            ll.addView(header);

            for (int i = 0; i < review_count; i++) {
                TextView tv = new TextView(this);
                LinearLayout.LayoutParams tv_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                tv_params.setMargins(30, 10, 20, 20);
                tv.setLayoutParams(tv_params);
                tv.setTextColor(getResources().getColor(R.color.black));
                try {
                    String review = reviews.getJSONObject(i).getString("content");
                    tv.setText(review);
                    ll.addView(tv);
                    ll.addView(createLineView());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "JSON Error", e);
                }
            }
        }
    }

    public View createLineView() {
        View v = new View(this);
        v.setBackgroundColor(getResources().getColor(R.color.black));
        LinearLayout.LayoutParams v_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                3);
        v_params.topMargin = 30;
        v_params.bottomMargin = 30;
        v.setLayoutParams(v_params);
        return v;
    }
}
