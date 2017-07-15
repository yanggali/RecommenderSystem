package structure;

import java.util.List;

/**
 * Created by Yangjiali on 2017/4/4 0004.
 * Version 1.0
 */
public class MovieTagWeight {
    String movieId;
    List<TagWeight> tagWeights;

    public MovieTagWeight(String movieId, List<TagWeight> tagWeights) {
        this.movieId = movieId;
        this.tagWeights = tagWeights;
    }
}
