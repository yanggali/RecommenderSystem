package utils;

import algorithms.Tensor_initial;
import model.Movie;

import java.util.*;

/**
 * Created by Yangjiali on 2017/5/4 0004.
 * Version 1.0
 */
public class CalSimilarity {

    public static String artist_tags = System.getProperty("user.dir")+"/data/lastfm/artist_tag.dat";
    public static Map<Integer, String> idToIndex = new HashMap<>();


    public static Map<String,List<String>> bookTagListMap = new HashMap<>();
    public static Map<String,List<String>> artistTagListMap = new HashMap<>();
    //存放actor,country等以及对应下标
    public static Map<String,Integer> movieIndex = new HashMap<>();
    public static Map<String,Integer> actorIndex;
    public static Map<String,Integer> countryIndex;
    public static Map<String,Integer> directorIndex;
    public static Map<String,Integer> genreIndex;
    public static Map<String,Integer> tagIndex;


    public Map<String, Integer> actorToIndex = new HashMap<>();
    public Map<String, Integer> countryToIndex = new HashMap<>();
    public Map<String, Integer> directorToIndex = new HashMap<>();
    public Map<String, Integer> genreToIndex = new HashMap<>();
    public Map<String,Integer> tagToIndex = new HashMap<>();


    //存放所有电影的map
    public Map<String, Movie> movieMap;
    public Map<String, Integer> movieToIndex;
    public Map<Integer,String> indexToMovie;
    public float[][] movieSimMatrix;

    public CalSimilarity() {
        movieMap = new HashMap<>();
        String movie_actors=System.getProperty("user.dir")+"/data/movieContent/movie_actors.dat";
        String movie_country = System.getProperty("user.dir")+"/data/movieContent/movie_countries.dat";
        String movie_directors = System.getProperty("user.dir")+"/data/movieContent/movie_directors.dat";
        String movie_genres = System.getProperty("user.dir")+"/data/movieContent/movie_genres.dat";
        String movie_tags = System.getProperty("user.dir")+"/data/movieContent/movie_tags.dat";
        initializeCountry(movie_country);
        initializeActors(movie_actors);
        initializeDirector(movie_directors);
        initializeGenres(movie_genres);
        initializeTags(movie_tags);
    }

    public Map<String, Movie> getMovieMap() {
        return movieMap;
    }

    public Map<String, Integer> getMovieToIndex() {
        return movieToIndex;
    }

    public Map<Integer, String> getIndexToMovie() {
        return indexToMovie;
    }

    public float[][] getMovieSimMatrix() {
        return movieSimMatrix;
    }

    /**
     * 初始化电影相似度矩阵
     * @param subMovieSet
     */
    public void initialMovieSim(Set<String> subMovieSet){
        movieToIndex = new HashMap<>();
        indexToMovie = new HashMap<>();
        for (String movieId : subMovieSet) {
            movieToIndex.put(movieId, movieToIndex.size());
            indexToMovie.put(indexToMovie.size(),movieId);
        }
        int len = subMovieSet.size();
        System.out.println(Runtime.getRuntime().totalMemory()/1000/1000);
        System.out.println(Runtime.getRuntime().freeMemory()/1000/1000);
        movieSimMatrix = new float[len][len];
        for(int i=0;i < len;i++){
            for (int j = 0;j < len;j++){
                if ( i < j){
                    movieSimMatrix[i][j] = calSimilarity(indexToMovie.get(i),indexToMovie.get(j));
                }
                movieSimMatrix[j][i] = movieSimMatrix[i][j];
            }
        }
    }
    public Map<String,Movie> getSubMap(String filepath,Map<String,Movie> MovieMap){
        Map<String,Movie> submovieMap = new HashMap<>();
        List<String> fileList = FileIO.readFileByLines(filepath);
        int num = 0;
        for (String movieId : fileList) {
            if (MovieMap.containsKey(movieId)){
                num++;
                //if (num > 100) break;
                submovieMap.put(movieId,MovieMap.get(movieId));
            }
        }
        return submovieMap;
    }

    //初始化电影相似度矩阵（根据文件）
    public void initialMovieSim(String movieToIndexFile, String filepath){
        movieToIndex = new HashMap<>();
        indexToMovie = new HashMap<>();
        List<String> movietoindexList = FileIO.readFileByLines(movieToIndexFile);
        for (String line : movietoindexList) {
            movieToIndex.put(line.split(" ")[0],Integer.parseInt(line.split(" ")[1]));
            indexToMovie.put(Integer.parseInt(line.split(" ")[1]),line.split(" ")[0]);
        }
        List<String> fileList = FileIO.readFileByLines(filepath);
        int len = fileList.size();
        System.out.println(Runtime.getRuntime().totalMemory()/1000/1000);
        System.out.println(Runtime.getRuntime().freeMemory()/1000/1000);
        System.out.println("数组长度"+len);
        movieSimMatrix = new float[len][len];
        System.out.println("=====");
        int i=0;
        for (String line : fileList) {
            String[] lineList = line.split(" ");
            for (int j = 0;j < len;j++) {
                movieSimMatrix[i][j] = Float.parseFloat(lineList[j]);
            }
            i++;
        }
        int index1,index2,index3;
        index1 = movieToIndex.get("47");
        index2 = movieToIndex.get("4963");
        index3 = movieToIndex.get("5810");
        System.out.println("47与4963相似度为："+movieSimMatrix[index1][index2]);
        System.out.println("47与5810相似度为："+movieSimMatrix[index1][index3]);
    }
    public Map<String,Float> similarityMap(String movieId){
        Map<String,Float> simMap = new HashMap<>();
        for (int i = 0; i < movieToIndex.size(); i++) {
            simMap.put(indexToMovie.get(i),movieSimMatrix[movieToIndex.get(movieId)][i]);
        }
        return simMap;
    }

    /**
     * 初始化张量下标
     * @param filepath：电影id文件
     */
    public void initialSubIndex(String filepath,String movieToIndexPath,String tensorIndexPath){
        movieToIndex = new HashMap<>();
        indexToMovie = new HashMap<>();
        StringBuilder movieToIndexStr = new StringBuilder();
        Map<String,Movie> movieSubMap = getSubMap(filepath, movieMap);
        for (Map.Entry<String, Movie> movieEntry : movieSubMap.entrySet()) {
            movieToIndex.put(movieEntry.getKey(),movieToIndex.size()+1);
            indexToMovie.put(indexToMovie.size()+1,movieEntry.getKey());
            movieToIndexStr.append(movieEntry.getKey()+" "+(movieToIndex.size()-1)+"\n");
            for (String actor : movieEntry.getValue().getActors()) {
                if (!actorToIndex.containsKey(actor)){
                    actorToIndex.put(actor,actorToIndex.size()+1);
                }
            }
            if (!directorToIndex.containsKey(movieEntry.getValue().getDirector()))
            {
                directorToIndex.put(movieEntry.getValue().getDirector(),directorToIndex.size()+1);
            }
            for (String genre : movieEntry.getValue().getGeneres()) {
                if (!genreToIndex.containsKey(genre)){
                    genreToIndex.put(genre,genreToIndex.size()+1);
                }
            }
            if (!countryToIndex.containsKey(movieEntry.getValue().getCountry())){
                countryToIndex.put(movieEntry.getValue().getCountry(),countryToIndex.size()+1);
            }
            for (String tag : movieEntry.getValue().getTags()) {
                if (!tagToIndex.containsKey(tag)){
                    tagToIndex.put(tag,tagToIndex.size()+1);
                }
            }
        }
        FileIO.writeToFile(movieToIndexPath,movieToIndexStr.toString());
        System.out.println("电影数："+movieToIndex.size()+"\n"
                +"演员数："+actorToIndex.size()+"\n"
                +"导演数："+directorToIndex.size()+"\n"
                +"类型数："+genreToIndex.size()+"\n"
                +"国家数："+countryToIndex.size()+"\n"
                +"标签数："+tagToIndex.size());
        StringBuilder str = new StringBuilder();
//        for (Map.Entry<String, Movie> movieEntry : movieSubMap.entrySet()) {
//                for (String genre : movieEntry.getValue().getGeneres()) {
//                    for (String tag : movieEntry.getValue().getTags()) {
//                        str.append(movieToIndex.get(movieEntry.getKey()) + " "
//                                + directorToIndex.get(movieEntry.getValue().getDirector())
//                                + " " + genreToIndex.get(genre) + " " + countryToIndex.get(movieEntry.getValue().getCountry())
//                                + " " + tagToIndex.get(tag)+"\n");
//                    }
//
//
//            }
//        }
        //只保存电影id和标签
        for (Map.Entry<String, Movie> movieEntry : movieSubMap.entrySet()) {
            for (String tag : movieEntry.getValue().getTags()) {
                str.append(movieToIndex.get(movieEntry.getKey())+" "+tagToIndex.get(tag)+"\n");
            }
        }
        FileIO.writeToFile(tensorIndexPath,str.toString());
    }
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


    //初始化导演
    public void initializeDirector(String path){
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
    public void initializeCountry(String path){
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
    public void initializeGenres(String path){
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
    public void initializeTags(String path){
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
    public void initializeActors(String path){
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
    public float calSimilarity(String aId,String bId){
        Movie a = movieMap.get(aId);
        Movie b = movieMap.get(bId);
        float similarity = 0;
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
    public Map<String, Float> similarityList(String aId) {
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
    //根据张量计算的内容相似度
    public Map<String, Float> similarityListByTensor(String aId) {
        Map<String,Float> map = new HashMap<>();
        float[] array= movieSimMatrix[Tensor_initial.movieToIndex.get(aId)-1];
        for(int i=0;i < array.length;i++){
            map.put(Tensor_initial.indexToMovie.get(i+1),array[i]);
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
        CalSimilarity cs = new CalSimilarity();
        String subIndexPath = System.getProperty("user.dir") + "/data/subMovieIndex(100_130).dat";
//        String movieToIndexPath = System.getProperty("user.dir") + "/data/movieToIndex(100_130).dat";
//        String subTensorIndex = System.getProperty("user.dir") + "/data/subTensorIndex(100_130).dat";
        String movieToIndexPath = System.getProperty("user.dir") + "/data/test(100_130).dat";
        String subTensorIndex = System.getProperty("user.dir") + "/data/testtensor(100_130).dat";
        cs.initialSubIndex(subIndexPath,movieToIndexPath,subTensorIndex);
    }
}
