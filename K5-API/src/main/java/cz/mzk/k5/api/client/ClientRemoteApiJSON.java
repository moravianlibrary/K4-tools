package cz.mzk.k5.api.client;

import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.client.domain.Streams;
import cz.mzk.k5.api.common.InternalServerErroException;
import retrofit.http.*;
import java.util.*;

/**
 * Created by holmanj on 8.2.15.
 */
public interface ClientRemoteApiJSON {
    @GET("/item/{pid}")
    Item getItem(@Path("pid") String pid) throws InternalServerErroException; // uuid:...

    @GET("/item/{pid}/children")
    List<Item> getChildren(@Path("pid") String pid) throws InternalServerErroException; // uuid:...

    @GET("/item/{pid}/streams")
    Streams listStreams(@Path("pid") String pid) throws InternalServerErroException; // uuid:...
}