package cz.mzk.k4.tools.api;

import domain.*;
import retrofit.http.*;
import java.util.*;

/**
 * Created by holmanj on 8.2.15.
 */
public interface ClientRemoteApiJSON {
    @GET("/item/{pid}")
    public Item getItem(@Path("pid") String pid) throws InternalServerErroException; // uuid:...

    @GET("/item/{pid}/children")
    public List<Item> getChildren(@Path("pid") String pid) throws InternalServerErroException; // uuid:...
}