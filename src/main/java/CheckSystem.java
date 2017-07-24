//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import model.RsItem;
import model.User;

import java.text.NumberFormat;
import java.util.ArrayList;

public class CheckSystem {
    ArrayList<User> users;
    RecommendationSystem recommendationS;
    int recommendTotalNum;
    int checkNum;
    float avgAccuracy;
    float avgRecall;
    float avgFmeasure;

    public CheckSystem(int totalNum, ArrayList<User> users, RecommendationSystem recommendationS) {
        this.users = users;
        this.recommendTotalNum = totalNum;
        this.checkNum = 0;
        this.recommendationS = recommendationS;
    }
    //对标签数低于threshold的用户推荐
    public void checkAllData(int threshold) {
        this.checkNum = 0;
        for (int i = 0; i < this.users.size(); ++i) {
            if (this.users.get(i).isHasRecord()&&this.users.get(i).getTagItems().size() < threshold) {
                int rightNum = this.rightNum(i);
                float accuracy = (float) (1.0D * (double) rightNum / (double) this.recommendTotalNum);
                float recall = (float) (1.0D * (double) rightNum / (double) this.users.get(i).getRateItems().size());
                float fmeasure;
                if (accuracy == 0 | recall == 0) {
                    fmeasure= 0;
                }
                else
                {
                    fmeasure = (2 * accuracy * recall) / (accuracy + recall);
                }
                this.avgAccuracy += accuracy;
                this.avgRecall += recall;
                this.avgFmeasure += fmeasure;
                ++this.checkNum;
                System.out.println("用户"+users.get(i).getUserId()+"命中数为："+rightNum);
                System.out.println("用户"+users.get(i).getUserId()+"的准确率："+accuracy+"   召回率："+recall+"   fmeasure:"+fmeasure);
            }
        }

        this.avgAccuracy /= (float) this.checkNum;
        this.avgRecall /= (float) this.checkNum;
        this.avgFmeasure /= (float)this.checkNum;
        System.out.println("--------平均--------");
        System.out.println("准确率   召回率   F-measure");
        System.out.println(this.format(this.avgAccuracy) + "   " + this.format(this.avgRecall) + "    " + this.format(this.avgFmeasure));
    }
    //计算正确命中的个数
    private int rightNum(int userId) {
        int rightNum = 0;
        User user = this.users.get(userId);
        //得到目标用户待推荐候选物品
        ArrayList<RsItem> items = this.recommendationS.recommendItems(userId);

        for (int i = 0; i < this.recommendTotalNum; ++i) {
            RsItem item = items.get(i);
            if (user.hasRateItem(item.getItem().getId())&&!user.getTagItems().keySet().contains(item.getItem().getId())) {
                ++rightNum;
            }
        }

        return rightNum;
    }

    public void checkaResult(int userId) {
        int rightNum = 0;
        User user = this.users.get(userId);
        ArrayList<RsItem> items = this.recommendationS.recommendItems(userId);
        System.out.println("一共推荐了"+items.size()+"个物品");
        for (int i = 0; i < items.size(); i++) {
            RsItem item = items.get(i);
            System.out.println(item.getItem().getId()+":"+item.getScore());
        }
        System.out.println("__________________________");
        System.out.println("命中内容");

        for (int i = 0; i < this.recommendTotalNum; ++i) {
            RsItem item = items.get(i);
            if (user.hasRateItem(item.getItem().getId())&&!user.getTagItems().containsValue(item.getItem().getId())) {
                ++rightNum;
                System.out.println(item.getItem().getId());
            }
        }

        System.out.println("__________________________");
        System.out.println("命中个数");
        System.out.println(rightNum);
        float accuracy = (float) (1.0D * (double) rightNum / (double) this.recommendTotalNum);
        float recall = (float) (1.0D * (double) rightNum / (double) this.users.get(userId).getRateItems().size());
        float fmeasure;
        if (accuracy == 0 || recall == 0) {
            fmeasure = 0;
        }else {
            fmeasure = 2 * accuracy * recall / (accuracy + recall);
        }
        System.out.println("__________________________");
        System.out.println("推荐内容");

        for (int i = 0; i < (items.size() > this.recommendTotalNum ? this.recommendTotalNum : items.size()); ++i) {
            RsItem item = items.get(i);
            System.out.println(item.getItem().getId() + "   " + item.getScore());
        }

        System.out.println("__________________________");
        System.out.println("准确率  召回率  F-measure");
        System.out.println(this.format(accuracy) + "   " + this.format(recall)+"    "+this.format(fmeasure));
        System.out.println("__________________________");
    }

    private String format(float data) {
        NumberFormat num = NumberFormat.getPercentInstance();
        num.setMaximumIntegerDigits(3);
        num.setMaximumFractionDigits(2);
        return num.format((double) data);
    }
}
