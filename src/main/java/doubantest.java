/**
 * Created by Yangjiali on 2017/4/11 0011.
 * Version 1.0
 */

import algorithms.BayesScoring;
import algorithms.ItemSimilarity;
import structure.UserRecord;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 在豆瓣读书数据集上测试
 */
public class doubantest {
    public static Map<String, UserRecord> userRecordMap = new HashMap<>();
    public static void main(String[] args) {
        //初始化
        initialData();
        userRecordMap = BayesScoring.getUsermap(1);

        recommendByTag();

    }
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
                //System.out.println(percentage);
                float cfNum = percentage * recommendcount;
                Map<String, Float> itemtagmap = sortByValue(BayesScoring.getItemTagsScore(entry.getKey(),7.9f));
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
                    Map<String, Float> itemtagmap = sortByValue(BayesScoring.getItemTagsScore(user,7.9f,temp/10));
//                    Map<String, Float> tagmap = sortByValue(BayesScoring.getItemscoreByTag(user));
//                    Iterator<Map.Entry<String,Float>> tagIt = tagmap.entrySet().iterator();
                    Map<String,Float> itemmap = ItemSimilarity.getItemMap(user,temp/10);

                    //将两种结果合并
                    Map<String,Float> allmap = new HashMap<>();
                    float cfNum = (float) recommendcount * (temp / 10);
                    int cfCount = 0;
                    for (Map.Entry<String, Float> entry : itemtagmap.entrySet()) {
                        if (cfCount++ > cfNum) break;
                        allmap.put(entry.getKey(),entry.getValue());

                    }
                    int contentCount = 0;
                    for (Map.Entry<String, Float> entry : itemmap.entrySet()) {
                        if (contentCount++ > recommendcount-cfNum) break;
                        allmap.put(entry.getKey(),entry.getValue());
                    }
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

    public static void recommendByTag()
    {
        for (int recommendcount = 100;recommendcount<=100;recommendcount+=50)
        {
            System.out.println("推荐数："+recommendcount);
            for (float temp = 10;temp <= 10;temp++)
            {
                System.out.println("根据用户记录的"+temp/10+"推荐");
                float avgprecision = 0, avgrecall = 0;
                int usercounts = 0; //标签数大于阈值的用户才推荐
                for (String user : userRecordMap.keySet()) {
                    float num = userRecordMap.get(user).getItemTags().size()*(temp/10);

                    usercounts++;
                    Map<String,Float> itemtagmap = sortByValue(BayesScoring.getItemscoreByTag(user,temp/10));
                    Iterator<Map.Entry<String, Float>> it = itemtagmap.entrySet().iterator();
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
                        precision = (float)hitcounts / (float) itemtagmap.size();
                        recall = (float) hitcounts / (float) ur.size();
                    }
                    else
                    {
                        precision = 0;
                        recall = 0;
                    }
                    avgprecision += precision;
                    avgrecall += recall;
                    System.out.println("用户推荐数："+itemtagmap.size()+";"+user+"的准确率："+precision+";召回率："+recall);

                }
                System.out.println("计算的用户数："+usercounts);
                System.out.println("平均准确率："+avgprecision/usercounts+"  平均召回率："+avgrecall/usercounts);
            }
        }
    }
    /**
     * 根据item-tag/tag推荐
     */
    public static void recommendByItemTag(int startNum,int endNum,int shift)
    {
        for (int recommendcount = startNum; recommendcount <= endNum; recommendcount += shift) {
            int resUserCount = 0;
            System.out.println("推荐数：" + recommendcount);
            for (float temp = 1; temp <= 10; temp ++) {
                float avgprecision = 0, avgrecall = 0;
                int usercounts = 0; //标签数大于阈值的用户才推荐
                //根据物品标签对寻找用户和物品
                for (String user : userRecordMap.keySet()) {
                    float num = userRecordMap.get(user).getItemTags().size() * (temp / 10);
                    usercounts++;
                    Map<String, Float> itemtagmap = BayesScoring.getItemTagsScore(user,7.9f);
                    if (itemtagmap.size() == 0) {
                        continue;
                    }
                    //被推荐的用户数
                    resUserCount++;
                    Map<String, Float> sortmap = sortByValue(itemtagmap);
                    Map<String, Float> ur = userRecordMap.get(user).getItems();
                    int hitcounts = 0, count = 0;
                    for (Map.Entry<String, Float> resIt : sortmap.entrySet()) {
                        if (++count > recommendcount) break;
                        if (ur.containsKey(resIt.getKey())) hitcounts++;
                    }

                    float precision, recall;
                    if (hitcounts != 0) {
                        precision = (float) hitcounts / count;
                        recall = (float) hitcounts / ur.size();
                    } else {
                        precision = 0;
                        recall = 0;
                    }
                    avgprecision += precision;
                    avgrecall += recall;
                }
                System.out.println("被推荐的用户数：" + usercounts);
                float averageP = avgprecision / usercounts;
                float averageR = avgrecall / usercounts;
                System.out.println("平均准确率：" + averageP + "  平均召回率：" + averageR + "  平均F-Measure：" + 2 * averageP * averageR / (averageR + averageP));
            }
        }
    }
    //初始化数据
    public static void initialData()
    {
        String pathname = "E:\\ADA_Project\\RecommenderSystem\\src\\main\\resources\\doubanset\\tags.dat";
        String pathname1 = "E:\\ADA_Project\\RecommenderSystem\\src\\main\\resources\\doubanset\\rates.dat";
        String itemscorepath = "E:\\ADA_Project\\RecommenderSystem\\src\\main\\resources\\doubanset\\bookscore.dat";
        //String itemtagpath = "E:\\ADA_Project\\RecommenderSystem\\src\\main\\resources\\doubanset\\books.dat";
        //初始化book_tag信息
        //ItemSimilarity.initialBookTags(itemtagpath);
        //初始化所有物品的评分
        BayesScoring.initialBookScore(itemscorepath);
        //初始化用户的item-tag记录
        BayesScoring.initialUserItemTagMap1(pathname);
        //初始化用户的阅读评分记录
        BayesScoring.initialUserRatingMap(pathname1);
    }
    //对map根据value排序
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map )
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
