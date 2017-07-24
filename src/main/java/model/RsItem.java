//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package model;

public class RsItem {
    float score;
    Item item;

    public RsItem(Item item) {
        this.item = item;
        this.score = 0.0F;
    }

    public float getScore() {
        return this.score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        } else if(this == obj) {
            return true;
        } else {
            if(obj instanceof RsItem) {
                RsItem compareItem = (RsItem)obj;
                if(this.item.getId() == compareItem.getItem().getId()) {
                    return true;
                }
            }

            return false;
        }
    }

    public int hashCode() {
        return (new Integer(this.item.getId())).hashCode();
    }
}
