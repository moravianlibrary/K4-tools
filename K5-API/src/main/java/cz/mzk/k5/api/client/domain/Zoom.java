package cz.mzk.k5.api.client.domain;

/**
 * Created by holmanj on 8.2.15.
 */
public class Zoom {

    private String url;
    private String type;

    /*
    "zoom": {
        "url": "http://localhost:8080/search/deepZoom/uuid:ef065970-8137-11e0-85ae-000d606f5dc6",
        "type": "deepzoom"
    }
     */

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
