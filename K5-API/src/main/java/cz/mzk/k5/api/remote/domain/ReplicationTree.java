package cz.mzk.k5.api.remote.domain;

import java.util.List;

/**
 * Created by holmanj on 16.11.15.
 */
public class ReplicationTree {

    /*
    {
    'pids':[
        'uuid:045b1250-7e47-11e0-add1-000d606f5dc6',
        'uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6',
        'uuid:91214030-80bb-11e0-b482-000d606f5dc6',
        'uuid:ef065970-8137-11e0-85ae-000d606f5dc6',
        'uuid:f5d05350-8137-11e0-9476-000d606f5dc6',
        'uuid:914e1c90-80bb-11e0-ab75-000d606f5dc6',
        ....
    ]
}
     */
    private List<String> pids;

    public List<String> getPids() {
        return pids;
    }

    public void setPids(List<String> pids) {
        this.pids = pids;
    }
}
