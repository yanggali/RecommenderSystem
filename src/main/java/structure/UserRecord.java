package structure;

import java.util.*;

/**
 * Created by Yangjiali on 2017/3/20 0020.
 * Version 1.0
 */
public class UserRecord {
    String userid;
    Map<String,Double> items;  //用户阅读记录及对应评分
    Map<String,Integer> tags;  //用户打过的标签及次数
    Set<ItemTag> itemTags;    //用户打过的itemtag
    Map<String,List<String>> itemTagList;   //用户物品及对应打的标签

    Map<String,Double> trainItems; //训练集
    Map<String,Double> testItems; //测试集

    public UserRecord() {
        items = new HashMap<>();
        tags = new HashMap<>();
        itemTags = new HashSet<>();
    }

    public UserRecord(String userid, Map<String,Double> items, Map<String,Integer> tags, Set<ItemTag> itemTags) {
        this.userid = userid;
        this.items = items;
        this.tags = tags;
        this.itemTags = itemTags;
    }
    public void setTrainItems(Map<String,Double> items){
        trainItems = items;
    }
    public void setTestItems(Map<String,Double> items){
        testItems = items;
    }
    public Map<String,Double> getTrainItems(){
        return trainItems;
    }
    public Map<String,Double> getTestItems(){
        return testItems;
    }
    public Map<String, List<String>> getItemTagList() {
        return itemTagList;
    }

    public void setItemTagList(Map<String, List<String>> itemTagList) {
        this.itemTagList = itemTagList;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
    public void addItem(String item,Double rate)
    {
        this.items.put(item,rate);
    }

    public Map<String, Double> getItems() {
        return items;
    }

    public void setItems(Map<String, Double> items) {
        this.items = items;
    }

    public void setTags(Map<String, Integer> tags) {
        this.tags = tags;
    }

    public void addTag(String tag,Integer times)
    {
        this.tags.put(tag.toLowerCase(),times);
    }

    public Map<String, Integer> getTags() {
        return tags;
    }

    public void addItemTag(String item, String tag)
    {
        ItemTag it = new ItemTag(item,tag.toLowerCase());
        itemTags.add(it);
    }

    public Set<ItemTag> getItemTags() {
        return itemTags;
    }



    public void setItemTags(Set<ItemTag> itemTags) {
        this.itemTags = itemTags;
    }
    public boolean isContainItem(String item)
    {
        return items.containsKey(item)?true:false;
    }
    public boolean isContainTag(String tag)
    {
        return tags.containsKey(tag.toLowerCase())?true:false;
    }
    public boolean isContainItemInItemTag(String item)
    {
        for (ItemTag temp:itemTags)
        {
            if(temp.getItem().equals(item))
            {
                return true;
            }
        }
        return false;
    }
    public boolean isContainItemTag(ItemTag it)
    {
        for (ItemTag temp:itemTags)
        {
            if(temp.equals(it))
            {
                return true;
            }
        }
        return false;
    }
    public int getTagTimes(String tag)
    {
        return tags.get(tag.toLowerCase());
    }
    public boolean equals(String id)
    {
        return userid.equals(id) ? true : false;
    }
    public Set<String> getTagsByItemTags(Set<String> items){
        Set<String> tags = new HashSet<>();
        for (ItemTag it : itemTags) {
            if (items.contains(it.getItem())){
                tags.add(it.getItem());
            }
        }
        return tags;
    }
    public Set<String> getTagsByItemTags(){
        Set<String> tags = new HashSet<>();
        for (ItemTag it:itemTags){
            tags.add(it.getTag());
        }
        return tags;
    }
    public Set<String> getItemsByItemTag()
    {
        Set<String> items = new HashSet<>();
        for (ItemTag it:itemTags)
        {
            items.add(it.getItem());
        }
        return items;
    }
}
