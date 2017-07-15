package utils;

import Model.Movie;

import java.util.*;

/**
 * Created by Yangjiali on 2017/5/4 0004.
 * Version 1.0
 */
public class CalSimilarity {
    public static String movie_actors="E:\\ADA_Project\\RecommenderSystem\\data\\movieContent\\movie_actors.dat";
    public static String movie_country = "E:\\ADA_Project\\RecommenderSystem\\data\\movieContent\\movie_countries.dat";
    public static String movie_directors = "E:\\ADA_Project\\RecommenderSystem\\data\\movieContent\\movie_directors.dat";
    public static String movie_genres = "E:\\ADA_Project\\RecommenderSystem\\data\\movieContent\\movie_genres.dat";
    public static String movie_tags = "E:\\ADA_Project\\RecommenderSystem\\data\\movieContent\\movie_tags.dat";
    public static String book_tags = "E:\\ADA_Project\\RecommenderSystem\\src\\main\\resources\\doubanset\\books.dat";
    public static String artist_tags = System.getProperty("user.dir")+"/data/lastfm/artist_tag.dat";
    public static Map<Integer,String> idToIndex = new HashMap<>();
    public static Map<String, Movie> movieMap = new HashMap<>();

    public static Map<String,List<String>> bookTagListMap = new HashMap<>();
    public static Map<String,List<String>> artistTagListMap = new HashMap<>();
    //存放actor,country等以及对应下标
    public static Map<String,Integer> movieIndex = new HashMap<>();
    public static Map<String,Integer> actorIndex;
    public static Map<String,Integer> countryIndex;
    public static Map<String,Integer> directorIndex;
    public static Map<String,Integer> genreIndex;
    public static Map<String,Integer> tagIndex;
    //初始化艺术家内容
    public static void initialArtists(){
        List<String> list = FileIO.readFileByLines(artist_tags);
        for (String str : list) {
            String[] artisttags = str.split("::");
            if (!artistTagListMap.containsKey(artisttags[0])){
                List<String> tagList = new ArrayList<>();
                tagList.add(artisttags[1]);
                artistTagListMap.put(artisttags[0],tagList);
            }
            else{
                artistTagListMap.get(artisttags[0]).add(artisttags[1]);
            }
        }
    }
    //初始化书籍内容
    public static void initialBooks(){
        List<String> list = FileIO.readFileByLines(book_tags);
        for (String str : list) {
            String[] booktags = str.split("\t");
            List<String> tagList = Arrays.asList(booktags[1].split(","));
            bookTagListMap.put(booktags[0],tagList);
        }
    }
    public static void initialieMovies(){
        initializeCountry(movie_country);
        initializeActors(movie_actors);
        initializeDirector(movie_directors);
        initializeGenres(movie_genres);
        initializeTags(movie_tags);
    }
    //初始化导演
    public static void initializeDirector(String path){
        directorIndex = new HashMap<>();
        List<String> list = FileIO.readFileByLines(path);
        for (String str : list) {
            String[] director = str.split("\t");
            String d;
            if (director.length <= 1){
                d = "n";
            }
            else {
                d = director[1];
            }
            if (!directorIndex.containsKey(d)){
                directorIndex.put(d,directorIndex.size());
            }
            if (!movieMap.containsKey(director[0])){
                Movie mv = new Movie();
                mv.setMovieId(Integer.parseInt(director[0]));
                mv.setDirector(director[1]);
                movieMap.put(director[0],mv);
            }else {
                movieMap.get(director[0]).setDirector(director[1]);
            }
        }
    }
    //初始化国家
    public static void initializeCountry(String path){
        countryIndex = new HashMap<>();
        List<String> list = FileIO.readFileByLines(path);
        for (String str : list) {
            String[] country = str.split("\t");
            //初始化所有电影
            if (!movieIndex.containsKey(country[0])){
                movieIndex.put(country[0],movieIndex.size());
            }
            String c;
            if (country.length <= 1){
                c = "n";
            }else {
                c = country[1];
            }
            if(!countryIndex.containsKey(c)){
                countryIndex.put(c,countryIndex.size());
            }
            if (!movieMap.containsKey(country[0])){
                Movie mv = new Movie();
                mv.setMovieId(Integer.parseInt(country[0]));
                mv.setCountry(c);
                movieMap.put(country[0],mv);
            }else {
                movieMap.get(country[0]).setCountry(c);

            }
        }
    }

    //初始化类型
    public static void initializeGenres(String path){
        genreIndex = new HashMap<>();
        List<String> list = FileIO.readFileByLines(path);
        for (String str : list) {
            String[] genres = str.split("\t");
            String g;
            if (genres.length <= 1){
                g = "n";
            }
            else {
                g = genres[1];
            }
            if(!genreIndex.containsKey(g)){
                genreIndex.put(g,genreIndex.size());
            }
            if (!movieMap.containsKey(genres[0])){
                Movie mv = new Movie();
                mv.setMovieId(Integer.parseInt(genres[0]));
                List<String> glist = new ArrayList<>();
                glist.add(g);
                mv.setGeneres(glist);
                movieMap.put(genres[0],mv);
            }else {
                movieMap.get(genres[0]).getGeneres().add(g);
            }
        }
    }
    //初始化标签
    public static void initializeTags(String path){
        tagIndex = new HashMap<>();
        List<String> list = FileIO.readFileByLines(path);
        for (String str : list) {
            String[] tags = str.split("\t");
            String t;
            if (tags.length == 3){
                t = tags[1];
            }
            else {
                t = "n";
            }
            if (!tagIndex.containsKey(t)) {
                tagIndex.put(t,tagIndex.size());
            }
            if (!movieMap.containsKey(tags[0])){
                Movie mv = new Movie();
                mv.setMovieId(Integer.parseInt(tags[0]));
                List<String> tlist = new ArrayList<>();
                tlist.add(tags[1]);
                mv.setTags(tlist);
                movieMap.put(tags[0],mv);
            }else {
                movieMap.get(tags[0]).getTags().add(tags[1]);
            }
        }
    }
    //初始化演员
    public static void initializeActors(String path){
        actorIndex = new HashMap<>();
        List<String> list = FileIO.readFileByLines(path);
        for (String str : list) {
            String[] actors = str.split("\t");
            String a;
            if (actors.length <= 1){
                a = "n";
            }
            else{
                a = actors[1];
            }
            if (!actorIndex.containsKey(a)){
                actorIndex.put(a,actorIndex.size());
            }
            if (!movieMap.containsKey(actors[0])){
                Movie mv = new Movie();
                mv.setMovieId(Integer.parseInt(actors[0]));
                List<String> alist = new ArrayList<>();
                alist.add(actors[1]);
                mv.setActors(alist);
                movieMap.put(actors[0],mv);
            }else {
                movieMap.get(actors[0]).getActors().add(actors[1]);
            }
        }
    }
//    //计算相似度矩阵
//    public static void calSimMatrix(int length){
//        similarityMatrix = new float[length][length];
//        for (int i =0;i < length;i++){
//            for (int j =i;j < length;j++){
//                similarityMatrix[i][j] = similarityMatrix[j][i] = calSimilarity(movieMap.get(i),movieMap.get(j));
//            }
//        }
//    }
    public static float calSimilarity(String aId,String bId){
        Movie a = movieMap.get(aId);
        Movie b = movieMap.get(bId);
        float similarity = 0;
        //System.out.println(a.getMovieId()+" "+b.getMovieId());
        if (a.getDirector().equals(b.getDirector()) && !a.getDirector().equals("null")) similarity+=0.1f*(float)1;
        if (a.getCountry().equals(b.getCountry()) && !a.getCountry().equals("null")) similarity+=0.1f*(float)1;
        similarity += 0.2f*calBetweenList(a.getActors(), b.getActors());
        similarity += 0.3f*calBetweenList(a.getGeneres(), b.getGeneres());
        similarity += 0.3f*calBetweenList(a.getTags(), b.getTags());
        return similarity;
    }
    //计算共有相似度
    public static float calBetweenList(List<String> listA,List<String> listB){
        if (listA.size()==0||listB.size()==0) return 0f;
        int overlap = 0;
        for (String str1 : listA) {
            for (String str2 : listB) {
                if (str1.equals(str2)) overlap++;
            }
        }
        int length = listA.size() >= listB.size() ? listA.size() : listB.size();
        return (float) overlap/(float)length;
    }

    //计算一部电影与所有物品的相似度
    public static Map<String, Float> similarityList(String aId) {
        Map<String,Float> map = new HashMap<>();
        for (Map.Entry<String, Movie> movieEntry : movieMap.entrySet()) {
            //float sim = calSimilarity(aId,movieEntry.getKey());
            float sim = calBetweenList(movieMap.get(aId).getTags(),movieEntry.getValue().getTags());
            if (sim == 0f) {
                continue;
            }else {
                map.put(movieEntry.getKey(),sim);
            }
        }
        return map;
    }
    //计算一个艺术家与所有艺术家的相似度
    public static Map<String, Float> similarityListofArtist(String aId) {
        Map<String,Float> map = new HashMap<>();
        for (Map.Entry<String, List<String>> artistEntry : artistTagListMap.entrySet()) {
            float sim = calBetweenList(artistTagListMap.get(aId),artistEntry.getValue());
            if (sim == 0f) {
                continue;
            }else {
                map.put(artistEntry.getKey(),sim);
            }
        }
        return map;
    }
    //计算一本书与所有物品的相似度
    public static Map<String, Float> similarityListofBook(String aId) {
        Map<String,Float> map = new HashMap<>();
        for (Map.Entry<String, List<String>> bookEntry : bookTagListMap.entrySet()) {
            float sim = calBetweenList(bookTagListMap.get(aId),bookEntry.getValue());
            if (sim != 0f) {
                map.put(bookEntry.getKey(),sim);
            }
        }
        return map;
    }
    public static float calSimilarityofBook(String a,String b){
        List<String> listA = bookTagListMap.get(a);
        List<String> listB = bookTagListMap.get(b);
        return calBetweenList(listA,listB);
    }
    public static float calSimilarityofArtist(String a,String b){
        List<String> listA = artistTagListMap.get(a);
        List<String> listB = artistTagListMap.get(b);
        return calBetweenList(listA,listB);
    }
    public static void main(String[] args) {
        initialieMovies();
        System.out.println(calSimilarity("31594","2343"));
        System.out.println(movieMap.get("31594"));
        System.out.println(movieMap.get("2343"));
//        for (Map.Entry<String, Movie> movieEntry : movieMap.entrySet()) {
//            System.out.println("电影"+movieEntry.getKey()+"与其他电影的相似度");
//            Map<String, Float> map = ItemSimilarity.sortByValue(similarityList(movieEntry.getKey()));
//            for (Map.Entry<String, Float> entry : map.entrySet()) {
//                System.out.println(entry.getKey() + ":" + entry.getValue());
//            }
//        }


    }
}
