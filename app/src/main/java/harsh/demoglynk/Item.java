package harsh.demoglynk;

public class Item {

    private String title;
    private String objectId;
    private String url;

    public Item() {
    }

    public String gettTitle() {
        return title;
    }

    public void settTitle(String title) {
        this.title = title;
    }

    public String gettObjectId() {
        return objectId;
    }

    public void settObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String gettURL() {
        return url;
    }

    public void settURL(String url) {
        this.url = url;
    }

}
