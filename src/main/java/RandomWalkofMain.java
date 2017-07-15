import algorithms.BayesScoring;
import algorithms.ItemSimilarity;
import structure.UserRecord;
import utils.FileIO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Yangjiali on 2017/5/2 0002.
 * Version 1.0
 */
public class RandomWalkofMain {
    public static Map<String, Integer> usernodeToIndex = new HashMap<>();
    public static Map<String, Integer> itemnodeToIndex = new HashMap<>();
    public static Map<String, Integer> tagnodeToIndex = new HashMap<>();
    public static Map<Integer,String> indexToUserNode = new HashMap<>();
    public static Map< Integer,String> indexToItemNode = new HashMap<>();
    public static Map< Integer,String> indexToTagNode = new HashMap<>();

    public static int MAXITERATIONTIMES = 1000;
    public static float MINERRORS = 0.0000001f;
    public static float ALPHA = 0.9f;
    public static float[][] matrix;
    public static void main(String[] args) {
        //String tuplepath = System.getProperty("user.dir")+"/data/matrix/last.fm/triples.dat";
        String tuplepath = System.getProperty("user.dir")+"/data/matrix/doubanbook/triples1.dat";
        //String tuplepath = System.getProperty("user.dir")+"/data/matrix/triple1.dat";

        RandomWalkofMain rwr = new RandomWalkofMain();
        double[][] matrix = rwr.initialMatrix(tuplepath, 1);
        rwr.normMatrix(matrix);
        //初始化用户数据
//        String tagpath = System.getProperty("user.dir")+"/src/main/resources/doubanset/booktags.dat";
//        String ratepath = System.getProperty("user.dir")+"/src/main/resources/doubanset/bookrates.dat";
//        String scorepath = System.getProperty("user.dir")+"/src/main/resources/doubanset/bookscore.dat";

        //初始化用户数据
        String tagpath = System.getProperty("user.dir")+"/data/doubanset/booktags.dat";
        String ratepath = System.getProperty("user.dir")+"/data/doubanset/bookrates.dat";
        String scorepath = System.getProperty("user.dir")+"/data/doubanset/bookscore.dat";
//        String tagpath = System.getProperty("user.dir")+"/data/lastfm/tags.dat";
//        String ratepath = System.getProperty("user.dir")+"/data/lastfm/ratings.dat";
//        String scorepath = System.getProperty("user.dir")+"/data/lastfm/artist_score.dat";

//        String tagpath = System.getProperty("user.dir")+"/data/hetrectags.dat";
//        String ratepath = System.getProperty("user.dir")+"/data/ratings.dat";
//        String scorepath = System.getProperty("user.dir")+"/data/moviedistribution.dat";
        test.initialData(tagpath,ratepath,scorepath);
        //test.initialLastFmData();
        //doubantest.initialData();
        Map<String,UserRecord> userMap = BayesScoring.getUsermap(1);
        rwr.recommendByRandomWalk(userMap,matrix);

    }
    //计算所有用户的推荐列表
//    public Map<String,Map<String,Float>> getRecList(double[][] transMatrix) {
//        Map<String,Map<String,Float>> recListMap = new HashMap<>();
//        long startTime = System.currentTimeMillis();
//        for (Map.Entry<String, Integer> entry : usernodeToIndex.entrySet()) {
//            double[] probability = randomWalkRestart(ALPHA, entry.getValue(), MAXITERATIONTIMES, MINERRORS, transMatrix);
//            Map<String, double> itemScore = new HashMap<>();
//            for (Map.Entry<Integer, String> itemEntry : indexToItemNode.entrySet()) {
//                itemScore.put(itemEntry.getValue(), probability[itemEntry.getKey()]);
//            }
//            itemScore = ItemSimilarity.sortByValue(itemScore);
//            recListMap.put(entry.getKey(),itemScore);
//        }
//        long endTime = System.currentTimeMillis();
//        System.out.println("推荐所用时间："+(endTime-startTime)/1000+"秒");
//        return recListMap;
//    }
    //获取top N
    public void getTopN(Map<String,Map<String,Float>> recListMap,Map<String,UserRecord> userRecordMap){
        for (int recommendCount = 100; recommendCount <= 400; recommendCount += 50) {
            System.out.println("推荐数："+recommendCount);
            float avgPrecision = 0, avgRecall = 0, avgFmeasure = 0,recUsers = 0;
            for (Map.Entry<String, Integer> entry : usernodeToIndex.entrySet()) {
                Map<String,Float> itemScore = recListMap.get(entry.getKey());
                itemScore = ItemSimilarity.sortByValue(itemScore);
                Set<String> allSet = new HashSet<>();
                Stream<Map.Entry<String,Float>> itemStream1 = itemScore.entrySet().stream();
                allSet.addAll(itemStream1.limit(recommendCount).map(e->e.getKey()).collect(Collectors.toSet()));
                //System.out.println("用户"+userEntry.getKey()+"的推荐列表长度为："+allSet.size());
                int recNum = allSet.size();
                allSet.retainAll(userRecordMap.get(entry.getKey()).getItems().keySet());
                allSet.removeAll(userRecordMap.get(entry.getKey()).getItemsByItemTag());
                int hitNum = allSet.size();
                //System.out.println("总推荐数："+recNum);
                //System.out.println("用户"+userEntry.getKey()+"的推荐数为："+itemScore.size());
                Set<String> userRecords = userRecordMap.get(entry.getKey()).getItems().keySet();
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
                avgPrecision += precision;
                avgRecall += recall;
                avgFmeasure += fmeasure;
                recUsers++;
            }
            System.out.println("平均准确率：" + avgPrecision / recUsers + "   平均召回率：" + avgRecall / recUsers + "   平均Fmeasure:" + avgFmeasure / recUsers);
        }
    }
    public void recommendByRandomWalk(Map<String,UserRecord> userRecordMap,double[][] transMatrix){
        Map<String,List<Float>> userAP = new HashMap<>();
        for (int recommendCount = 30; recommendCount <= 50; recommendCount += 10) {
            long startTime = System.currentTimeMillis();
            System.out.println("推荐数：" + recommendCount);
            float avgPrecision = 0, avgRecall = 0, avgFmeasure = 0;
            int recUsers = 0;
            for (Map.Entry<String, Integer> entry : usernodeToIndex.entrySet()) {
                double[] probability = randomWalkRestart(ALPHA, entry.getValue(), MAXITERATIONTIMES, MINERRORS, transMatrix);
                Map<String,Double> itemScore = new HashMap<>();
                for (Map.Entry<Integer, String> itemEntry : indexToItemNode.entrySet()) {
                    if (probability[itemEntry.getKey()]!=Double.NaN&&probability[itemEntry.getKey()]>0)
                        itemScore.put(itemEntry.getValue(),probability[itemEntry.getKey()]);
                }
                itemScore= ItemSimilarity.sortByValue(itemScore);
                Set<String> allSet = new HashSet<>();
                Stream<Map.Entry<String,Double>> itemStream1 = itemScore.entrySet().stream();
                allSet.addAll(itemStream1.limit(recommendCount).map(e->e.getKey()).collect(Collectors.toSet()));
                //System.out.println("用户"+userEntry.getKey()+"的推荐列表长度为："+allSet.size());
                int recNum = allSet.size();
                allSet.retainAll(userRecordMap.get(entry.getKey()).getItems().keySet());
                //allSet.removeAll(userRecordMap.get(entry.getKey()).getItemsByItemTag());
                int hitNum = allSet.size();
                //System.out.println("总推荐数："+recNum);
                //System.out.println("用户"+userEntry.getKey()+"的推荐数为："+itemScore.size());
                recUsers++;
                Set<String> userRecords = userRecordMap.get(entry.getKey()).getItems().keySet();
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
                if (!userAP.containsKey(entry.getKey())){
                    List<Float> precisionList = new ArrayList<>();
                    precisionList.add(precision);
                    userAP.put(entry.getKey(),precisionList);
                }else {
                    userAP.get(entry.getKey()).add(precision);
                }
                //System.out.println("用户"+entry.getKey()+"的准确率为："+precision);
                avgPrecision += precision;
                avgRecall += recall;
                avgFmeasure += fmeasure;
                if (recUsers == 100) break;
            }
            System.out.println("推荐数为"+recommendCount+"时平均准确率：" + avgPrecision / recUsers + "   平均召回率：" + avgRecall / recUsers + "   平均Fmeasure:" + avgFmeasure / recUsers);
            long endTime = System.currentTimeMillis();
            System.out.println("时间："+(endTime-startTime)/1000+"秒");
        }
        MainOfAll.calMap(userAP);
    }

    /**
     * 重启动随机游走
     * @param alpha：成功转移概率
     * @param startPoint：待转移初始节点
     * @param maxIterationTimes：最大迭代次数
     * @param minErrors：最小误差
     * @param transMatrix：转移矩阵
     */
    public double[] randomWalkRestart(float alpha, int startPoint, long maxIterationTimes, float minErrors, double transMatrix[][]){
        int iterationTimes = 0;
        double[] rank_sp = new double[transMatrix[0].length];   //过程中得到的向量
        double[] e = new double[transMatrix[0].length];    //开始时的向量
        //init rank_sp, set identify vector
        rank_sp[startPoint] = 1.0f;
        e[startPoint] = 1.0f;
        boolean flag = true;
        while(iterationTimes < maxIterationTimes){
            double[] temp = new double[rank_sp.length];
            for (int i=0;i < rank_sp.length;i++){
                temp[i] = rank_sp[i];
            }
            if(flag == true){
                double[] copy_ranksp = new double[transMatrix.length];
                for(int i= 0; i<transMatrix.length; i++){
                    double sp = 0;
                    for(int j=0; j<transMatrix[0].length; j++){
                        copy_ranksp[i]+= alpha*transMatrix[i][j]*rank_sp[j];
                    }
                }
                for (int k =0;k < transMatrix.length;k++){
                    rank_sp[k] = copy_ranksp[k]+(1-alpha)*e[k];
                }
                if(judge(temp,rank_sp,minErrors)){
                    flag = false;
                }
            }else
                break;
            iterationTimes++;
        }
        //System.out.println("迭代"+iterationTimes+"次后节点["+startPoint+"]的rank score为:");
//        for(int i=0; i<rank_sp.length; i++){
//            System.out.println(rank_sp[i]);
//        }
        return rank_sp;
    }

    /**
     * 判断误差
     * @param a 向量a
     * @param b 向量b
     * @param minErrors 最小误差
     * @return
     */
    public boolean judge(double a[], double b[], double minErrors){
        boolean flag = true;
        for(int i=0; i<a.length; i++){
            if(Math.abs(a[i]-b[i])<minErrors)
                continue;
            else{
                flag = false;
                break;
            }
        }
        return flag;
    }
    //将矩阵写入文件保存成三元组
    public static void  writeToFile(String filepath,String outputfile){
        List<String> list = FileIO.readFileByLines(filepath);
        int index = 0;
        for (String str : list) {
            String[] tuple = str.split("\t");
            if (!usernodeToIndex.containsKey(tuple[0])){
                usernodeToIndex.put(tuple[0],index);
                indexToUserNode.put(index,tuple[0]);
                index++;
            }
        }
        for (String str : list) {
            String[] tuple = str.split("\t");
            if (!itemnodeToIndex.containsKey(tuple[1])){
                itemnodeToIndex.put(tuple[1],index);
                indexToItemNode.put(index,tuple[1]);
                index++;
            }
        }
        for (String str : list) {
            String[] tuple = str.split("\t");
            if (!tagnodeToIndex.containsKey(tuple[2])&&!tuple.equals("null")){
                tagnodeToIndex.put(tuple[2],index);
                indexToTagNode.put(index,tuple[2]);
                index++;
            }
        }
        System.out.println("共有"+usernodeToIndex.size()+"个用户，"+itemnodeToIndex.size()+"个物品，"+tagnodeToIndex.size()+"个标签");
        //初始化转移矩阵
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : list) {
            String[] tuple = str.split("\t");
            int userIndex = usernodeToIndex.get(tuple[0]),itemIndex = itemnodeToIndex.get(tuple[1]),tagIndex = tagnodeToIndex.get(tuple[2]);
            stringBuffer.append(userIndex+" "+itemIndex+" 1\n");
            stringBuffer.append(userIndex+" "+tagIndex+" 1\n");
            stringBuffer.append(tagIndex+" "+itemIndex+" 1\n");
        }
        FileIO.appendToFile(outputfile,stringBuffer.toString());
    }
    /**
     * 读取用户物品二元组初始化到二维矩阵
     * @param filepath
     * @return
     */
    public double[][] initialMatrix(String filepath,int space) {
        List<String> list = FileIO.readFileByLines(filepath);
        int index = 0;
        for (String str : list) {
            String[] tuple = str.split("\t");
            if (!usernodeToIndex.containsKey(tuple[0])){
                usernodeToIndex.put(tuple[0],index);
                indexToUserNode.put(index,tuple[0]);
                index++;
            }
        }
        for (String str : list) {
            String[] tuple = str.split("\t");
            if (!itemnodeToIndex.containsKey(tuple[1])){
                itemnodeToIndex.put(tuple[1],index);
                indexToItemNode.put(index,tuple[1]);
                index++;
            }
        }
        for (String str : list) {
            String[] tuple = str.split("\t");
            if (!tagnodeToIndex.containsKey(tuple[2])&&!tuple.equals("null")){
                tagnodeToIndex.put(tuple[2],index);
                indexToTagNode.put(index,tuple[2]);
                index++;
            }
        }
        System.out.println("共有"+usernodeToIndex.size()+"个用户，"+itemnodeToIndex.size()+"个物品，"+tagnodeToIndex.size()+"个标签");
        double[][] matrix = new double[index][index];
        //初始化转移矩阵
        int i = 0,count = 0;
        for (String str : list) {
            if (i%space == 0){
                String[] tuple = str.split("\t");
                if (!tuple[2].equals("null")){
                    int userIndex = usernodeToIndex.get(tuple[0]),itemIndex = itemnodeToIndex.get(tuple[1]),tagIndex = tagnodeToIndex.get(tuple[2]);
                    //System.out.println(usernodeToIndex.get(tuple[0])+" "+itemnodeToIndex.get(tuple[1]));
                    matrix[userIndex][itemIndex]++;
                    matrix[itemIndex][userIndex]++;
                    matrix[userIndex][tagIndex]++;
                    matrix[tagIndex][userIndex]++;
                    matrix[itemIndex][tagIndex]++;
                    matrix[tagIndex][itemIndex]++;
                    count++;
                }
                else {
                    int userIndex = usernodeToIndex.get(tuple[0]),itemIndex = itemnodeToIndex.get(tuple[1]);
                    matrix[userIndex][itemIndex]++;
                    matrix[itemIndex][userIndex]++;
                }
            }
            i++;
        }
        System.out.println("一共初始化了"+count+"条记录");
        return matrix;
    }
    //归一化转移矩阵
    public void normMatrix(double[][] matrix){
        for (int i = 0; i < matrix[0].length; i++) {
            int sum = 0;
            for (int j = 0; j < matrix.length; j++) {
                sum += matrix[i][j];
            }
            if (sum!=0){
                for (int j = 0; j < matrix.length; j++){
                    matrix[i][j] /= (double) sum;
                }
            }

        }
    }
}
