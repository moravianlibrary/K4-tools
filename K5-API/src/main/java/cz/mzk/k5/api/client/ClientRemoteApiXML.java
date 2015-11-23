package cz.mzk.k5.api.client;

import cz.mzk.k5.api.common.K5ApiException;
import org.w3c.dom.Document;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.QueryMap;

import java.util.Map;

/**
 * Created by holmanj on 8.2.15.
 */
public interface ClientRemoteApiXML {

    @GET("/item/{pid}/streams/{streamId}")
    Document getStream(@Path("pid") String pid, @Path("streamId") String streamId) throws K5ApiException;

    @GET("/item/{pid}/foxml")
    Document getFoxml(@Path("pid") String pid) throws K5ApiException;

    @GET("/search{query}")
    Document solrSearch(@Path(value="query", encode=false) String query) throws K5ApiException;
}