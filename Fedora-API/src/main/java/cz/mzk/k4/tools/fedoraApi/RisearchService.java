package cz.mzk.k4.tools.fedoraApi;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Streaming;
import retrofit.http.Query;
import java.util.List;

/**
 * Created by rumanekm on 5.2.15.
 *
 * http://www.fedora.info/download/2.1.1/userdocs/server/webservices/risearch/
 */
public interface RisearchService {

    @GET("/risearch?type=triples&flush=true&lang=spo&format=N-Triples&distinct=off&stream=off")
    List<RelationshipTuple> query(@Query("query") String query, @Query("limit") int limit);

    @GET("/risearch?type=triples&flush=true&lang=spo&format=N-Triples&distinct=off&stream=off&limit=30")
    List<RelationshipTuple> query(@Query("query") String query);

    @Streaming
    @GET("/risearch?type=triples&flush=true&lang=spo&format=N-Triples&distinct=off&stream=on")
    Response queryStream(@Query("query") String query);
}

// Streaming to file:
//    try {
//        Response result = risearch.queryStream(query);
//        is = result.getBody().in();
//        br = new BufferedReader(new InputStreamReader(is));
//
//        String line;
//        while ((line = br.readLine()) != null) {
//            Files.write(resultFile, (line + "\n").getBytes(), StandardOpenOption.APPEND);
//        }
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
