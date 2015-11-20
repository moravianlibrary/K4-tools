package cz.mzk.k5.api.remote.domain;

import java.util.List;

/**
 * Created by holmanj on 16.11.15.
 */
public class ReplicatedObject {

    /*
     {
    "identifiers":["uuid:045b1250-7e47-11e0-add1-000d606f5dc6","issn:0862-6111"],
    "publishers":[],
    "creators":[],
    "title":"Dějiny a současnost",
    "type":"model:periodical",
    "handle":"http://localhost:8080/search/handle/uuid:045b1250-7e47-11e0-add1-000d606f5dc6"
}
     */

    private List<String> identifiers;
    private List<String> publishers;
    private List<String> creators;
    private String title;
    private String type;
    private String handle;

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    public List<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }

    public List<String> getCreators() {
        return creators;
    }

    public void setCreators(List<String> creators) {
        this.creators = creators;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    @Override
    public String toString() {
        return "ReplicatedObject{" +
                "identifiers=" + identifiers +
                ", publishers=" + publishers +
                ", creators=" + creators +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", handle='" + handle + '\'' +
                '}';
    }
}
