package cz.mzk.k4.tools.api;

import domain.*;
import java.util.*;

/**
 * Created by holmanj on 8.2.15.
 * Posílá dotaz dál podle očekávaného typu dat (různá deserializase jsonu, stringu a xml)
 */
public class ClientRemoteApi {

    private ClientRemoteApiJSON apiJSON;
    private ClientRemoteApiString apiString;

    public ClientRemoteApi(ClientRemoteApiJSON apiJSON, ClientRemoteApiString apiString) {
        this.apiJSON = apiJSON;
        this.apiString = apiString;
    }

    public Item getItem(String pid) {
        return apiJSON.getItem(pid);
    }

    public List<Item> getChildren(String pid) {
        return apiJSON.getChildren(pid);
    }

    public String getOCR(String pid) {
        return apiString.getOCR(pid);
    }
}
