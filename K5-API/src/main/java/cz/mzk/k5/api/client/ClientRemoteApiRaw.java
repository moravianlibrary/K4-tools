package cz.mzk.k5.api.client;

import cz.mzk.k5.api.common.K5ApiException;
import retrofit.http.GET;
import retrofit.http.Path;

import java.io.InputStream;

/**
 * Created by holmanj on 8.2.15.
 */
public interface ClientRemoteApiRaw {

    @GET("/item/{pid}/streams/{streamId}")
    InputStream getStream(@Path("pid") String pid, @Path("streamId") String streamId) throws K5ApiException;
}