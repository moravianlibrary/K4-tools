package cz.mzk.k4.tools.fedoraApi;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by rumanekm on 5.2.15.
 *
 * http://www.fedora.info/download/2.1.1/userdocs/server/webservices/risearch/
 */
public interface RisearchService {

    @GET("/risearch?type=triples&flush=true&lang=spo&format=N-Triples&limit=30&distinct=off&stream=off")
    public String query(@Query("query") String query);
}
