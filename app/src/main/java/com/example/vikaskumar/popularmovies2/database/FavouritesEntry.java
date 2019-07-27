package com.example.vikaskumar.popularmovies2.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "favourites")
public class FavouritesEntry {

    @PrimaryKey
    private int id;
    private String title;
    private String synopsis;
    private Double rating;
    private String release_date;
    private String poster_path;

    public FavouritesEntry(int id, String title, String synopsis, Double rating, String release_date, String poster_path) {
        this.id = id;
        this.title = title;
        this.synopsis = synopsis;
        this.rating = rating;
        this.release_date = release_date;
        this.poster_path = poster_path;
    }

    @Ignore
    public FavouritesEntry(String title, String synopsis, Double rating, String release_date, String poster_path) {
        this.title = title;
        this.synopsis = synopsis;
        this.rating = rating;
        this.release_date = release_date;
        this.poster_path = poster_path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

}
