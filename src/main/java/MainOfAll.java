import Jama.Matrix;
import algorithms.BayesScoring;
import algorithms.ItemSimilarity;
import model.Movie;
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
    public static Map<String,Integer> userToIndex;
    public static Map<String,Integer> itemToIndex;
    public static double[][] rateMatrix;
    public static Map<String,Map<String,Float>> recListMap = new HashMap<>();
    public static String rateFile = System.getProperty("user.dir")+"/src/main/resources/doubanset/user_rates.dat";
    public static String tagFile = System.getProperty("user.dir")+"/src/main/resources/doubanset/user_tags.dat";
    public static CalSimilarity cs;
    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().totalMemory()/1000/1000);
        System.out.println(Runtime.getRuntime().freeMemory()/1000/1000);

        String tagpath = System.getProperty("user.dir")+"/data/movieRating/hetrectags.dat";
        String ratepath = System.getProperty("user.dir")+"/data/movieRating/ratings.dat";
        String scorepath = System.getProperty("user.dir")+"/data/movieRating/moviedistribution.dat";
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
        matrixPrepareWork();

        //recommendByMatrix();
        //recommendWay(2);
        //recommendByAll(1);


    }

    public static void recommendByMatrix(){
        //用矩阵法求解
        //获取rateMatrix
        double[][] rateMatrix = FileIO.fileToMatrix(System.getProperty("user.dir") + "/data/matrix/rateMatrix.dat");

        //获取itemToIndex
        itemToIndex = FileIO.getMap(System.getProperty("user.dir")+"/data/movieIndex/movieToIndex.dat");
        userToIndex = FileIO.getMap(System.getProperty("user.dir")+"/data/movieIndex/userToIndex.dat");
        Map<Integer,String> indexToItem = new HashMap<>();
        for (Map.Entry<String, Integer> entry : itemToIndex.entrySet()) {
            indexToItem.put(entry.getValue(),entry.getKey());
        }
        for (int recNum = 10;recNum <= 10;recNum++){
            int times=0;
            double RMSE = 0;
            while (times < 5){
                double[][] tempRateMatrix = new double[rateMatrix.length][rateMatrix[0].length];
                for (int i = 0;i < rateMatrix.length;i++){
                    for (int j = 0;j < rateMatrix[0].length;j++){
                        tempRateMatrix[i][j] = rateMatrix[i][j];
                    }
                }
                //1 对每一个用户抽取训练集（tempRateMatrix）和测试集（testMap）
                for (Map.Entry<String, UserRecord> userRecordEntry : subUserRecordMap.entrySet()) {
                    int totallen = userRecordEntry.getValue().getItems().size();
                    Set<String> recordSet = userRecordEntry.getValue().getItems().keySet();
                    String[] recordArray = recordSet.toArray(new String[recordSet.size()]);
                    Map<String,Double> testMap = new HashMap<>();
                    Map<String,Double> trainMap = new HashMap<>();
                    for (int i = 0;i < totallen;i++){
                        if (i >= totallen*times/5&&i<totallen*(times+1)/5){
                            tempRateMatrix[itemToIndex.get(recordArray[i])][userToIndex.get(userRecordEntry.getKey())] = 0;
                            testMap.put(recordArray[i], userRecordEntry.getValue().getItems().get(recordArray[i]));
                        }
                        else {
                            trainMap.put(recordArray[i],userRecordEntry.getValue().getItems().get(recordArray[i]));
                        }
                    }
                    userRecordEntry.getValue().setTestItems(testMap);
                    userRecordEntry.getValue().setTrainItems(trainMap);
                }
                //计算所有物品的平均值
                double[] avgRate = getAvgRate(tempRateMatrix);

                //2 进行实验（基于内容）
                Matrix S,F,C,I,SS;
                //2.1 初始化相似度矩阵S(1570*1570)和物品特征矩阵F(1570*19)
                double[][] simMatrix = BayesScoring.getItemSimMatrix(tempRateMatrix);

                S = new Matrix(simMatrix);
                F = new Matrix(FileIO.fileToMatrix(System.getProperty("user.dir") + "/data/matrix/itemFeatureMatrix.dat"));
                //2.2 计算特征间相似度矩阵C即填充后的物品相似度矩阵SS
                double[][] eye = new double[F.getColumnDimension()][F.getColumnDimension()];
                for (int i=0;i < eye.length;i++){
                    for (int j =0;j < eye.length;j++){
                        if (i==j)
                            eye[i][j] = 10;
                    }
                }
                I = new Matrix(eye);
                //C=(F'F+lamdaI)^-1F'SF(F'F+lamdaI)^-1
                C = (F.transpose().times(F).plus(I)).inverse().times(F.transpose()).times(S).times(F)
                        .times((F.transpose().times(F).plus(I)).inverse());
                SS = F.times(C).times(F.transpose());
                double avgRMSE = 0;
                int nonRec = 0;
                //计算RMSE
                for (Map.Entry<String, UserRecord> userRecordEntry : subUserRecordMap.entrySet()) {
                    Set<Integer> trainIndexSet = new HashSet<>();
                    for (String s : userRecordEntry.getValue().getTrainItems().keySet()) {
                        trainIndexSet.add(itemToIndex.get(s));
                    }
                    Map<String,Double> recMap = new HashMap<>();
                    //对于每一个物品计算它的评分
                    for (Map.Entry<String, Double> recordEntry : userRecordEntry.getValue().getTestItems().entrySet()) {
                        int i = itemToIndex.get(recordEntry.getKey());
                        //计算i的得分:找到该用户已评论过的且与i最相似的top-k个物品
                        Map<Integer,Double> indexSim = new HashMap<>();
                        for (int j = 0;j < SS.getColumnDimension();j++){
                            if (trainIndexSet.contains(j)){
                                indexSim.put(j,SS.get(i,j));
                            }
                        }
                        indexSim = ItemSimilarity.sortByValue(indexSim,1);
                        double sum1 = 0,sum2 = 0,rate = 0;
                        int  k = 0;
                        for (Map.Entry<Integer, Double> entry : indexSim.entrySet()) {
                            if (k++ < 10){
                                sum1 += entry.getValue() * (tempRateMatrix[entry.getKey()][userToIndex.get(userRecordEntry.getKey())] - avgRate[entry.getKey()]);
                                sum2 += entry.getValue();
                            }
                            else break;
                        }
                        rate = sum1 / sum2 + avgRate[i];
                        //if (rate > 5) System.out.println("sum1:"+sum1+" sum2:"+sum2+" avgRate[i]:"+avgRate[i]);
                        recMap.put(indexToItem.get(i),rate);
                    }
                    recMap = ItemSimilarity.sortByValue(recMap,1);
                    //计算RMSE
                    double rmse = 0;
                    //测试集
                    Map<String,Double> testMap = userRecordEntry.getValue().getTestItems();
                    //推荐集
                    int n = 0;
                    for (Map.Entry<String, Double> recEntry : recMap.entrySet()) {
                        if (recEntry.getValue() >= 1){
                            n++;
                            rmse += (recEntry.getValue() - testMap.get(recEntry.getKey()))*(recEntry.getValue() - testMap.get(recEntry.getKey()));
                        }
                        else continue;
                    }
                    if (n > 0){
                        rmse = Math.sqrt(rmse/n);
                        avgRMSE += rmse;
                    }
                    else nonRec++;
                }
                avgRMSE /= (subUserRecordMap.size()-nonRec);
                RMSE += avgRMSE;
                System.out.println("第"+times+"次平均绝对误差："+avgRMSE);
                //推荐

                times++;
            }
            System.out.println("全部平均误差为："+RMSE/5);
        }

    }
    //获取每一个物品的平均值
    public static double[] getAvgRate(double[][] rateMatrix){
        double[] avgRate = new double[rateMatrix.length];
        int[] nonZeros = new int[rateMatrix.length];
        for (int i = 0;i < rateMatrix.length;i++){
            for (int j = 0;j < rateMatrix[0].length;j++){
                if (rateMatrix[i][j]!=0){
                    avgRate[i]+=rateMatrix[i][j];
                    nonZeros[i]++;
                }
            }
        }
        for (int i = 0;i < rateMatrix.length;i++){
            if (nonZeros[i]!=0)
                avgRate[i]/=nonZeros[i];
        }
        return avgRate;
    }

    /**
     * 将电影子集id对应index存入到文件
     * 将用户子集id对应index存入到文件
     * 将用户电影评分矩阵存入到文件
     * 将电影相似度矩阵写入文件
     * 将物品特征矩阵写入文件中
     */
    public static void matrixPrepareWork(){
        String movieToIndexPath = System.getProperty("user.dir")+"/data/movieIndex/movieToIndex.dat";
        String userToIndexPath = System.getProperty("user.dir")+"/data/movieIndex/userToIndex.dat";
        String rateMatrixPath = System.getProperty("user.dir")+"/data/matrix/rateMatrix.dat";
        double[][] rateMatrix = BayesScoring.getRateMatrix(subUserRecordMap,userToIndexPath,movieToIndexPath);
        FileIO.matrixToFile(rateMatrix,rateMatrixPath);
        double[][] itemSimMatrix = BayesScoring.getItemSimMatrix(rateMatrix);
        Set<String> movieSet = new HashSet<>();
        for (Map.Entry<String, UserRecord> entry : subUserRecordMap.entrySet()) {
            movieSet.addAll(entry.getValue().getItems().keySet());
            //movieSet.addAll(entry.getValue().getItemsByItemTag());
        }
        System.out.println("过滤之后还剩：" + subUserRecordMap.size() + "个用户\n还有" + movieSet.size() + "部电影");

        //将相似度矩阵写入文件
        String simMatrixPath = System.getProperty("user.dir")+"/data/matrix/simMatrix.dat";
        FileIO.matrixToFile(itemSimMatrix, simMatrixPath);

        //将物品特征矩阵写入文件中
        cs = new CalSimilarity();
        Map<String, Movie> subMovieMap = cs.getSubMap(movieSet);
        int[][] itemfeaturematrix = cs.initialItemFeatureMatrix(subMovieMap,1);
        System.out.println(itemfeaturematrix.length+" "+itemfeaturematrix[0].length);
        String itemfeaturepath = System.getProperty("user.dir")+"/data/matrix/itemFeatureMatrix.dat";
        FileIO.matrixToFile(itemfeaturematrix,itemfeaturepath);
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

    public static void recommendWay(int type) {
        //对于基于矩阵的方法，要初始化用户对应index，物品对应index
        if (type == 2) {
            String userToIndexPath = System.getProperty("user.dir")+"/data/movieIndex/userToIndex.dat";
            String itemToIndexPath = System.getProperty("user.dir")+"/data/movieIndex/movieToIndex.dat";
            userToIndex = FileIO.getMap(userToIndexPath);
            itemToIndex = FileIO.getMap(itemToIndexPath);
            //从文件中读取用户物品评分矩阵
            String ratePath = System.getProperty("user.dir")+"/data/matrix/rateMatrix_v2.dat";
            rateMatrix = FileIO.fileToMatrix(ratePath);
        }
        for (int recNum = 10; recNum <= 100; recNum += 10) {
            //System.out.println("推荐数："+recNum);
            long initialTime = System.currentTimeMillis();
            float avgPrecision = 0;
            for (Map.Entry<String, UserRecord> userEntry : subUserRecordMap.entrySet()) {
                float userPrecision = getPrecision(userEntry.getValue(), type, recNum);
                //System.out.println("用户"+userEntry.getKey()+"的准确率为："+userPrecision);
                avgPrecision += userPrecision;
            }
            avgPrecision /= subUserRecordMap.size();
            System.out.println("推荐数为：" + recNum + "时所有用户的准确率为：" + avgPrecision);
            long endTime = System.currentTimeMillis();
            System.out.println("总共用时" + ((endTime - initialTime) / 1000) + "秒");
        }

    }
    //返回一个用户的推荐准确率(通过十字交叉法得到)
    public static float getPrecision(UserRecord ur,int type,int recNum){
        String[] userItemArray = ur.getItems().keySet().toArray(new String[0]);
        int len=userItemArray.length;
        float avgPrecision = 0;
        for(int i=0;i < 4*len/5;i+=len/5){
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
            //根据矩阵方法推荐
            else if (type == 2){

                double[] rateItems = rateMatrix[userToIndex.get(ur.getUserid())];
                itemScoreMap = new HashMap<>();
                for (Map.Entry<String, Integer> entry : itemToIndex.entrySet()) {
                    itemScoreMap.put(entry.getKey(),(float)rateItems[entry.getValue()]);
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
            itemScoreMap = ItemSimilarity.sortByValue(itemScoreMap,1);
            Set<String> recommendSet = new HashSet<>();
            Stream<Map.Entry<String,Float>> itemStream1 = itemScoreMap.entrySet().stream();
            recommendSet.addAll(itemStream1.limit(recNum).map(e->e.getKey()).collect(Collectors.toSet()));

            recommendSet.removeAll(trainItemSet);
            int num = recommendSet.size();
            recommendSet.retainAll(testItemSet);
            if (num != 0){
                avgPrecision += recommendSet.size()/num;
            }
        }
        avgPrecision /= 5;
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
