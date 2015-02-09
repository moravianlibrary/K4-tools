package cz.mzk.k4.tools.api;

import retrofit.http.*;

/**
 * Created by holmanj on 8.2.15.
 */
public interface ClientRemoteApiString {

    @GET("/item/{pid}/streams/TEXT_OCR")
    public String getOCR(@Path("pid") String pid); // uuid:...
}
