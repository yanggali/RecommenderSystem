//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package model;

import java.util.ArrayList;

public class Item {
    boolean hasRecord = false;
    String itemName;
    int id;
    float totalRate;
    int rateNum;
    float avgRate;
    int positiveRateNum;
    int negativeRateNum;
    ArrayList<Integer> rateUsers;
    ArrayList<Integer> tagUsers;

    public Item(int id) {
        this.id = id;
        this.totalRate = 0.0F;
        this.rateNum = 0;
        this.positiveRateNum = 0;
        this.negativeRateNum = 0;
        this.rateUsers = new ArrayList();
        this.tagUsers = new ArrayList();
    }

    public void getaRate(float rate, int userId) {
        this.totalRate += rate;
        ++this.rateNum;
        this.rateUsers.add(userId);
    }

    public void getaTag(int userId) {
        this.tagUsers.add(userId);
    }

    public String getItemName() {
        return this.itemName;
    }

    public ArrayList<Integer> getTagUsers() {
        return this.tagUsers;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void getaPositiveRate() {
        ++this.positiveRateNum;
    }

    public ArrayList<Integer> getRateUsers() {
        return this.rateUsers;
    }

    public void getaNegativeRate() {
        ++this.negativeRateNum;
    }

    public float getAvgRate() {
        if(this.hasRecord) {
            this.avgRate = this.totalRate / (float)this.rateNum;
            return this.avgRate;
        } else {
            return 2.5F;
        }
    }

    public void setHasRecord(boolean hasRecord) {
        this.hasRecord = hasRecord;
    }

    public boolean isHasRecord() {
        return this.hasRecord;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getTotalRate() {
        return this.totalRate;
    }

    public void setTotalRate(float totalRate) {
        this.totalRate = totalRate;
    }

    public int getRateNum() {
        return this.rateNum;
    }

    public void setRateNum(int rateNum) {
        this.rateNum = rateNum;
    }

    public int getPositiveRateNum() {
        return this.positiveRateNum;
    }

    public void setPositiveRateNum(int positiveRateNum) {
        this.positiveRateNum = positiveRateNum;
    }

    public int getNegativeRateNum() {
        return this.negativeRateNum;
    }

    public void setNegativeRateNum(int negativeRateNum) {
        this.negativeRateNum = negativeRateNum;
    }
}
