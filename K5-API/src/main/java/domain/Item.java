package domain;


/**
 * Created by holmanj on 5.2.15.
 */
public class Item {

    // identifikator zobrazovaneho pidu
    private String pid;
    // model
    private String model;
    private Handle handle;
//    private String title; // TODO zatím dělá problémy: https://github.com/ceskaexpedice/kramerius/issues/211
    // titulek korenoveho periodika (pripadne monografie)
    private String root_title;
    // identifikator korenoveho periodika
    private String root_pid;
    // kontext (pidy a modely po cestě ke kořeni - číslo, ročník, periodikum )
    private Context[][] context;
    // datový uzel - krom metadata nese i samotná data - obrázek
    private boolean datanode;
    // pro zobrazovaný titul je možno použít deep zoom prohlížečku
    private Zoom zoom;
    // pokud by stream IMG_FULL obsahoval pdf, byl by přítomen klíč pdf
    private Pdf pdf;
    // titul je součástí sbírky
    private String[] collections;
    // titul byl pořízen replikací - pid zdroje a replikované kopie
    private String[] replicatedFrom;
    private String policy;
    private Details details;

/*
{
     // identifikator zobrazovaneho pidu
    "pid": "uuid:ef065970-8137-11e0-85ae-000d606f5dc6",
    // model
    "model": "page",
    // titulek
    "title": [
        1
    ],
    // titulek korenoveho periodika (pripadne monografie)
    "root_title": "Dějiny a současnost",
    // identifikator korenoveho periodika
    "root_pid": "uuid:045b1250-7e47-11e0-add1-000d606f5dc6",
    // kontext
    "context": [
        [
            {
                "pid": "uuid:045b1250-7e47-11e0-add1-000d606f5dc6",
                "model": "periodical"
            },
            {
                "pid": "uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6",
                "model": "periodicalvolume"
            },
            {
                "pid": "uuid:91214030-80bb-11e0-b482-000d606f5dc6",
                "model": "periodicalitem"
            },
            {
                "pid": "uuid:ef065970-8137-11e0-85ae-000d606f5dc6",
                "model": "page"
            }
        ]
    ],
    // datový uzel - krom metadata nese i samotná data - obrázek
    "datanode": false,
    // pro zobrazovaný titul je možno použít deep zoom prohlížečku
    "zoom": {
        "url": "http://localhost:8080/search/deepZoom/uuid:ef065970-8137-11e0-85ae-000d606f5dc6",
        "type": "deepzoom"
    },

    // pokud by stream IMG_FULL obsahoval pdf, byl by přítomen klíč pdf
    //"pdf": {
    //    "url": "http://localhost:8080/search/....",
    //},

    // titul je součástí sbírky
    "collections": [
        "vc:f73dee31-ae76-4dbc-b7b9-d986df497596"
    ],

    // titul byl pořízen replikací
    "replicatedFrom": [
    // první zdroj - originál
        "http://kramerius.mzk.cz/search/handle/uuid:ef065970-8137-11e0-85ae-000d606f5dc6",
    // druhý zdroj - replikovaná kopie
        "http://cdk-test.lib.cas.cz/search/handle/uuid:ef065970-8137-11e0-85ae-000d606f5dc6"
    ]
}
 */

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Handle getHandle() {
        return handle;
    }

    public void setHandle(Handle handle) {
        this.handle = handle;
    }

//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }

    public String getRoot_title() {
        return root_title;
    }

    public void setRoot_title(String root_title) {
        this.root_title = root_title;
    }

    public String getRoot_pid() {
        return root_pid;
    }

    public void setRoot_pid(String root_pid) {
        this.root_pid = root_pid;
    }

    public Context[][] getContext() {
        return context;
    }

    public void setContext(Context[][] context) {
        this.context = context;
    }

    public boolean isDatanode() {
        return datanode;
    }

    public void setDatanode(boolean datanode) {
        this.datanode = datanode;
    }

    public Zoom getZoom() {
        return zoom;
    }

    public void setZoom(Zoom zoom) {
        this.zoom = zoom;
    }

    public Pdf getPdf() {
        return pdf;
    }

    public void setPdf(Pdf pdf) {
        this.pdf = pdf;
    }

    public String[] getCollections() {
        return collections;
    }

    public void setCollections(String[] collections) {
        this.collections = collections;
    }

    public String[] getReplicatedFrom() {
        return replicatedFrom;
    }

    public void setReplicatedFrom(String[] replicatedFrom) {
        this.replicatedFrom = replicatedFrom;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }
}
