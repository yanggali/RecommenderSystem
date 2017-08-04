package algorithms;

/**
 * Created by Yangjiali on 2017/7/4 0004.
 * Version 1.0
 */

import model.Movie;
import utils.CalSimilarity;
import utils.FileIO;

import java.util.*;

/**
 * initial tensor
 */
public class Tensor_initial {
    public static Map<String, Movie> movieMapList = new HashMap<>();
    public static Map<String, Integer> movieToIndex = new HashMap<>();
    public static Map<Integer,String> indexToMovie = new HashMap<>();
    public static Map<String, Integer> actorToIndex = new HashMap<>();
    public static Map<String, Integer> countryToIndex = new HashMap<>();
    public static Map<String, Integer> directorToIndex = new HashMap<>();
    public static Map<String, Integer> genreToIndex = new HashMap<>();
    public static Map<String,Integer> tagToIndex = new HashMap<>();
    public static Map<String,Movie> movieList = new HashMap<>();
    public static Map<String,Movie> movieSubList = new HashMap<>();
    public static void main(String[] args) {
        float[][] sim = new float[2596][2596];
        sim[1][1] = 1;
    }
    public static void indexIntial(){
        CalSimilarity cs = new CalSimilarity();
        Map<String,Movie> movieList = cs.getMovieMap();
        System.out.println(movieList.size());
        String filepath = System.getProperty("user.dir")+"\\data\\subMovieIndex(100_130).dat";
        movieSubList = getSubList(filepath, movieList);
        System.out.println(movieSubList.size());
        String outpath = System.getProperty("user.dir")+"\\data\\movieContent\\subTensorIndex(100_130).dat";
        initialSubIndex(movieSubList,outpath);
    }
    //初始化各个属性的下标
    public static void initialSubIndex(Map<String,Movie> movieMap,String filepath){
        for (Map.Entry<String, Movie> movieEntry : movieMap.entrySet()) {
            movieToIndex.put(movieEntry.getKey(),movieToIndex.size()+1);
            indexToMovie.put(movieToIndex.size()+1,movieEntry.getKey());
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
        System.out.println("电影数："+movieToIndex.size()+"\n"
                            +"演员数："+actorToIndex.size()+"\n"
                            +"导演数："+directorToIndex.size()+"\n"
                            +"类型数："+genreToIndex.size()+"\n"
                            +"国家数："+countryToIndex.size()+"\n"
                            +"标签数："+tagToIndex.size());
//        StringBuilder str = new StringBuilder();
//        for (Map.Entry<String, Movie> movieEntry : movieSubList.entrySet()) {
//
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
//        FileIO.appendToFile(filepath,str.toString());
    }
    public static Map<String,Movie> getSubList(String filepath,Map<String,Movie> MovieMap){
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
    //将各属性对应到各下标中
    public static void initialIndex(){
        movieToIndex = CalSimilarity.movieIndex;
//        actorToIndex = CalSimilarity.actorIndex;
//        countryToIndex = CalSimilarity.countryIndex;
//        directorToIndex = CalSimilarity.directorIndex;
//        genreToIndex = CalSimilarity.genreIndex;
//        tagToIndex = CalSimilarity.tagIndex;
    }
    public static void printList() {
        int count = 0;
        System.out.println(movieMapList.size());
        for (Map.Entry<String, Movie> movieEntry : movieMapList.entrySet()) {
            for (String actor : movieEntry.getValue().getActors()) {
                for (String genre : movieEntry.getValue().getGeneres()) {
                    if (directorToIndex.get(movieEntry.getValue().getDirector())==null)
                    {
                        System.out.println(movieToIndex.get(movieEntry.getKey())+":"+movieEntry.getKey());
                    }
//                    System.out.println(movieToIndex.get(movieEntry.getKey())+" "+directorToIndex.get(movieEntry.getValue().getDirector())
//                            +" "+actorToIndex.get(actor)+" "+countryToIndex.get(movieEntry.getValue().getCountry())+" "+genreToIndex.get(genre));
                }
            }
        }
    }
    //将元素下标写入文件中
    public static void writeToFile(String filename){
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, Movie> movieEntry : movieMapList.entrySet()) {
            for (String actor : movieEntry.getValue().getActors()) {
                for (String genre : movieEntry.getValue().getGeneres()) {
                    if (directorToIndex.get(movieEntry.getValue().getDirector())!=null) {
                        stringBuffer.append((movieToIndex.get(movieEntry.getKey())+1) + " " + (directorToIndex.get(movieEntry.getValue().getDirector())+1)
                                + " " + (actorToIndex.get(actor)+1) + " " + (countryToIndex.get(movieEntry.getValue().getCountry())+1) + " " + (genreToIndex.get(genre)+1) + "\n");
                    }
                }
            }
        }
        FileIO.appendToFile(filename,stringBuffer.toString());
    }
    public static void writeMovieGenre(String filename){
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, Movie> movieEntry : movieMapList.entrySet()) {
                for (String genre : movieEntry.getValue().getGeneres()) {
                    stringBuffer.append((movieToIndex.get(movieEntry.getKey()) + 1) + " "+ (genreToIndex.get(genre) + 1) + "\n");
                }
        }
        FileIO.appendToFile(filename,stringBuffer.toString());
    }
    //四路张量
    public static void writeFourWay(String filename){
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, Movie> movieEntry : movieMapList.entrySet()) {
            for (String genre : movieEntry.getValue().getGeneres()) {
                if (directorToIndex.get(movieEntry.getValue().getDirector())!=null){
                    System.out.println((movieToIndex.get(movieEntry.getKey()) + 1) +" "
                            +(directorToIndex.get(movieEntry.getValue().getDirector())+1)+ " "
                            +(countryToIndex.get(movieEntry.getValue().getCountry())+1)+ " "
                            + (genreToIndex.get(genre) + 1) + "\n");
                    stringBuffer.append((movieToIndex.get(movieEntry.getKey()) + 1) +" "
                            +(directorToIndex.get(movieEntry.getValue().getDirector())+1)+ " "
                            +(countryToIndex.get(movieEntry.getValue().getCountry())+1)+ " "
                            + (genreToIndex.get(genre) + 1) + "\n");
                }

            }
        }
        FileIO.appendToFile(filename,stringBuffer.toString());
    }
}
