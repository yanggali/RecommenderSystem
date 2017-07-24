import algorithms.BayesScoring;
import algorithms.ItemSimilarity;
import algorithms.Tensor_initial;
import structure.UserRecord;
import utils.CalSimilarity;
import utils.FileIO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Yangjiali on 2017/5/3 0003.
 * Version 1.0
 */
public class MainOfAll {
    public static Map<String, UserRecord> userRecordMap = new HashMap<>();
    public static Map<String , UserRecord> subUserRecordMap = new HashMap<>();
    public static Map<String,Map<String,Float>> recListMap = new HashMap<>();
    public static String rateFile = System.getProperty("user.dir")+"/src/main/resources/doubanset/user_rates.dat";
    public static String tagFile = System.getProperty("user.dir")+"/src/main/resources/doubanset/user_tags.dat";
    public static void main(String[] args) {
        String tagpath = System.getProperty("user.dir")+"/data/hetrectags.dat";
        String ratepath = System.getProperty("user.dir")+"/data/ratings.dat";
        String scorepath = System.getProperty("user.dir")+"/data/moviedistribution.dat";
        //test.initialData();
        CalSimilarity.initialieMovies();


//        String tagpath = System.getProperty("user.dir")+"/data/lastfm/tags.dat";
//        String ratepath = System.getProperty("user.dir")+"/data/lastfm/ratings.dat";
//        String scorepath = System.getProperty("user.dir")+"/data/lastfm/artist_score.dat";
//        CalSimilarity.initialArtists();

//        String tagpath = System.getProperty("user.dir")+"/src/main/resources/doubanset/booktags.dat";
//        String ratepath = System.getProperty("user.dir")+"/src/main/resources/doubanset/bookrates.dat";
//        String scorepath = System.getProperty("user.dir")+"/src/main/resources/doubanset/bookscore.dat";
//        CalSimilarity.initialBooks();

        test.initialData(tagpath,ratepath,scorepath);

        //doubantest.initialData();


        userRecordMap = BayesScoring.getUsermap((float) 1);
        subUserRecordMap = getSubUserMap(100,200,userRecordMap);
        Set<String> movieSet = new HashSet<>();
        for (Map.Entry<String, UserRecord> entry : subUserRecordMap.entrySet()) {
            movieSet.addAll(entry.getValue().getItemsByItemTag());
        }
        System.out.println("过滤之后还剩：" + subUserRecordMap.size() + "个用户\n还有" + movieSet.size() + "部电影");

//        StringBuilder sb = new StringBuilder();
//        for (String movie : movieSet) {
//            sb.append(movie).append("\n");
//        }
//        String filepath = System.getProperty("user.dir")+"\\data\\subMovieIndex.dat";
//        FileIO.appendToFile(filepath,sb.toString());
        Tensor_initial.indexIntial();
        //初始化矩阵相似度矩阵
        String simPath=System.getProperty("user.dir")+"/data/movieContent/sim.dat";
        CalSimilarity.initialMovieSim(simPath);
//        System.out.println(BayesScoring.tagUserMap.get("空").size());
        //dividDataSets(userRecordMap,rateFile,tagFile);
        recommendByAll(1);
//        float sim1 = CalSimilarity.calBetweenList(CalSimilarity.movieMap.get("44665").getTags(),CalSimilarity.movieMap.get("65126").getTags());
//        float sim2 = CalSimilarity.movieSimMatrix[Tensor_initial.movieToIndex.get("44665") - 1][Tensor_initial.movieToIndex.get("65126") - 1];
//        System.out.println(sim1+"\n"+sim2);
    }
    public static Map<String, UserRecord> getSubUserMap(int mincount,int maxcount,Map<String, UserRecord> allMap){
        Map<String, UserRecord> subMap = new HashMap<>();
        for (Map.Entry<String, UserRecord> entry : allMap.entrySet()) {
            if (entry.getValue().getItems().size() > mincount && entry.getValue().getItems().size() < maxcount) {
                subMap.put(entry.getKey(),entry.getValue());
            }
        }
        return subMap;
    }
    //划分数据集，一半训练，一半测试
    public static void dividDataSets(Map<String, UserRecord> userRecordMap,String rateData,String tagData){
        StringBuffer rateStr = new StringBuffer(),tagStr = new StringBuffer();
        for (Map.Entry<String, UserRecord> entry : userRecordMap.entrySet()) {
            int len = entry.getValue().getItems().size(),index = 0;
            for (Map.Entry<String, Float> itemRate : entry.getValue().getItems().entrySet()) {
                int half = len/2;
                if (index < half){
                    rateStr.append(entry.getKey()+"::"+itemRate.getKey()+"::"+itemRate.getValue()+"\n");
                }
                else {
                    Map<String,List<String>> tagListMap = entry.getValue().getItemTagList();
                    if (tagListMap.get(itemRate.getKey())!=null){
                        for (String tag : tagListMap.get(itemRate.getKey())) {
                            tagStr.append(entry.getKey()+"::"+itemRate.getKey()+"::"+tag+"\n");
                        }
                    }
                    else tagStr.append(entry.getKey()+"::"+itemRate.getKey()+"::null"+"\n");
                }
                index++;
            }
        }
        FileIO.appendToFile(rateData,rateStr.toString());
        FileIO.appendToFile(tagData,tagStr.toString());
    }
    //图与内容结合的方式
    public static void recommendByAll(float weight) {
        Map<String,List<Float>> userAP = new HashMap<>();
        long initialTime = System.currentTimeMillis();
        for (int recommendCount = 10; recommendCount <=50; recommendCount +=10) {
            long startTime = System.currentTimeMillis();
            System.out.println("推荐数：" + recommendCount);
            float avgPrecision = 0, avgRecall = 0, avgFmeasure = 0;
            int recUsers = 0;
            for (Map.Entry<String, UserRecord> userEntry : subUserRecordMap.entrySet()) {
                //推荐列表(协同推荐)
                //Map<String, Float> itemScore = BayesScoring.getItemscoreByAll(userEntry.getKey());
                Map<String,Float> itemScore = new HashMap<>();
                //调整权值
                //BayesScoring.getItemScoreofModel3ByAdjust(userEntry.getKey(),itemScore,weight);
                //固定权值
                //BayesScoring.getItemScoreofModel1(userEntry.getKey(),itemScore);
                //基于电影内容
                BayesScoring.getItemScoreByContent(userEntry.getKey(),itemScore);
                //基于艺术家内容
                //BayesScoring.getArtistScoreByContent(userEntry.getKey(),itemScore);
                //基于书本内容
                //BayesScoring.getBookScoreByContent(userEntry.getKey(),itemScore);
                //itemScore = BayesScoring.getItemscoreByAll(userEntry.getKey());

                itemScore = ItemSimilarity.sortByValue(itemScore);
                Set<String> allSet = new HashSet<>();
                Stream<Map.Entry<String,Float>> itemStream1 = itemScore.entrySet().stream();
                allSet.addAll(itemStream1.limit(recommendCount).map(e->e.getKey()).collect(Collectors.toSet()));
                int recNum = allSet.size();
                allSet.retainAll(userEntry.getValue().getItems().keySet());
                allSet.removeAll(userEntry.getValue().getItemsByItemTag());
                int hitNum = allSet.size();

                //System.out.println("用户"+userEntry.getKey()+"的推荐数为："+recNum);
                recUsers++;
                Set<String> userRecords = userEntry.getValue().getItems().keySet();
                float precision, recall, fmeasure;
                //推荐数
                if (hitNum == 0) {
                    precision = 0;
                    recall = 0;
                    fmeasure = 0;
                } else {
                    precision = (float) hitNum / (float) recNum;
                    recall = (float) hitNum / (float) userRecords.size();
                    fmeasure = 2 * precision * recall / (precision + recall);
                }
                if (!userAP.containsKey(userEntry.getKey())){
                    List<Float> precisionList = new ArrayList<>();
                    precisionList.add(precision);
                    userAP.put(userEntry.getKey(),precisionList);
                }else {
                    userAP.get(userEntry.getKey()).add(precision);
                }
                //System.out.println("用户" + userEntry.getKey() + "的准确率：" + precision + "    召回率：" + recall + "   Fmeasure：" + fmeasure);
                avgPrecision += precision;
                avgRecall += recall;
                avgFmeasure += fmeasure;
                if (recUsers == 100) break;
            }
            System.out.println("平均准确率：" + avgPrecision / recUsers + "   平均召回率：" + avgRecall / recUsers + "   平均Fmeasure:" + avgFmeasure / recUsers);
            long endTime = System.currentTimeMillis();
            System.out.println("时间：" + (float)(endTime - startTime)/(float)1000 + "秒");

        }
        calMap(userAP);
        long totalTime = System.currentTimeMillis();
        System.out.println("计算MAP的时间为："+(float)(totalTime-initialTime)/(float)1000+"秒");
    }

    /**
     * 计算所有用户的MAP
     * @param userAp
     */
    public static void calMap(Map<String,List<Float>> userAp){
        //计算MAP
        float MAP = 0;
        int allUsers = 0;
        for (Map.Entry<String, List<Float>> entry : userAp.entrySet()) {
            allUsers++;
            float ap = 0;
            for (Float p : entry.getValue()) {
                ap+=p;
            }
            ap/=entry.getValue().size();
            MAP+=ap;
        }
        MAP/=allUsers;
        System.out.println("所有用户的MAP为："+MAP);
    }
    //图与内容结合的方式（对每个用户调节权值）
    public static void recommend() {
        for (int recommendCount = 100; recommendCount <= 300; recommendCount += 50) {
            long startTime = System.currentTimeMillis();
            System.out.println("推荐数：" + recommendCount);
            float avgPrecision = 0, avgRecall = 0, avgFmeasure = 0;
            int recUsers = 0;
            for (Map.Entry<String, UserRecord> userEntry : userRecordMap.entrySet()) {
                int cfNum = 0;
                //if (userEntry.getValue().getItemTags() < 10)
                //推荐列表(协同推荐)
                Map<String, Float> itemScore = BayesScoring.getItemscoreByAll(userEntry.getKey());
                //基于内容的推荐
                Map<String, Float> itemScoreByContent = getItemScoreByContent(userEntry.getKey());
                if (itemScore.size() <= 0 && itemScoreByContent.size() <= 0) {
                    continue;
                }
                Set<String> allSet = new HashSet<>();
                Stream<Map.Entry<String, Float>> itemStream1 = itemScore.entrySet().stream();
                allSet.addAll(itemStream1.limit(recommendCount / 2).map(e -> e.getKey()).collect(Collectors.toSet()));
                Stream<Map.Entry<String, Float>> itemStream2 = itemScoreByContent.entrySet().stream();
                allSet.addAll(itemStream2.limit(recommendCount / 2).map(e -> e.getKey()).collect(Collectors.toSet()));
                int recNum = allSet.size();
                allSet.retainAll(userEntry.getValue().getItems().keySet());
                int hitNum = allSet.size();
                //System.out.println("总推荐数："+recNum);
                //System.out.println("用户"+userEntry.getKey()+"的推荐数为："+itemScore.size());
                recUsers++;
                Set<String> userRecords = userEntry.getValue().getItems().keySet();
                float precision, recall, fmeasure;
                //推荐数
                if (hitNum == 0) {
                    precision = 0;
                    recall = 0;
                    fmeasure = 0;
                } else {
                    precision = (float) hitNum / (float) recNum;
                    recall = (float) hitNum / (float) userRecords.size();
                    fmeasure = 2 * precision * recall / (precision + recall);
                }
//                System.out.println("用户" + userEntry.getKey() + "的命中数为：" + hitNum);
//                System.out.println("用户" + userEntry.getKey() + "的准确率：" + precision + "    召回率：" + recall + "   Fmeasure：" + fmeasure);
                avgPrecision += precision;
                avgRecall += recall;
                avgFmeasure += fmeasure;
            }
            System.out.println("平均准确率：" + avgPrecision / recUsers + "   平均召回率：" + avgRecall / recUsers + "   平均Fmeasure:" + avgFmeasure / recUsers);
            long endTime = System.currentTimeMillis();
            System.out.println("时间："+(endTime-startTime)/1000+"秒");
        }
    }

    //得到某个用户基于内容的推荐列表
    public static Map<String, Float> getItemScoreByContent(String userId) {
        Map<String,Float> itemScore = new HashMap<>();
        for (String item : userRecordMap.get(userId).getItemsByItemTag()) {
            itemScore.putAll(CalSimilarity.similarityList(item));
        }
        return ItemSimilarity.sortByValue(itemScore);
    }
}
