package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yangjiali on 2017/5/4 0004.
 * Version 1.0
 */
public class Movie {
    int movieId;
    String year;
    List<String> actors;
    String director;
    String country;
    List<String> generes;
    List<String> tags;
    List<String> keywords;

    public Movie() {
        director ="null";
        country = "null";
        year = "null";
        keywords = new ArrayList<>();
        actors = new ArrayList<>();
        generes = new ArrayList<>();
        tags = new ArrayList<>();
    }

    public Movie(int movieId, List<String> actors, String director, String country, List<String> generes, List<String> tags, String year, List<String> keywords) {
        this.year = year;
        this.movieId = movieId;
        this.actors = actors;
        this.director = director;
        this.country = country;
        this.generes = generes;
        this.tags = tags;
        this.keywords = keywords;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getKeywords() {


        return keywords;
    }

    public int getMovieId() {
        return movieId;
    }

    public List<String> getActors() {
        return actors;
    }

    public String getDirector() {
        return director;
    }

    public String getCountry() {
        return country;
    }

    public List<String> getGeneres() {
        return generes;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setActors(List<String> actors) {
        this.actors = actors;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setGeneres(List<String> generes) {
        this.generes = generes;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movieId=" + movieId +
                ", actors=" + actors +
                ", director='" + director + '\'' +
                ", country='" + country + '\'' +
                ", generes=" + generes +
                ", tags=" + tags +
                '}';
    }
}
