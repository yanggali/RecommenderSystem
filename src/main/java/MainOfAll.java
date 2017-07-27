import algorithms.BayesScoring;
import algorithms.ItemSimilarity;
import structure.UserRecord;
import utils.CalSimilarity;

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
    public static CalSimilarity cs;
    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().totalMemory()/1000/1000);
        System.out.println(Runtime.getRuntime().freeMemory()/1000/1000);

        String tagpath = System.getProperty("user.dir")+"/data/hetrectags.dat";
        String ratepath = System.getProperty("user.dir")+"/data/ratings.dat";
        String scorepath = System.getProperty("user.dir")+"/data/moviedistribution.dat";
        //test.initialData();


//        String tagpath = System.getProperty("user.dir")+"/data/lastfm/tags.dat";
//        String ratepath = System.getProperty("user.dir")+"/data/lastfm/ratings.dat";
//        String scorepath = System.getProperty("user.dir")+"/data/lastfm/artist_score.dat";
//        CalSimilarity.initialArtists();

//        String tagpath = System.getProperty("user.dir")+"/src/main/resources/doubanset/booktags.dat";
//        String ratepath = System.getProperty("user.dir")+"/src/main/resources/doubanset/bookrates.dat";
//        String scorepath = System.getProperty("user.dir")+"/src/main/resources/doubanset/bookscore.dat";
//        CalSimilarity.initialBooks();

        BayesScoring.initialData(tagpath,ratepath,scorepath);

        //doubantest.initialData();


        userRecordMap = BayesScoring.getUsermap((float) 1);
        subUserRecordMap = getSubUserMap(100,110,userRecordMap);
        Set<String> movieSet = new HashSet<>();
        for (Map.Entry<String, UserRecord> entry : subUserRecordMap.entrySet()) {
            movieSet.addAll(entry.getValue().getItems().keySet());
            //movieSet.addAll(entry.getValue().getItemsByItemTag());
        }
        System.out.println("过滤之后还剩：" + subUserRecordMap.size() + "个用户\n还有" + movieSet.size() + "部电影");

//        StringBuilder sb = new StringBuilder();
//        for (String movie : movieSet) {
//            sb.append(movie).append("\n");
//        }
//        String filepath = System.getProperty("user.dir")+"\\data\\subMovieIndex(100_130).dat";
//        FileIO.appendToFile(filepath,sb.toString());
        //Set<String> movieSet = new HashSet<>();
        //List<String> strList = FileIO.readFileByLines(System.getProperty("user.dir")+"\\data\\subMovieIndex(100_130).dat");

//        cs = new CalSimilarity();
//        //cs.initialMovieSim(movieSet);
//        cs.initialMovieSim(System.getProperty("user.dir")+"/data/movieToIndex(100_130).dat",System.getProperty("user.dir")+"/data/movieContent/sim(100_130).dat");
//        System.out.println("内容相似度计算结束");
//
//        recommendWay(1);
        //recommendByAll(1);

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
    public static void recommendWay(int type){
        for(int recNum= 10; recNum <= 25;recNum+=5){
            //System.out.println("推荐数："+recNum);
            long initialTime = System.currentTimeMillis();
            float avgPrecision = 0;
            for (Map.Entry<String, UserRecord> userEntry : subUserRecordMap.entrySet()) {
                float userPrecision = getPrecision(userEntry.getValue(),type,recNum);
                //System.out.println("用户"+userEntry.getKey()+"的准确率为："+userPrecision);
                avgPrecision += userPrecision;
            }
            avgPrecision /= subUserRecordMap.size();
            System.out.println("推荐数为："+recNum+"时所有用户的准确率为："+avgPrecision);
            long endTime = System.currentTimeMillis();
            System.out.println("总共用时"+((endTime-initialTime)/1000)+"秒");
        }

    }
    //返回一个用户的推荐准确率(通过十字交叉法得到)
    public static float getPrecision(UserRecord ur,int type,int recNum){
        String[] userItemArray = ur.getItems().keySet().toArray(new String[0]);
        int len=userItemArray.length;
        float avgPrecision = 0;
        for(int i=0;i < len;i+=len/5){
            Set<String> trainItemSet = new HashSet<>();
            Set<String> testItemSet = new HashSet<>();
            for (int j = 0; j < len; j++) {
                if (j >= i && j < i + len / 5) {
                    testItemSet.add(userItemArray[j]);
                }
                else {
                    trainItemSet.add(userItemArray[j]);
                }
            }
            Map<String,Float> itemScoreMap;
            //基于矢量的内容推荐
            if (type == 1){
                itemScoreMap = new HashMap<>();
                for (String item : trainItemSet) {
                    //System.out.println("正在计算"+item);
                    Map<String,Float> itemSimMap;
                    itemSimMap = cs.similarityMap(item);
                    for (Map.Entry<String, Float> itemEntry : itemSimMap.entrySet()) {
                        if (!itemScoreMap.containsKey(itemEntry.getKey())){
                            itemScoreMap.put(itemEntry.getKey(),itemEntry.getValue());
                        }else {
                            itemScoreMap.put(itemEntry.getKey(),itemScoreMap.get(itemEntry.getKey())+itemSimMap.get(itemEntry.getKey()));
                        }
                    }
                }
            }
            //混合推荐
            else{
                itemScoreMap = new HashMap<>();
                Set<String> trainTagSet = ur.getTagsByItemTags(trainItemSet);
                for (String item : trainItemSet) {
                    //System.out.println("正在计算"+item);
                    Map<String,Float> itemSimMap;
                    itemSimMap = BayesScoring.getItemScoreofModel1(subUserRecordMap,item);
                    for (Map.Entry<String, Float> itemEntry : itemSimMap.entrySet()) {
                        if (!itemScoreMap.containsKey(itemEntry.getKey())){
                            itemScoreMap.put(itemEntry.getKey(),itemEntry.getValue());
                        }else {
                            itemScoreMap.put(itemEntry.getKey(),itemScoreMap.get(itemEntry.getKey())+itemSimMap.get(itemEntry.getKey()));
                        }
                    }
                }
            }
            itemScoreMap = ItemSimilarity.sortByValue(itemScoreMap,2);
            Set<String> recommendSet = new HashSet<>();
            Stream<Map.Entry<String,Float>> itemStream1 = itemScoreMap.entrySet().stream();
            recommendSet.addAll(itemStream1.limit(recNum).map(e->e.getKey()).collect(Collectors.toSet()));
            recommendSet.retainAll(testItemSet);
            avgPrecision += recommendSet.size();
        }
        avgPrecision /= (5 * recNum);
        return avgPrecision;

    }
    //图与内容结合的方式
    public static void recommendByAll(float weight) {
        Map<String,List<Float>> userAP = new HashMap<>();
        long initialTime = System.currentTimeMillis();
        for (int recommendCount = 1; recommendCount <=10; recommendCount +=1) {
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
                BayesScoring.getItemScoreByContent(userEntry.getKey(),itemScore,cs);
                //基于艺术家内容
                //BayesScoring.getArtistScoreByContent(userEntry.getKey(),itemScore);
                //基于书本内容
                //BayesScoring.getBookScoreByContent(userEntry.getKey(),itemScore);
                //itemScore = BayesScoring.getItemscoreByAll(userEntry.getKey());

                itemScore = ItemSimilarity.sortByValue(itemScore,1);
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
            itemScore.putAll(cs.similarityMap(item));
        }
        return ItemSimilarity.sortByValue(itemScore,1);
    }
}
