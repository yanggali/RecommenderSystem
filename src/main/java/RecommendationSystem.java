//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import model.Item;
import model.RsItem;
import model.Tag;
import model.User;

import java.util.*;
import java.util.Map.Entry;

public class RecommendationSystem {
    private ArrayList<Item> items;
    private ArrayList<User> users;
    private ArrayList<Tag> tags;
    private Map<Integer, RsItem> rsItems;
    private int ua;
    private int ub;
    private int va;
    private int vb;

    public RecommendationSystem(Factory factory) {
        this.items = factory.getItems();
        this.users = factory.getUsers();
        this.tags = factory.getTags();
        this.ua = 0;
        this.ub = 0;
        this.va = 0;
        this.vb = 0;
        this.rsItems = new HashMap<>();
    }

    public void setData(int ua, int ub, int va, int vb) {
        this.ua = ua;
        this.ub = ub;
        this.va = va;
        this.vb = vb;
    }

    //对目标用户推荐物品
    public ArrayList<RsItem> recommendItems(int userId) {
        User user = this.users.get(userId);
        Map<Integer, Integer> userTagItems = user.getTagItems();
        Iterator var5 = userTagItems.entrySet().iterator();

        while (var5.hasNext()) {
            Entry<Integer, Integer> entryU = (Entry) var5.next();
            Item itemU = this.items.get(entryU.getKey());
            Tag tagU = this.tags.get(entryU.getValue());
            int itemUA = itemU.getPositiveRateNum() + this.ua;
            int itemUB = itemU.getNegativeRateNum() + this.ub;
            float itemUScore = (float) (((double) itemUA + 1.0D) / ((double) (itemUA + itemUB) + 1.0D));
            List<Integer> tagUsers = tagU.getUsers();
            ArrayList<Integer> itemTagUsers = itemU.getTagUsers();
            Set<User> neighbours = new HashSet<>();

            int j;
            User neighbour;
            for (j = 0; j < tagUsers.size(); ++j) {
                neighbour = this.users.get(tagUsers.get(j));
                if (neighbour.getUserId() != userId) {
                    neighbours.add(neighbour);
                }
            }

            for (j = 0; j < itemTagUsers.size(); ++j) {
                neighbour = this.users.get(itemTagUsers.get(j));
                if (neighbour.getUserId()!=userId) {
                    neighbours.add(neighbour);
                }
            }
            for (User user1 : neighbours) {
                Map<Integer, Integer> neighbourTagItems = user1.getTagItems();
                Iterator var18 = neighbourTagItems.entrySet().iterator();
                //List<Integer> neighbourItems = user1.getRateItems();
                Set<Integer> neighbourItems = user1.getTagItems().keySet();
                for (Integer itemid : neighbourItems) {
                    Item itemV = this.items.get(itemid);
                    int itemVA = itemV.getPositiveRateNum() + this.va;
                    int itemVB = itemV.getNegativeRateNum() + this.vb;
                    float itemVScore = (float) (((double) itemVA + 1.0D) / ((double) (itemVA + itemVB) + 1.0D));
                    int itemId = itemV.getId();
                    RsItem rsItem;
                    if (this.rsItems.containsKey(itemId)) {
                        rsItem = this.rsItems.get(itemId);
                        this.rsItems.get(itemId).setScore(rsItem.getScore() + itemUScore * itemVScore);
                    } else {
                        rsItem = new RsItem(itemV);
                        this.rsItems.put(itemId, rsItem);
                    }
                }

            }
        }

        ArrayList<RsItem> sortedItems = new ArrayList(this.rsItems.values());
        sortedItems.sort((o1, o2) -> (new Float(o2.getScore()).compareTo(o1.getScore())));
        return sortedItems;
    }
}
