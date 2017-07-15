package structure;

/**
 * Created by Yangjiali on 2017/3/20 0020.
 * Version 1.0
 */
public class ItemTag {
    String item;
    String tag;
    public ItemTag()
    {

    }
    public ItemTag(String item, String tag) {
        this.item = item.toLowerCase();
        this.tag = tag.toLowerCase();
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemTag itemTag = (ItemTag) o;

        if (!item.equals(itemTag.item)) return false;
        return tag.equals(itemTag.tag);

    }

    @Override
    public int hashCode() {
        int result = item.hashCode();
        result = 31 * result + tag.hashCode();
        return result;
    }
    public void print()
    {
        System.out.print(item+"-"+tag+" ");
    }
    public String tostring()
    {
        return item+"_"+tag;
    }
}
