package cz.mzk.k5.api.client;

import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.client.domain.Streams;
import cz.mzk.k5.api.common.InternalServerErroException;
import org.w3c.dom.Document;
import retrofit.http.GET;
import retrofit.http.Path;

import java.util.List;

/**
 * Created by holmanj on 8.2.15.
 */
public interface ClientRemoteApiXML {

    @GET("/item/{pid}/streams/{streamId}")
    Document getStream(@Path("pid") String pid, @Path("streamId") String streamId) throws InternalServerErroException;

    @GET("/item/{pid}/foxml")
    Document getFoxml(@Path("pid") String pid) throws InternalServerErroException;
}