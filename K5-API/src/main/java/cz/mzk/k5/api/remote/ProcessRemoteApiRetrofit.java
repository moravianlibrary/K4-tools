package cz.mzk.k5.api.remote;

import com.google.gson.JsonObject;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.domain.Parameters;
import cz.mzk.k5.api.remote.domain.Process;
import cz.mzk.k5.api.remote.domain.ProcessLog;
import cz.mzk.k5.api.remote.domain.ReplicatedObject;
import cz.mzk.k5.api.remote.domain.ReplicationTree;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

import java.util.List;
import java.util.Map;

/**
 * Created by holmanj on 16.11.15.
 */
public interface ProcessRemoteApiRetrofit {

    // parameters: 1. uuid je uuid, ka kterém se proces spustí, 2. uuid je název dokumentu do pole "name"
    @POST("/processes")
    Process planProcess(@Query("def") String def, @Body Parameters parameters) throws K5ApiException;

    @GET("/processes/{uuid}")
    Process getProcess(@Path("uuid") String uuid) throws K5ApiException;

    @GET("/processes/{uuid}/logs")
    ProcessLog getProcessLog(@Path("uuid") String uuid) throws K5ApiException;

    @PUT("/processes/{uuid}?stop") // retrofit does not allow PUT with empty body
    Process stopProcess(@Path("uuid") String uuid, @Body Parameters fakeBody) throws K5ApiException;

    @DELETE("/processes/{uuid}")
    JsonObject deleteProcessLog(@Path("uuid") String uuid) throws K5ApiException;

    @GET("/replication/{pid}")
    ReplicatedObject getReplicatedObjectInfo(@Path("pid") String pid) throws K5ApiException;

    @GET("/replication/{pid}/tree")
    ReplicationTree getReplicatedObjectTree(@Path("pid") String pid) throws K5ApiException;

    @GET("/processes")
    List<Process> listProcesses() throws K5ApiException;

    @GET("/processes")
    List<Process> listProcesses(@Query("resultSize") int pocetProcesu) throws K5ApiException;

    @GET("/processes")
    List<Process> listProcesses(@Query("resultSize") int pocetProcesu, @Query("offset") int offset) throws K5ApiException;

    @GET("/processes")
    List<Process> filterProcesses(@QueryMap Map<String, String> fields) throws K5ApiException;
}
