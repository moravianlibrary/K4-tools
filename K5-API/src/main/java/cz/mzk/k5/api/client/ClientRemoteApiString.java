package cz.mzk.k5.api.client;

import cz.mzk.k5.api.common.InternalServerErroException;
import retrofit.http.*;

/**
 * Created by holmanj on 8.2.15.
 */
public interface ClientRemoteApiString {

    @GET("/item/{pid}/streams/TEXT_OCR")
    public String getOCR(@Path("pid") String pid) throws InternalServerErroException; // uuid:...
}
