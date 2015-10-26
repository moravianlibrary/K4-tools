package cz.mzk.k4.tools.api;

import domain.Item;
import retrofit.RetrofitError;

import java.util.List;

/**
 * Created by holmanj on 8.2.15.
 * Posílá dotaz dál podle očekávaného typu dat (různá deserializase jsonu, stringu a xml)
 */
public class ClientRemoteApi {

    private ClientRemoteApiJSON apiJSON;
    private ClientRemoteApiString apiString;

    // získávat API přes factory:
    // ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    public ClientRemoteApi(ClientRemoteApiJSON apiJSON, ClientRemoteApiString apiString) {
        this.apiJSON = apiJSON;
        this.apiString = apiString;
    }

    public Item getItem(String pid) throws InternalServerErroException {
        return apiJSON.getItem(pid);
    }

    public List<Item> getChildren(String pid) throws InternalServerErroException {
        try {
            return apiJSON.getChildren(pid);
        } catch (RetrofitError error) {
            System.out.println(error.getUrl());
            throw error;
        }
    }

    public String getOCR(String pid) throws InternalServerErroException {
        return apiString.getOCR(pid);
    }
}
