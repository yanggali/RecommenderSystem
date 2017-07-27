package algorithms;

import structure.TagWeight;
import utils.FileIO;

import java.util.*;

/**
 * Created by Yangjiali on 2017/4/4 0004.
 * Version 1.0
 */
public class ItemSimilarity {
    public static Map<String, List<TagWeight>> movieTagMap = new HashMap<>();
    public static Map<String, List<TagWeight>> bookTagMap = new HashMap<>();
    public static void initialBookTags(String filepath)
    {
        List<String> list = FileIO.readFileByLines(filepath);
        for (String str:list)
        {
            String[] tagweight = str.split("\t");
            String[] taglist = tagweight[1].split(",");
            List<TagWeight> tagWeightsList = new LinkedList<>();
            for (int i=0;i < taglist.length;i++){
                TagWeight tagWeight = new TagWeight(taglist[i],taglist.length-i);
                tagWeightsList.add(tagWeight);
            }
            movieTagMap.put(tagweight[1],tagWeightsList);
        }
    }
    public static void initialMovieTags(String filepath)
    {
        List<String> list = FileIO.readFileByLines(filepath);
        for (String str:list)
        {
            String[] tagweight = str.split("::");
            if (!movieTagMap.containsKey(tagweight[0]))
            {
                TagWeight tagWeight = new TagWeight(tagweight[1], Integer.valueOf(tagweight[2]));
                List<TagWeight> tagWeights = new ArrayList<>();
                tagWeights.add(tagWeight);
                movieTagMap.put(tagweight[0],tagWeights);
            }
            else
            {
                //如果已经包含该电影
                TagWeight tagWeight = new TagWeight(tagweight[1], Integer.valueOf(tagweight[2]));
                movieTagMap.get(tagweight[0]).add(tagWeight);
            }
        }
    }

    /**
     * 计算当前item与其他所有item的相似度，并保留top num个
     * @param itemid,num
     * @return 其他item以及他们的相似度
     */
    public static Map<String,Float> getItemSimilarity(String itemid,int num)
    {
        Map<String,Float> itemSimilarity = new HashMap<>();
        if (movieTagMap.get(itemid) != null) {
            List<TagWeight> currentItem = movieTagMap.get(itemid);
            for (Map.Entry<String, List<TagWeight>> entry : movieTagMap.entrySet()) {
                float sum = 0;
                if (!entry.getKey().toString().equals(itemid)) {
                    List<TagWeight> otherItem = entry.getValue();
                    Set<String> intersection = new HashSet<>();
                    for (TagWeight tagWeight : currentItem) {
                        for (TagWeight tw : otherItem) {
                            if (tagWeight.getTagId() == tw.getTagId()) {
                                intersection.add(tagWeight.getTagId());
                                sum += (tagWeight.getTagWeight() - tw.getTagWeight()) * (tagWeight.getTagWeight() - tw.getTagWeight());
                            }
                        }
                    }
                    for (TagWeight tagWeight : currentItem) {
                        if (!intersection.contains(tagWeight.getTagId())) {
                            sum += tagWeight.getTagWeight() * tagWeight.getTagWeight();
                        }
                    }
                    for (TagWeight tw : otherItem) {
                        if (!intersection.contains(tw.getTagId())) {
                            sum += tw.getTagWeight() * tw.getTagWeight();
                        }
                    }
                    itemSimilarity.put(entry.getKey(), 1 / ((float)Math.sqrt(sum) + 1));
                }
            }
//            Iterator<Map.Entry<String, List<TagWeight>>> it = movieTagMap.entrySet().iterator();
//            while (it.hasNext()) {
//                float sum = 0;
//                Map.Entry<String, List<TagWeight>> entry = it.next();
//                if (!entry.getKey().toString().equals(itemid)) {
//                    List<TagWeight> otherItem = entry.getValue();
//                    Set<String> intersection = new HashSet<>();
//                    for (TagWeight tagWeight : currentItem) {
//                        for (TagWeight tw : otherItem) {
//                            if (tagWeight.getTagId() == tw.getTagId()) {
//                                intersection.add(tagWeight.getTagId());
//                                sum += (tagWeight.getTagWeight() - tw.getTagWeight()) * (tagWeight.getTagWeight() - tw.getTagWeight());
//                            }
//                        }
//                    }
//                    for (TagWeight tagWeight : currentItem) {
//                        if (!intersection.contains(tagWeight.getTagId())) {
//                            sum += tagWeight.getTagWeight() * tagWeight.getTagWeight();
//                        }
//                    }
//                    for (TagWeight tw : otherItem) {
//                        if (!intersection.contains(tw.getTagId())) {
//                            sum += tw.getTagWeight() * tw.getTagWeight();
//                        }
//                    }
//                    itemSimilarity.put(entry.getKey(), 1 / ((float)Math.sqrt(sum) + 1));
//                }

        } else {
            for (Map.Entry<String, List<TagWeight>> entry : movieTagMap.entrySet()) {
                float sum = 0;
                if (!entry.getKey().toString().equals(itemid)) {
                    List<TagWeight> otherItem = movieTagMap.get(entry.getKey());
                    for (TagWeight tw : otherItem) {
                        sum += tw.getTagWeight() * tw.getTagWeight();
                    }
                    itemSimilarity.put(entry.getKey(),1/((float)(Math.sqrt(sum))+1));
                }
            }
        }

        itemSimilarity = sortByValue(itemSimilarity,1);
        Map<String, Float> result = new HashMap<>();
        int count = 0;
        for (Map.Entry<String, Float> entry : itemSimilarity.entrySet()) {
            if (count++ < num) {
                result.put(entry.getKey(), entry.getValue());
            } else break;
        }
        result = sortByValue(result,1);
        return result;

    }
    public static float calSimilarity(int[] a,int[] b,int length)
    {
        float similarity = 0;
        for (int i = 0; i < length; i++) {
            similarity += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return 1/((float)Math.sqrt(similarity)+1);
    }

    /**
     * 根据用户的item-tag中的item找到相似item，每个用户只使用其中num个记录
     * @param userid
     * @return item及相应的距离
     */
    public static Map<String,Float> getItemMap(String userid,float per)
    {
        Map<String,Float> itemMap = new HashMap<>();
        Set<String> items = BayesScoring.getUsermap(1).get(userid).getItemsByItemTag();
        int count = 0;
        for (String item : items) {
            if (++count > items.size()*per) break;
            Map<String, Float> tempMap = getItemSimilarity(item, 400);
            addMap(itemMap, tempMap);
        }
        //将已阅读过的记录删除
        for (String item:items)
        {
            if (itemMap.containsKey(item)){
                itemMap.remove(item);
            }
        }
        itemMap = sortByValue(itemMap,1);
        return itemMap;
    }
    public static Map<String,Float> getItemMap(String userid)
    {
        Map<String,Float> itemMap = new HashMap<>();
        Set<String> items = BayesScoring.getUsermap(1).get(userid).getItemsByItemTag();
        int count = 0;
        for (String item : items) {
            Map<String, Float> tempMap = getItemSimilarity(item, 400);
            addMap(itemMap, tempMap);
        }
        //将已阅读过的记录删除
        for (String item:items)
        {
            if (itemMap.containsKey(item)){
                itemMap.remove(item);
            }
        }
        itemMap = sortByValue(itemMap,1);
        return itemMap;
    }
    private static void addMap(Map<String,Float> total,Map<String,Float> add)
    {
        Iterator<Map.Entry<String,Float>> it = add.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, Float> entry = it.next();
            //如果有多条路径指向该item，将相似度相加
            if (total.containsKey(entry.getKey())) {
                total.put(entry.getKey(), entry.getValue() + total.get(entry.getKey()));
            } else {
                total.put(entry.getKey(), entry.getValue());
            }
        }
    }
    //打印map
    public static <K, V extends Comparable<? super V>> void print( Map<K, V> map )
    {
        Iterator<Map.Entry<K,V>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K,V> entry = it.next();
            System.out.println("物品："+entry.getKey()+"，相似度："+entry.getValue());
        }
    }

    //对map根据value排序
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ,int type) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                if (type == 1){
                    return -(o1.getValue()).compareTo(o2.getValue());
                }
                else
                    return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
