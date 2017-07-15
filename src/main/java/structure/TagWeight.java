package structure;

/**
 * Created by Yangjiali on 2017/4/4 0004.
 * Version 1.0
 */
public class TagWeight {
    String tagId;
    int tagWeight;

    public TagWeight(String tagId, int tagWeight) {
        this.tagId = tagId;
        this.tagWeight = tagWeight;
    }

    public String getTagId() {
        return tagId;
    }

    public int getTagWeight() {
        return tagWeight;
    }

    public void setTagId(String tagId) {

        this.tagId = tagId;
    }

    public void setTagWeight(int tagWeight) {
        this.tagWeight = tagWeight;
    }
}
