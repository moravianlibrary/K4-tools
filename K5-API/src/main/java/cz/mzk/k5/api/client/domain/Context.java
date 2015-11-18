package cz.mzk.k5.api.client.domain;

/**
 * Created by holmanj on 8.2.15.
 */
public class Context {

    private String pid;
    private String model;

    /*
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
    ]
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
}
