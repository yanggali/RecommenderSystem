import algorithms.BayesScoring;
import algorithms.ItemSimilarity;
import structure.UserRecord;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Yangjiali on 2017/3/19 0019.
 */
public class test {
    public static Map<String, UserRecord> userRecordMap = new HashMap<>();
    public static void main(String[] args) {
        //初始化lastfm数据集
        initialLastFmData();
        userRecordMap = BayesScoring.getUsermap(1);

        recommendByTag(400,400,50);
        //recommendByItemTag(400,400,50);
        //recommendByItem();
        //recommend(400,400,50);
    }
    public static void recommenByTagOrItem(){

    }
    public static void recommend(int startNum,int endNum,int shift)
    {
        System.out.println("加权混合方法");
        for (int recommendcount = startNum;recommendcount <= endNum;recommendcount+=shift)
        {
            System.out.println("推荐数："+recommendcount);
            //根据物品标签对寻找用户和物品
            for (float temp = 1;temp <= 10;temp++)
            {
                System.out.println("取每个用户的"+temp/10+"推荐");
                float avgprecision  = 0, avgrecall = 0,avgfmeasure = 0;
                int usercounts = 0; //标签数大于阈值的用户才推荐
                long starttime = System.currentTimeMillis();
                float totalNum = 0;
                for (String user : userRecordMap.keySet()) {
                    float num = userRecordMap.get(user).getItemTags().size() * (temp / 10);
                    totalNum+=num;
                    usercounts++;
                    Map<String, Float> itemtagmap = sortByValue(BayesScoring.getItemTagsScore(user,0.57223f,temp/10));
//                    Map<String, Float> tagmap = sortByValue(BayesScoring.getItemscoreByTag(user));
//                    Iterator<Map.Entry<String,Float>> tagIt = tagmap.entrySet().iterator();
                    Map<String,Float> itemmap = ItemSimilarity.getItemMap(user,temp/10);
                    //将两种结果合并
                    Map<String,Float> allmap = new HashMap<>();
                    float cfNum = (float) recommendcount * 0.5f;
                    int cfCount = 0;
                    for (Map.Entry<String, Float> entry : itemtagmap.entrySet()) {
                        if (cfCount++ > cfNum) break;
                        allmap.put(entry.getKey(),entry.getValue());
                    }
                    int contentCount = 0;
                    for (Map.Entry<String, Float> entry : itemmap.entrySet()) {
                        if (contentCount++ > cfNum) break;
                        allmap.put(entry.getKey(),entry.getValue());
                    }
//                    int i = 0;
//                    while (allmap.size() < recommendcount)
//                    {
//                        if (!itemtagIt.hasNext()&&!itemIt.hasNext()) break;
//                        if (itemtagIt.hasNext()) {
//                            Map.Entry<String,Float> entry1 = itemtagIt.next();
//                            allmap.put(entry1.getKey(), entry1.getValue());
//                        }
////                        if (tagIt.hasNext()) {
////                            Map.Entry<String, Float> entry2 = tagIt.next();
////                            allmap.put(entry2.getKey(), entry2.getValue());
////                        }
//                        if (itemIt.hasNext()) {
//                            Map.Entry<String,Float> entry3 = itemIt.next();
//                            allmap.put(entry3.getKey(), entry3.getValue());
//                        }
//                    }
                    //System.out.println("allmap长度:"+allmap.size());
                    Iterator<Map.Entry<String, Float>> it = allmap.entrySet().iterator();
                    Map<String, Float> ur = userRecordMap.get(user).getItems();
                    int hitcounts = 0, count = 0;
                    while (it.hasNext()) {
                        if (++count > recommendcount) break;
                        Map.Entry<String, Float> entry = it.next();
                        if (ur.containsKey(entry.getKey())) hitcounts++;
                    }
                    float precision, recall,fmeasure;
                    if (hitcounts != 0) {
                        precision = Float.valueOf(hitcounts) / (float)allmap.size();
                        recall = Float.valueOf(hitcounts) /  (ur.size() * (temp / 10));
                        fmeasure = 2 * precision * recall / (precision + recall);
                    } else {
                        precision = 0;
                        recall = 0;
                        fmeasure = 0;
                    }
                    avgprecision += precision;
                    avgrecall += recall;
                    avgfmeasure += fmeasure;
                }
                System.out.println("一共根据"+totalNum+"条记录推荐");
                long endtime = System.currentTimeMillis();
                System.out.println("取每个用户的" + temp / 10 + "推荐所需时间为：" + (endtime - starttime) / 1000 + "秒");
                System.out.println("计算的用户数：" + usercounts);
                float averageP = avgprecision / usercounts;
                float averageR = avgrecall / usercounts;
                float averageF = avgfmeasure / usercounts;
                System.out.println("平均准确率：" + averageP + "  平均召回率：" + averageR +"  平均F-Measure："+averageF);
            }
        }
    }
    //根据用户记录分配权重
    public static void recommendV2(int startNum,int endNum,int shift)
    {
        System.out.println("加权混合方法");
        for (int recommendcount = startNum;recommendcount <= endNum;recommendcount+=shift)
        {
            System.out.println("推荐数："+recommendcount);
            float avgprecision = 0, avgrecall = 0, avgfmeasure = 0;
            long starttime = System.currentTimeMillis();
            float totalNum = 0;
            for (Map.Entry<String, UserRecord> entry : userRecordMap.entrySet()) {
                float percentage = (float) (entry.getValue().getItemTags().size() - 1) / 1247;
                System.out.println(percentage);
                float cfNum = percentage * recommendcount;
                Map<String, Float> itemtagmap = sortByValue(BayesScoring.getItemTagsScore(entry.getKey(),0.57223f));
                //Map<String, Float> tagmap = sortByValue(BayesScoring.getItemscoreByTag(entry.getKey()));
                Map<String, Float> itemmap = ItemSimilarity.getItemMap(entry.getKey());
                //将两种结果合并
                Map<String, Float> allmap = new HashMap<>();
                Stream<Map.Entry<String, Float>> entryStreamCf = itemtagmap.entrySet().stream();
                Stream<Map.Entry<String, Float>> entryStreamItem = itemmap.entrySet().stream();
                Set<String> allRecItems = new HashSet<>();
                allRecItems.addAll(entryStreamCf.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit((int)cfNum)
                        .map(e->e.getKey())
                        .collect(Collectors.toSet()));
                if (itemmap != null){
                    allRecItems.addAll(entryStreamItem.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .limit(recommendcount - (int) cfNum)
                            .map(e -> e.getKey())
                            .collect(Collectors.toSet()));
                }
                int recNum = allRecItems.size();
                allRecItems.retainAll(entry.getValue().getItems().keySet());
                //System.out.println("用户"+entry.getKey()+"的命中数："+allRecItems.size());
                float precision = 0,recall = 0,fmeasure = 0;
                if (allRecItems.size() > 0)
                {
                    precision = (float) allRecItems.size()/recNum;
                    recall = (float) allRecItems.size() / entry.getValue().getItems().size();
                    fmeasure = (float) 2 * precision * recall / (precision + recall);
                }
                else {
                    precision = 0;
                    recall = 0;
                    fmeasure = 0;
                }
                avgprecision += precision;
                avgrecall += recall;
                avgfmeasure += fmeasure;
            }
            long endtime = System.currentTimeMillis();
            System.out.println("计算的时间为："+(endtime-starttime)/1000+"秒");
            System.out.println("计算的用户数：" + userRecordMap.size());
            System.out.println("avgprecision:"+avgprecision);
            float averageP = avgprecision / userRecordMap.size();
            float averageR = avgrecall / userRecordMap.size();
            float avgF = avgfmeasure / userRecordMap.size();
            System.out.println("平均准确率：" + averageP + "  平均召回率：" + averageR + "  平均F-Measure：" + avgF);
        }
    }
    public static void recommendByTag(int startNum,int endNum,int shift)
    {
        for (int recommendcount = startNum;recommendcount<=endNum;recommendcount+=shift)
        {
            System.out.println("推荐数："+recommendcount);
            for (float temp = 1;temp <= 10;temp++)
            {
                System.out.println("选取用户的"+temp/10+"推荐");
                float avgprecision = 0, avgrecall = 0;
                int usercounts = 0; //标签数大于阈值的用户才推荐
                //根据物品标签对寻找用户和物品
                for (String user : userRecordMap.keySet()) {
                    float num = userRecordMap.get(user).getItemTags().size()*(temp/10);
                        usercounts++;
                        Map<String,Float> itemtagmap = BayesScoring.getItemscoreByTag(user,temp/10);
                        Map<String,Float> sortmap = sortByValue(itemtagmap);
                        Iterator<Map.Entry<String, Float>> it = sortmap.entrySet().iterator();
                        Map<String,Float> ur = userRecordMap.get(user).getItems();
                        int hitcounts = 0,count = 0;
                        while (it.hasNext())
                        {
                            if (++count > recommendcount) break;
                            Map.Entry<String, Float> entry = it.next();
                            if (ur.containsKey(entry.getKey())) hitcounts++;
                        }
                        float precision, recall;
                        if (hitcounts != 0)
                        {
                            precision = Float.valueOf(hitcounts) / (float) itemtagmap.size();
                            recall = Float.valueOf(hitcounts) / Float.valueOf(ur.size());
                        }
                        else
                        {
                            precision = 0;
                            recall = 0;
                        }
                        avgprecision += precision;
                        avgrecall += recall;
                    }
                System.out.println("计算的用户数："+usercounts);
                float averageP = avgprecision / usercounts;
                float averageR = avgrecall / usercounts;
                System.out.println("平均准确率：" + averageP + "  平均召回率：" + averageR +"  平均F-Measure："+2*averageP*averageR/(averageR+averageP));
            }
        }
    }
    /**
     * 根据item-tag/tag推荐
     */
    public static void recommendByItemTag(int startNum,int endNum,int shift)
    {
        for (int recommendcount = startNum; recommendcount <= endNum; recommendcount += shift) {
            System.out.println("推荐数：" + recommendcount);
            for (int temp = 1;temp <=10;temp++){
                System.out.println("对打标签数超过"+temp+"的用户推荐");
                int usercounts = 0; //标签数大于阈值的用户才推荐
                float avgprecision = 0, avgrecall = 0;
                for (Map.Entry<String,UserRecord> entry:userRecordMap.entrySet() ) {
                    float num = entry.getValue().getItemTags().size()*(temp/10);
                    if (entry.getValue().getItemTags().size() < temp) {
                        continue;
                    } else {
                        usercounts++;
                        int resUserCount = 0;
                        //根据物品标签对寻找用户和物品
                        Map<String, Float> itemtagmap = BayesScoring.getItemTagsScore(entry.getKey(),temp/10);
                        if (itemtagmap.size() == 0) {
                            continue;
                        }
                        //被推荐的用户数
                        Map<String, Float> sortmap = sortByValue(itemtagmap);
                        Map<String, Float> ur = userRecordMap.get(entry.getKey()).getItems();
                        int hitcounts = 0, count = 0;
                        for (Map.Entry<String, Float> resIt : sortmap.entrySet()) {
                            if (++count > recommendcount) break;
                            if (ur.containsKey(resIt.getKey())) hitcounts++;
                        }
                        float precision, recall;
                        if (hitcounts != 0) {
                            precision = (float) hitcounts / itemtagmap.size();
                            recall = (float) hitcounts / ur.size();
                        } else {
                            precision = 0;
                            recall = 0;
                        }
                        avgprecision += precision;
                        avgrecall += recall;
                    }
                }
                System.out.println("被推荐的用户数：" + usercounts);
                float averageP = avgprecision / usercounts;
                float averageR = avgrecall / usercounts;
                System.out.println("平均准确率：" + averageP + "  平均召回率：" + averageR + "  平均F-Measure：" + 2 * averageP * averageR / (averageR + averageP));
            }

        }
    }

    /**
     * 根据计算已有物品与其他物品的相似度来计算
     */
    public static void recommendByItem()
    {
        for (int recommendcount = 100;recommendcount<=400;recommendcount+=50) {
            System.out.println("推荐数："+recommendcount);
            for (float temp = 1; temp <= 10; temp ++) {
                float avgPrecision = 0, avgRecall = 0,avgFmeasure = 0;
                int usercounts = 0; //标签数大于阈值的用户才推荐
                System.out.println("取每个用户的"+temp/10+"推荐");
                for (UserRecord user : userRecordMap.values()) {
                    float num =  user.getItemTags().size()*(float)(temp/10);

                    Map<String,Float> itemmap = ItemSimilarity.getItemMap(user.getUserid(),temp/10);
                    if (itemmap.size() > 0) usercounts++;
                    Map<String,Float> ur = user.getItems();
                    int hitcounts = 0,count = 0;
                    for (Map.Entry<String, Float> entry : itemmap.entrySet()) {
                        if (++count > recommendcount) break;
                        if (ur.containsKey(entry.getKey())) hitcounts++;
                    }
                    count--;
                    float precision, recall,fmeasure;
                    if (hitcounts != 0)
                    {
                        precision = Float.valueOf(hitcounts) / Float.valueOf(count);
                        recall = Float.valueOf(hitcounts) / Float.valueOf(ur.size());
                        fmeasure = 2 * precision * recall / (precision + recall);
                    }
                    else
                    {
                        precision = 0;
                        recall = 0;
                        fmeasure = 0;
                    }
                    avgPrecision += precision;
                    avgRecall += recall;
                    avgFmeasure += fmeasure;
                }
                System.out.println("计算的用户数："+usercounts);
                System.out.println("平均准确率："+avgPrecision/usercounts+"  平均召回率："+avgRecall/usercounts+"   平均Fmeasure："+avgFmeasure/usercounts);
            }
        }

    }
    /**
     * 初始化lastfm数据集
     * 1、用户-物品-标签三元组；2、评分数据；3、艺术家评分分布；4、艺术家标签分布
     */
    public static void initialLastFmData()
    {
        String pathname = System.getProperty("user.dir")+"\\data\\lastfm\\tags.dat";
        String pathname1 = System.getProperty("user.dir")+"\\data\\lastfm\\ratings.dat";
        String itemscorepath = System.getProperty("user.dir")+"\\data\\lastfm\\artist_score.dat";
        //初始化movie_tag信息
        String filepath = System.getProperty("user.dir") + "\\data\\lastfm\\artist_tag.dat";
        //ItemSimilarity.initialMovieTags(filepath);
        //初始化所有物品的评分
        BayesScoring.initialItemScore(itemscorepath);
        //初始化用户的item-tag记录
        BayesScoring.initialUserItemTagMap(pathname);
        //初始化用户的阅读评分记录
        BayesScoring.initialUserRatingMap(pathname1);
    }
    /**
     * 初始化
     * 1、用户-物品-标签三元组；2、评分数据；3、电影评分分布（正面率）；4、电影标签分布
     */
    public static void initialData(String tagpath,String ratepath,String scorepath)
    {
        String pathname = System.getProperty("user.dir")+"/data/hetrectags.dat";
        String pathname1 = System.getProperty("user.dir")+"/data/ratings.dat";
        String itemscorepath = System.getProperty("user.dir")+"/data/moviedistribution.dat";
        //初始化movie_tag信息
        String filepath = System.getProperty("user.dir") + "/data/movie_tags.dat";
        //ItemSimilarity.initialMovieTags(filepath);
        //初始化所有物品的评分
        BayesScoring.initialItemScore(scorepath);
        //初始化用户的item-tag记录
        BayesScoring.initialUserItemTagMap(tagpath);
        //初始化用户的阅读评分记录
        BayesScoring.initialUserRatingMap(ratepath);
    }
    public static void initialsmallData()
    {
        String pathname = System.getProperty("user.dir")+"\\data\\movielens_small\\triples.dat";
        String pathname1 = System.getProperty("user.dir")+"\\data\\movielens_small\\ratings.dat";
        String itemscorepath = System.getProperty("user.dir")+"\\data\\movielens_small\\moviedistribution.dat";
        //初始化movie_tag信息
        String filepath = System.getProperty("user.dir") + "\\data\\movielens_small\\movie_tag.dat";
        ItemSimilarity.initialMovieTags(filepath);
        //初始化所有物品的评分
        BayesScoring.initialItemScore(itemscorepath);
        //初始化用户的item-tag记录
        BayesScoring.initialUserItemTagMap(pathname);
        //初始化用户的阅读评分记录
        BayesScoring.initialUserRatingMap(pathname1);
    }

    //对map根据value排序
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return -(o1.getValue()).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}
