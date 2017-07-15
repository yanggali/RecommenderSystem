//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User {
    boolean hasRecord;
    int userId;
    ArrayList<Integer> rateItems;
    Map<Integer, Integer> tagItems;

    public User(int userId) {
        this.userId = userId;
        this.rateItems = new ArrayList();
        this.tagItems = new HashMap();
    }

    public boolean hasRateItem(int itemId) {
        for(int i = 0; i < this.rateItems.size(); ++i) {
            if (itemId == this.rateItems.get(i)) {
                return true;
            }
        }

        return false;
    }

    public void setHasRecord(boolean hasRecord) {
        this.hasRecord = hasRecord;
    }

    public void rateaItem(int itemId) {
        this.rateItems.add(Integer.valueOf(itemId));
    }

    public void tagaItem(int itemId, int tagId) {
        this.tagItems.put(Integer.valueOf(itemId), Integer.valueOf(tagId));
    }

    public boolean isHasRecord() {
        return this.hasRecord;
    }

    public int getUserId() {
        return this.userId;
    }

    public ArrayList<Integer> getRateItems() {
        return this.rateItems;
    }

    public Map<Integer, Integer> getTagItems() {
        return this.tagItems;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                '}';
    }
}
