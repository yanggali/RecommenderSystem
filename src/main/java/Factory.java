//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import model.Item;
import model.Tag;
import model.User;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Factory {
    String itemPath;
    String ratePath;
    String tagPath;
    ArrayList<Item> items;
    ArrayList<User> users;
    ArrayList<Tag> tags;

    public Factory(String itemPath, String ratePath, String tagPath) {
        this.itemPath = itemPath;
        this.ratePath = ratePath;
        this.tagPath = tagPath;
        this.items = new ArrayList();
        this.users = new ArrayList();
        this.tags = new ArrayList();

        int i;
        for(i = 0; i < 66000; ++i) {
            Item item = new Item(i);
            this.items.add(item);
        }

        for(i = 0; i < 72000; ++i) {
            User user = new User(i);
            this.users.add(user);
        }

        for(i = 0; i < 16600; ++i) {
            Tag tag = new Tag(i);
            this.tags.add(tag);
        }

    }

    public void work() {
        this.makeItems();
        this.getItemAvgRate();
        this.getUserRate();
        this.getItemTags();
    }
    //初始化所有的物品
    private void makeItems() {
        try {
            FileReader fs = new FileReader(this.itemPath);
            BufferedReader br = new BufferedReader(fs);
            String record = br.readLine();

            while((record = br.readLine()) != null) {
                String[] str = record.split("\t");
                int itemId = Integer.parseInt(str[0]);
                String itemName = str[1];
                Item item = this.items.get(itemId);
                item.setHasRecord(true);
                item.setItemName(itemName);
            }
        } catch (IOException var8) {
            var8.printStackTrace();
        }

    }
    //获取物品的平均得分
    private void getItemAvgRate() {
        try {
            FileReader fs = new FileReader(this.ratePath);
            BufferedReader br = new BufferedReader(fs);
            String record = br.readLine();

            while((record = br.readLine()) != null) {
                String[] str = record.split("::");
                int userId = Integer.parseInt(str[0]);
                int itemId = Integer.parseInt(str[1]);
                float itemRate = Float.parseFloat(str[2]);
                Item item = (Item)this.items.get(itemId);
                item.getaRate(itemRate, userId);
            }
        } catch (IOException var9) {
            var9.printStackTrace();
        }

    }

    private void getUserRate() {
        try {
            FileReader fs = new FileReader(this.ratePath);
            BufferedReader br = new BufferedReader(fs);

            int itemId;
            User user;
            for(String record = br.readLine(); (record = br.readLine()) != null; user.rateaItem(itemId)) {
                String[] str = record.split("::");
                int userId = Integer.parseInt(str[0]);
                itemId = Integer.parseInt(str[1]);
                float itemRate = Float.parseFloat(str[2]);
                Item item = (Item)this.items.get(itemId);
                user = (User)this.users.get(userId);
                if(itemRate > item.getAvgRate()) {
                    item.getaPositiveRate();
                } else {
                    item.getaNegativeRate();
                }
            }
        } catch (IOException var10) {
            var10.printStackTrace();
        }

    }
    //douban
//    private void getItemTagsInDouban() {
//        try {
//            FileReader fs = new FileReader(this.tagPath);
//            BufferedReader br = new BufferedReader(fs);
//            String record = br.readLine();
//            while((record = br.readLine()) != null) {
//                String[] str = record.split("\t");
//                int userId = Integer.parseInt(str[0]);
//                int itemId = Integer.parseInt(str[1]);
//                for (String tagstr:str[2].split(" ")){
//                    User user = this.users.get(userId);
//                    Item item = this.items.get(itemId);
//                    Tag tag = this.tags.get(tagstr);
//                    user.tagaItem(itemId, tagId);
//                    user.setHasRecord(true);
//                    item.getaTag(userId);
//                    tag.tagaMovie(userId);
//                }
//
//            }
//        } catch (IOException var11) {
//            var11.printStackTrace();
//        }
//    }
    private void getItemTags() {
        try {
            FileReader fs = new FileReader(this.tagPath);
            BufferedReader br = new BufferedReader(fs);
            String record = br.readLine();
            while((record = br.readLine()) != null) {
                String[] str = record.split("::");
                int userId = Integer.parseInt(str[0]);
                int itemId = Integer.parseInt(str[1]);
                int tagId = Integer.parseInt(str[2]);
                User user = this.users.get(userId);
                Item item = this.items.get(itemId);
                Tag tag = this.tags.get(tagId);
                user.tagaItem(itemId, tagId);
                user.setHasRecord(true);
                item.getaTag(userId);
                tag.tagaMovie(userId);
            }
        } catch (IOException var11) {
            var11.printStackTrace();
        }
    }

    public ArrayList<Item> getItems() {
        return this.items;
    }

    public ArrayList<User> getUsers() {
        return this.users;
    }

    public ArrayList<Tag> getTags() {
        return this.tags;
    }
}
