package cz.mzk.k5.api.client.domain;

/**
 * Created by holmanj on 8.2.15.
 */
public class Handle {
    /*
    "handle":
   {
       "href": "http://krameriusndktest.mzk.cz/search/handle/uuid:d1ce1150-842b-11e3-b315-001018b5eb5c"
   }
     */
    private String href;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
