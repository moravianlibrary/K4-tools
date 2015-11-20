package cz.mzk.k5.api.client;

import cz.mzk.k5.api.common.InternalServerErroException;
import retrofit.http.*;

import java.io.InputStream;

/**
 * Created by holmanj on 8.2.15.
 */
public interface ClientRemoteApiString {

    @GET("/item/{pid}/streams/{streamId}")
    String getStream(@Path("pid") String pid, @Path("streamId") String streamId) throws InternalServerErroException;

}
