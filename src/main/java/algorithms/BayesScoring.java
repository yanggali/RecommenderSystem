package algorithms;

import structure.ItemTag;
import structure.UserRecord;
import utils.CalSimilarity;
import utils.DBHelper;
import utils.FileIO;

import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Yangjiali on 2017/3/24 0024.
 * Version 1.0
 */
public class BayesScoring {
    public static int maxRecord,maxCount;
    private static Map<String, UserRecord> usermap = new HashMap<>();
    private static Map<String,Float> itemscore = new HashMap<>();
    private static Map<ItemTag,List<String>> itemTagListMap = new HashMap<>();
    public static Map<String,List<String>> tagListMap = new HashMap<>();
    public static float bookAvgRate = 0,artistAvgRate = 0,avgRate = 0;
    public static Map<String, UserRecord> getUsermap(float ratio) {
        if (ratio == 1f) return usermap;
        for (Map.Entry<String, UserRecord> entry : usermap.entrySet()) {
            Set<ItemTag> initialSet = entry.getValue().getItemTags();
            int totallen = initialSet.size();
            float len = totallen * ratio;
            len = len < 1f ? 1 : len;
            Set<ItemTag> itemTagSet = entry.getValue().getItemTags().stream().limit((int) len).collect(Collectors.toSet());
            initialSet.clear();
            initialSet.addAll(itemTagSet);
        }
        return usermap;
    }

    //所有打过标签及其用户列表
    public static Map<String, Set<String>> tagUserMap = new HashMap<>();
    //所有物品及用户列表
    public static Map<String,Set<String>> itemUserMap = new HashMap<>();

    /**
     *
     * @param filepath item的评分
     */
    public static void initialItemScore(String filepath)
    {
        List<String> list = FileIO.readFileByLines(filepath);
        int count = 0;
        for (String str:list) {
            String[] record = str.split("::");
            itemscore.put(record[0],Float.valueOf(record[1]));
            avgRate += Float.valueOf(record[1]);
            count++;
        }
        avgRate/=count;
    }

    /**
     * 初始化豆瓣读书的评分
     * @param filepath
     */
    public static void initialBookScore(String filepath){
        List<String> list = FileIO.readFileByLines(filepath);
        float max = 0,min = 10;
        bookAvgRate = 0;
        int count = 0;
        for (String str : list) {
            count++;
            String[] record = str.split("::");
            bookAvgRate += Float.valueOf(record[1]);
            itemscore.put(record[0], Float.valueOf(record[1]));
            max = max > Float.valueOf(record[1]) ? max : Float.valueOf(record[1]);
            min = min < Float.valueOf(record[1]) ? min : Float.valueOf(record[1]);
        }
        for (Map.Entry<String, Float> entry : itemscore.entrySet()) {
            itemscore.put(entry.getKey(), (entry.getValue() - min) / (max - min));
        }
        bookAvgRate/=count;
        bookAvgRate = (bookAvgRate - min) / (max - min);
    }

    /** <2>
     * 根据用户的标签寻找相应的物品
     * @param userid
     * @return
     */
    public static Map<String,Float> getItemscoreByTag(String userid)
    {
        Map<String,Float> itemsscore = new HashMap<>();
        Set<ItemTag> userITSet = usermap.get(userid).getItemTags();
        Map<String, Integer> tagSet = usermap.get(userid).getTags();
        int count = 0;
        for (ItemTag itemTag:userITSet)
        {
            //用户对这个标签的标记次数
            float usertagcount = tagSet.get(itemTag.getTag());
            for (String otheritem : tagListMap.get(itemTag.getTag())) {

                if (!itemsscore.containsKey(otheritem)){
                    itemsscore.put(otheritem,usertagcount);
                }
                else {
                    itemsscore.put(otheritem,usertagcount+itemsscore.get(otheritem));
                }

            }
        }

        Set<String> items = usermap.get(userid).getItemsByItemTag();
        for (String item:items)
        {
            if (itemsscore.containsKey(item)){
                itemsscore.remove(item);
            }
        }
        return itemsscore;
    }

    public static Map<String,Float> getItemscoreByTag(String userid,float per)
    {
        Map<String,Float> itemsscore = new HashMap<>();
        Set<ItemTag> userITSet = usermap.get(userid).getItemTags();
        Map<String, Integer> tagSet = usermap.get(userid).getTags();
        int count = 0;
        for (ItemTag itemTag:userITSet)
        {
            if (++count > userITSet.size()*per) break;
            //用户对这个标签的标记次数
            float usertagcount = tagSet.get(itemTag.getTag());
            for (String item : tagListMap.get(itemTag.getTag())) {


                if (!itemsscore.containsKey(item)){
                    itemsscore.put(item,usertagcount);
                }
                else {
                    itemsscore.put(item,usertagcount+itemsscore.get(item));
                }
            }


//            for (UserRecord otheruser:usermap.values())
//            {
//                int itemcount = 0;
//                if (otheruser.isContainTag(itemTag.getTag()))
//                {
//                    for (ItemTag it:otheruser.getItemTags())
//                    {
//                        if (!itemsscore.containsKey(it.getItem()))
//                        {
//                            itemsscore.put(it.getItem(), usertagcount);
//                        }
//                        else
//                        {
//                            itemsscore.put(it.getItem(),usertagcount+itemsscore.get(it.getItem()));
//                        }
//                        //}
//                    }
//                }
//            }
        }
        Set<String> items = usermap.get(userid).getItemsByItemTag();
        for (String item:items)
        {
            if (itemsscore.containsKey(item)){
                itemsscore.remove(item);
            }
        }
        return itemsscore;
    }

    /**
     *
     * @param userId
     * @param itemScoreMap
     */
    public static void getBookScoreByContent(String userId,Map<String,Float> itemScoreMap){
        for (ItemTag itemTag : usermap.get(userId).getItemTags()) {
            Map<String,Float> itemSimMap = CalSimilarity.similarityListofBook(itemTag.getItem());
            for (Map.Entry<String, Float> item : itemSimMap.entrySet()) {
                if (!itemScoreMap.containsKey(item.getKey())){
                    itemScoreMap.put(item.getKey(),item.getValue());
                }else {
                    itemScoreMap.put(item.getKey(),itemScoreMap.get(item.getKey())+itemSimMap.get(item.getKey()));
                }
            }
        }
    }

    /**
     * 根据内容获取艺术家推荐列表
     * @param userId
     * @param itemScoreMap
     */
    public static void getArtistScoreByContent(String userId,Map<String,Float> itemScoreMap){
//        for (ItemTag itemTag : usermap.get(userId).getItemTags()) {
//            Map<String,Float> itemSimMap = CalSimilarity.similarityListofArtist(itemTag.getItem());
//            for (Map.Entry<String, Float> item : itemSimMap.entrySet()) {
//                if (!itemScoreMap.containsKey(item.getKey())){
//                    itemScoreMap.put(item.getKey(),item.getValue());
//                }else {
//                    itemScoreMap.put(item.getKey(),itemScoreMap.get(item.getKey())+itemSimMap.get(item.getKey()));
//                }
//            }
//        }
        for (String item : usermap.get(userId).getItemsByItemTag()) {
            Map<String,Float> itemSimMap = CalSimilarity.similarityListofArtist(item);
            //System.out.println(itemSimMap.size());
            for (Map.Entry<String, Float> itemEntry : itemSimMap.entrySet()) {
                if (!itemScoreMap.containsKey(itemEntry.getKey())){
                    itemScoreMap.put(itemEntry.getKey(),itemEntry.getValue());
                }else {
                    itemScoreMap.put(itemEntry.getKey(),itemScoreMap.get(itemEntry.getKey())+itemSimMap.get(itemEntry.getKey()));
                }
            }
        }
    }
    /**
     * 根据内容推荐
     * @param userId
     * @return
     */
    public static void getItemScoreByContent(String userId,Map<String,Float> itemScoreMap){
        for (String item : usermap.get(userId).getItemsByItemTag()) {
            //Map<String,Float> itemSimMap = CalSimilarity.similarityList(item);
            Map<String,Float> itemSimMap = CalSimilarity.similarityListByTensor(item);
            for (Map.Entry<String, Float> itemEntry : itemSimMap.entrySet()) {
                if (!itemScoreMap.containsKey(itemEntry.getKey())){
                    itemScoreMap.put(itemEntry.getKey(),itemEntry.getValue());
                }else {
                    itemScoreMap.put(itemEntry.getKey(),itemScoreMap.get(itemEntry.getKey())+itemSimMap.get(itemEntry.getKey()));
                }
            }
        }
    }

    /**
     * 模型三
     * 根据所有关系求Item及其评分
     * @param userId
     * @return
     */
    public static Map<String,Float> getItemscoreByAll(String userId){
        //initialMax();
        //System.out.println(maxCount+" "+maxRecord);
        Map<String,Float> itemScoreMap = new HashMap<>();
        for (ItemTag itemTag : usermap.get(userId).getItemTags()) {
            float currentScore = itemscore.getOrDefault(itemTag.getItem(),avgRate);
//            //根据item-tag找用户
            for (String otherUser : itemTagListMap.get(itemTag)) {
                for (ItemTag it : usermap.get(otherUser).getItemTags()) {
                    float otherScore = itemscore.getOrDefault(it.getItem(),avgRate);
                    if (!itemScoreMap.containsKey(it.getItem())) {
                        itemScoreMap.put(it.getItem(), currentScore * otherScore);
                    }else {
                        itemScoreMap.put(it.getItem(), itemScoreMap.get(it.getItem()) + currentScore * otherScore);
                    }
                }
            }
//            //根据tag找用户
//            for (String otherUser : tagUserMap.get(itemTag.getTag())) {
//                for (ItemTag it : usermap.get(otherUser).getItemTags()) {
//                    float otherScore = itemscore.getOrDefault(it.getItem(),avgRate);
//                    if (!itemScoreMap.containsKey(it.getItem())) {
//                        itemScoreMap.put(it.getItem(), currentScore * otherScore);
//                    }else {
//                        itemScoreMap.put(it.getItem(), itemScoreMap.get(it.getItem()) + currentScore * otherScore);
//                    }
//                }
//            }
//            //根据item找用户
//            for (String otherUser : itemUserMap.get(itemTag.getItem())) {
//                for (ItemTag it : usermap.get(otherUser).getItemTags()) {
//                    float otherScore = itemscore.getOrDefault(it.getItem(),avgRate);
//                    if (!itemScoreMap.containsKey(it.getItem())) {
//                        itemScoreMap.put(it.getItem(), currentScore * otherScore);
//                    }else {
//                        itemScoreMap.put(it.getItem(), itemScoreMap.get(it.getItem()) + currentScore * otherScore);
//                    }
//                }
//            }
            //根据tag找到item
//            for (String item : tagListMap.get(itemTag.getTag())) {
//                float otherScore = itemscore.getOrDefault(item,avgRate);
//                if (!itemScoreMap.containsKey(item)) {
//                    itemScoreMap.put(item, currentScore * otherScore);
//                }else {
//                    itemScoreMap.put(item, itemScoreMap.get(item) + currentScore * otherScore);
//                }
//            }

            //根据item的相似度计算item
//            Map<String,Float> itemSimMap = CalSimilarity.similarityList(itemTag.getItem());
//            for (Map.Entry<String, Float> item : itemSimMap.entrySet()) {
////                float weight = getWeight(userId,item.getKey());
////                System.out.println("用户"+userId+"与物品"+item.getKey()+"的权重是:"+weight);
//                if (!itemScoreMap.containsKey(item.getKey())){
//                    itemScoreMap.put(item.getKey(),item.getValue());
//                }else {
//                    itemScoreMap.put(item.getKey(),itemScoreMap.get(item.getKey())+itemSimMap.get(item.getKey()));
//                }
//            }

        }
//        normMap(itemScoreMap,0.5f);
//        Map<String,Float> simMap = new HashMap<>();
//        //getItemScoreByContent(userId,simMap);
//        getArtistScoreByContent(userId,simMap);
//        //getBookScoreByContent(userId,simMap);
//        normMap(simMap,0.5f);
//        for (Map.Entry<String, Float> entry : simMap.entrySet()) {
//            if (!itemScoreMap.containsKey(entry.getKey())){
//                itemScoreMap.put(entry.getKey(),entry.getValue());
//            }else {
//                itemScoreMap.put(entry.getKey(),entry.getValue()+itemScoreMap.get(entry.getKey()));
//            }
//        }
        return ItemSimilarity.sortByValue(itemScoreMap);
    }

    /**
     * 初始化用户最多的阅读数maxRecord和物品最大的被阅读数maxCount
     */
    public static void initialMax() {
        maxRecord = 0;
        for (Map.Entry<String, UserRecord> entry : usermap.entrySet()) {
            maxRecord = entry.getValue().getItemsByItemTag().size() > maxRecord ? entry.getValue().getItemsByItemTag().size() : maxRecord;
        }
        maxCount = 0;
        for (Map.Entry<String, Set<String>> entry : itemUserMap.entrySet()) {
            maxCount = entry.getValue().size() > maxCount ? entry.getValue().size() : maxCount;
        }
    }
    public static float getWeight(String userId, String itemId) {
        float active = (float) usermap.get(userId).getItemsByItemTag().size() / (float) maxRecord;
        System.out.println(itemUserMap.get(itemId).size()+" "+maxCount);
        float fresh = 1 - ((float)itemUserMap.get(itemId).size() / (float) maxCount);
        return fresh / active;
    }

    //归一化评分
    public static void normMap(Map<String, Float> itemScore,float weight) {
        float min = 1000,max = 0;
        for (Float aFloat : itemScore.values()) {
            min = aFloat < min ? aFloat : min;
            max = aFloat > max ? aFloat : max;
        }
        for (Map.Entry<String, Float> entry : itemScore.entrySet()) {
            itemScore.put(entry.getKey(),weight*(entry.getValue()-min)/(max-min));
        }
    }
    /**
     * 模型三，调节权重
     * @param userId
     * @param itemScoreMap
     * @return
     */
    public static Map<String, Float> getItemScoreofModel3ByAdjust(String userId, Map<String, Float> itemScoreMap,float weight) {
        getItemScoreofModel2(userId,itemScoreMap);
        getItemScoreofModel1(userId,itemScoreMap);
        normMap(itemScoreMap,weight);
        Map<String,Float> simMap = new HashMap<>();
        //getItemScoreByContent(userId,simMap);
        //getArtistScoreByContent(userId,simMap);
        getBookScoreByContent(userId,simMap);
        normMap(simMap,1-weight);
        for (Map.Entry<String, Float> entry : simMap.entrySet()) {
            if (!itemScoreMap.containsKey(entry.getKey())){
                itemScoreMap.put(entry.getKey(),entry.getValue());
            }else {
                itemScoreMap.put(entry.getKey(),entry.getValue()+itemScoreMap.get(entry.getKey()));
            }
        }
        return ItemSimilarity.sortByValue(itemScoreMap);
    }
    public static Map<String, Float> getItemScoreofModel3(String userId, Map<String, Float> itemScoreMap) {
        getItemScoreofModel2(userId,itemScoreMap);
        getItemScoreofModel1(userId, itemScoreMap);
        normMap(itemScoreMap,0.5f);
        Map<String,Float> simMap = new HashMap<>();
        getItemScoreByContent(userId,simMap);
        //getArtistScoreByContent(userId,simMap);
        //getBookScoreByContent(userId,simMap);
        normMap(simMap,0.5f);
        for (Map.Entry<String, Float> entry : simMap.entrySet()) {
            if (!itemScoreMap.containsKey(entry.getKey())){
                itemScoreMap.put(entry.getKey(),entry.getValue());
            }else {
                itemScoreMap.put(entry.getKey(),entry.getValue()+itemScoreMap.get(entry.getKey()));
            }
        }


//        for (String item : usermap.get(userId).getItemsByItemTag()) {
//            float currentScore = itemscore.getOrDefault(item, 0.57223f);
//            //根据item的相似度计算item
//            Map<String, Float> itemSimMap = CalSimilarity.similarityList(item);
//            for (Map.Entry<String, Float> itemEntry : itemSimMap.entrySet()) {
//                if (!itemScoreMap.containsKey(itemEntry.getKey())) {
//                    itemScoreMap.put(itemEntry.getKey(), itemEntry.getValue());
//                } else {
//                    itemScoreMap.put(itemEntry.getKey(), itemScoreMap.get(itemEntry.getKey()) + itemEntry.getValue());
//                }
//            }
//        }
        return ItemSimilarity.sortByValue(itemScoreMap);
    }
    /**
     * 模型一
     * @param userId
     * @return
     */
    public static void getItemScoreofModel1(String userId,Map<String ,Float> itemScoreMap) {
        for (ItemTag itemTag : usermap.get(userId).getItemTags()) {
            float currentScore = itemscore.getOrDefault(itemTag.getItem(), avgRate);
            if (!itemTag.getTag().equals("null")) {
                for (String otherUser : tagUserMap.get(itemTag.getTag())) {
                    if (!userId.equals(otherUser)) {
                        for (ItemTag it : usermap.get(otherUser).getItemTags()) {
                            float otherScore = itemscore.getOrDefault(it.getItem(), avgRate);
                            if (!itemScoreMap.containsKey(it.getItem())) {
                                itemScoreMap.put(it.getItem(), currentScore * otherScore);
                            } else {
                                itemScoreMap.put(it.getItem(), itemScoreMap.get(it.getItem()) + currentScore * otherScore);
                            }
                        }
                    }
                }
            }
        }
        for (String it : usermap.get(userId).getItemsByItemTag()) {
            float currentScore = itemscore.getOrDefault(it, avgRate);
            //根据item找用户
            for (String otherUser : itemUserMap.get(it)) {
                if (!otherUser.equals(userId)){
                    for (String item : usermap.get(otherUser).getItemsByItemTag()) {
                        float otherScore = itemscore.getOrDefault(item, avgRate);
                        if (!itemScoreMap.containsKey(item)) {
                            itemScoreMap.put(item, currentScore * otherScore);
                        } else {
                            itemScoreMap.put(item, itemScoreMap.get(item) + currentScore * otherScore);
                        }
                    }
                }

            }
        }
        normMap(itemScoreMap,1);
//        return ItemSimilarity.sortByValue(itemScoreMap);
    }

    /**
     * 模型二
     * @param userId
     * @param itemScoreMap
     */
    public static void getItemScoreofModel2(String userId,Map<String,Float> itemScoreMap){
        for (ItemTag itemTag : usermap.get(userId).getItemTags()) {
            if (!itemTag.getTag().equals("null")){
                float currentScore = itemscore.getOrDefault(itemTag.getItem(), avgRate);
                //根据item-tag找用户
                for (String otherUser : itemTagListMap.get(itemTag)) {
                    for (String item : usermap.get(otherUser).getItemsByItemTag()) {
                        float otherScore = itemscore.getOrDefault(item, avgRate);
                        if (!itemScoreMap.containsKey(item)) {
                            itemScoreMap.put(item, currentScore * otherScore);
                        } else {
                            itemScoreMap.put(item, itemScoreMap.get(item) + currentScore * otherScore);
                        }
                    }
                }
            }

        }
        normMap(itemScoreMap,1);
    }
    public static Map<String,Float> getItemTagsScore(String userid,float avgrate) {
        Map<String, Float> itemsscore = new HashMap<>();      //存放item的评分
        UserRecord user = usermap.get(userid);
        for (ItemTag it : user.getItemTags()) {
            float itscore = itemscore.getOrDefault(it.getItem(),avgrate);
            float useritscore = itscore;
            for (String otheruser : itemTagListMap.get(it)) {
                if (!otheruser.equals(userid)) {
                    int itemcount = 0;
                    for (String otherit : usermap.get(otheruser).getItems().keySet()) {
                        float otheritscore = itemscore.getOrDefault(otherit, avgrate);
                        float currentitscore = otheritscore + useritscore;
                        if (!itemsscore.containsKey(otherit)) {
                            itemsscore.put(otherit, currentitscore);
                        } else {
                            float score = itemsscore.get(otherit);
                            itemsscore.put(otherit, score + currentitscore);
                        }
                    }
                }
            }
        }
        Set<String> items = user.getItemsByItemTag();
        for (String item : items) {
            if (itemsscore.containsKey(item)) {
                itemsscore.remove(item);
            }
        }
        return itemsscore;
    }
    /**  <1>
     * @param userid 待推荐用户id
     * @return     推荐的item-tag
     */
    public static Map<String,Float> getItemTagsScore(String userid,float avgrate,float per) {
        Map<String, Float> itemsscore = new HashMap<>();      //存放item的评分
        UserRecord user = usermap.get(userid);
        for (ItemTag it : user.getItemTags()) {
            float itscore = itemscore.getOrDefault(it.getItem(),0.57223f);
            float useritscore = itscore;
            for (String otheruser : itemTagListMap.get(it)) {
                if (!otheruser.equals(userid)) {
                    int itemcount = 0;
                    for (String otherit : usermap.get(otheruser).getItems().keySet()) {
                        if (++itemcount > usermap.get(otheruser).getItems().size()*per) break;
                        float otheritscore = itemscore.getOrDefault(otherit, avgrate);
                        float currentitscore = otheritscore + useritscore;
                        if (!itemsscore.containsKey(otherit)) {
                            itemsscore.put(otherit, currentitscore);
                        } else {
                            float score = itemsscore.get(otherit);
                            itemsscore.put(otherit, score + currentitscore);
                        }
                    }
                }
            }
        }
        Set<String> items = user.getItemsByItemTag();
        for (String item : items) {
            if (itemsscore.containsKey(item)) {
                itemsscore.remove(item);
            }
        }
        return itemsscore;
    }


    /**
     * 问题！！！三步之后得到的物品太多
     * @param userid 待推荐用户id
     * @return    user-item-user-items之后得到的物品及评分集合
     */
    public static Map<String,Float> getItemsScore(String userid)
    {
        Map<String, Float> itemMap = new HashMap<>();
        UserRecord userRecord = usermap.get(userid);
        Map<String,Float> items = userRecord.getItems();
        Iterator<Map.Entry<String,Float>> it = items.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, Float> entry = it.next();
            //计算该物品的评分
            float useritscore = entry.getValue()*itemscore.get(entry.getKey());
            for (UserRecord user:usermap.values())
            {
                if (!user.equals(userid)&&user.isContainItem(entry.getKey()))
                {
                    System.out.println("根据"+entry.getKey()+"找到用户"+user.getUserid());
                    //计算该用户所有的item评分
                    for(String otherit:user.getItems().keySet())
                    {
                        float otheritscore = itemscore.get(otherit);
                        if (!itemMap.containsKey(otherit))
                        {
                            System.out.println("计算物品"+otherit+"的得分为:"+useritscore*otheritscore);
                            itemMap.put(otherit,useritscore*otheritscore);
                        }
                        else
                        {
                            float score = itemMap.get(otherit)+otheritscore*useritscore;
                            System.out.println("计算已存在物品"+otherit+"的得分为:"+score);
                            itemMap.put(otherit,score);
                        }
                    }
                }
            }
        }
        return itemMap;
    }

    public static float scoringFunction(String itemid)
    {
        DBHelper dbHelper1,dbHelper0;
        float Rp=0,Rm=0,score = 0;
        try {
            String sql1="SELECT count(*) FROM user_ratedmovies WHERE movieId='"+itemid+"' AND rate='1'";
            String sql0="SELECT count(*) FROM user_ratedmovies WHERE movieId='"+itemid+"' AND rate='-1'";
            dbHelper1 = new DBHelper(sql1);
            dbHelper0 = new DBHelper(sql0);
            ResultSet resSet1,resSet0;
            resSet1 = dbHelper1.pst.executeQuery();
            resSet0 = dbHelper0.pst.executeQuery();
            while (resSet1.next())
            {
                Rp = Float.valueOf(resSet1.getString(1));
            }
            while (resSet0.next())
            {
                Rm = Float.valueOf(resSet0.getString(1));
            }
            score = (Rp+1)/(Rp+Rm+1);
            //System.out.println("Rateplus:"+Rp+" Rateminus:"+Rm+" score:"+score);
            dbHelper0.close();
            dbHelper1.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return score;
    }
    /**
     * get recommended item-tag list after three-step for a user
     */
    public static Set<ItemTag> getItemTags(String userid)
    {
        UserRecord userRecord = usermap.get(userid);
        Set<UserRecord> userslist = new HashSet<>();  //存放两步之后可到达的用户
        Set<ItemTag> itemTagsList = userRecord.getItemTags();
        Set<ItemTag> itset = new HashSet<>();   //存放三步后所有可到达的item-tags
        for (ItemTag it:itemTagsList)
        {
            for (UserRecord user: usermap.values()) {
                if (!user.equals(userid)&&user.isContainItemTag(it))
                {
                    //userslist.add(user);
                    itset.addAll(user.getItemTags());
                    itset.remove(it);

                }
            }
        }
        //System.out.println("用户数："+userslist.size());
//        for (UserRecord user:userslist)
//        {
//            itset.addAll(user.getItemTags());
//        }
        return itset;
    }
    /**
     * initial rating record list of all users
     */
    public static void initialUserRatingMap(String filepath)
    {

        Map<String,List<Float>> itemScoreList = new HashMap<>();
        List<String> list = FileIO.readFileByLines(filepath);
        for (String str:list)
        {
            String[] record = str.split("::");
            if (!itemScoreList.containsKey(record[1])){
                List<Float> scoreList = new ArrayList<>();
                scoreList.add(Float.valueOf(record[2]));
                itemScoreList.put(record[1],scoreList);
            }
            else
            {
                itemScoreList.get(record[1]).add(Float.valueOf(record[2]));
            }
            if (!usermap.containsKey(record[0]))
            {
                UserRecord userRecord = new UserRecord();
                userRecord.setUserid(record[0]);
                userRecord.addItem(record[1],Float.valueOf(record[2]));
            }
            else
            {
                usermap.get(record[0]).addItem(record[1],Float.valueOf(record[2]));
//                UserRecord userRecord = usermap.get(record[0]);
//                if (userRecord.isContainItemInItemTag(record[1]))
//                {
//                    continue;
//                }
//                else
//                {
//                    usermap.get(record[0]).addItem(record[1],Float.valueOf(record[2]));
//                }
            }
        }
//        for (Map.Entry<String, List<Float>> entry : itemScoreList.entrySet()) {
//            if (!itemscore.containsKey(entry.getKey())) {
//                float avg = 0;
//                for (Float aFloat : entry.getValue()) {
//                    avg += aFloat;
//                }
//                avg/=(float) entry.getValue().size();
//                int plus = 0,minus = 0;
//                for (Float aFloat : entry.getValue()) {
//                    if (aFloat < avg) minus++;
//                    else plus++;
//                }
//                itemscore.put(entry.getKey(),(float)plus/(float)(plus+minus));
//            }
//        }
    }

    /**
     * 读取豆瓣图书中用户的物品标签记录
     * @param filepath
     */
    public static void initialUserItemTagMap1(String filepath)
    {
        List<String> list = FileIO.readFileByLines(filepath);
        for (String str:list)
        {
            String[] record = str.split("::");
            if (!itemUserMap.containsKey(record[1])) {
                Set<String> itemUserList = new HashSet<>();
                itemUserList.add(record[0]);
                itemUserMap.put(record[1],itemUserList);
            }
            else {
                itemUserMap.get(record[1]).add(record[0]);
            }
            List<String> tagList = new ArrayList<>();
            for (String tag : record[2].split(" ")) {
                tagList.add(tag);
                tag = tag.toLowerCase();
                if (!tagUserMap.containsKey(tag)) {
                    Set<String> tagUserList = new HashSet<>();
                    tagUserList.add(record[0]);
                    tagUserMap.put(tag, tagUserList);
                } else {
                    tagUserMap.get(tag).add(record[0]);
                }
                ItemTag it = new ItemTag(record[1], tag.toLowerCase());
                if (!itemTagListMap.containsKey(it)) {
                    List<String> users = new ArrayList<>();
                    users.add(record[0]);
                    itemTagListMap.put(it, users);
                } else {
                    itemTagListMap.get(it).add(record[0]);
                }
                if (!tagListMap.containsKey(it.getTag())) {
                    List<String> tagItems = new ArrayList<>();
                    tagItems.add(record[0]);
                    tagListMap.put(it.getTag(), tagItems);
                } else {
                    tagListMap.get(it.getTag()).add(record[1]);
                }
            }

            //初始化tag-userlistMap

            if (!usermap.containsKey(record[0]))
            {
                UserRecord userRecord = new UserRecord();
                userRecord.setUserid(record[0]);
                String[] tags = record[2].split(" ");
                for (String onetag:tags)
                {
                    userRecord.addTag(onetag, 1);
                    userRecord.addItemTag(record[1],onetag);
                }
                Map<String,List<String>> itemTagList = new HashMap<>();
                itemTagList.put(record[1],tagList);
                userRecord.setItemTagList(itemTagList);
                usermap.put(record[0],userRecord);
            }
            else
            {
                //如果已经包含该用户
                UserRecord userRecord = usermap.get(record[0]);
                userRecord.getItemTagList().put(record[1],tagList);
                String[] tags = record[2].split(" ");
                for (String onetag:tags)
                {
                    if (userRecord.getTags().get(onetag)!=null){
                        userRecord.getTags().put(onetag, userRecord.getTags().get(onetag) + 1);
                    }
                    else{
                        userRecord.addTag(onetag,1);
                    }
                    userRecord.addItemTag(record[1],onetag);
                }
                usermap.put(record[0], userRecord);
            }
        }
        System.out.println(list.size());
    }
    /**
     * initial tag record list of all users
     * tag and corresponding times,item-tag
     */
    public static void initialUserItemTagMap(String filepath)
    {
        List<String> list = FileIO.readFileByLines(filepath);
        for (String str:list)
        {
            String[] record = str.split("::");
            if (record.length <=2){
                System.out.println(str);
            }
            if (!tagUserMap.containsKey(record[2])){
                if (!record[2].equals("null")){
                    Set<String> tagUserList = new HashSet<>();
                    tagUserList.add(record[0]);
                    tagUserMap.put(record[2].toLowerCase(),tagUserList);
                }
            }else {
                tagUserMap.get(record[2]).add(record[0]);
            }
            if (!itemUserMap.containsKey(record[1])) {
                Set<String> itemUserList = new HashSet<>();
                itemUserList.add(record[0]);
                itemUserMap.put(record[1],itemUserList);
            }
            else {
                itemUserMap.get(record[1]).add(record[0]);
            }
            ItemTag it = new ItemTag(record[1], record[2]);
            //初始化tag-userlistMap
            if (!itemTagListMap.containsKey(it)){
                List<String> users = new LinkedList<>();
                users.add(record[0]);
                itemTagListMap.put(it,users);
            }else{
                itemTagListMap.get(it).add(record[0]);
            }
            if (!tagListMap.containsKey(record[2])){
                List<String> tagItems = new ArrayList<>();
                tagItems.add(record[1]);
                tagListMap.put(record[2],tagItems);
            }
            else{
                tagListMap.get(record[2]).add(record[1]);
            }
            if (!usermap.containsKey(record[0]))
            {
                UserRecord userRecord = new UserRecord();
                userRecord.setUserid(record[0]);
                userRecord.addTag(record[2],1);
                userRecord.addItemTag(record[1],record[2]);
                usermap.put(record[0],userRecord);
            }
            else
            {
                //如果已经包含该标签，则将该标签数加一
                if (usermap.get(record[0]).isContainTag(record[2]))
                {
                    int tagtimes = usermap.get(record[0]).getTagTimes(record[2]);
                    usermap.get(record[0]).addTag(record[2],tagtimes+1);
                    usermap.get(record[0]).addItemTag(record[1],record[2]);
                }
                else
                {
                    usermap.get(record[0]).addTag(record[2],1);
                    usermap.get(record[0]).addItemTag(record[1],record[2]);
                }

            }
        }
        System.out.println(list.size());
    }
}
