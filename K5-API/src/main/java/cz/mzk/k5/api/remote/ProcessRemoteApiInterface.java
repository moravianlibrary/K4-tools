package cz.mzk.k5.api.remote;

import cz.mzk.k5.api.common.InternalServerErroException;
import cz.mzk.k5.api.remote.domain.Parameters;
import cz.mzk.k5.api.remote.domain.Process;
import cz.mzk.k5.api.remote.domain.ProcessLog;
import cz.mzk.k5.api.remote.domain.ReplicatedObject;
import cz.mzk.k5.api.remote.domain.ReplicationTree;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

import java.util.List;

/**
 * Created by holmanj on 16.11.15.
 */
public interface ProcessRemoteApiInterface {

    // parameters: 1. uuid je uuid, ka kterém se proces spustí, 2. uuid je název dokumentu do pole "name"
    @POST("/processes")
    public Process planProcess(@Query("def") String def, @Body Parameters parameters) throws InternalServerErroException;

    @GET("/processes/{uuid}")
    public Process getProcess(@Path("uuid") String uuid) throws InternalServerErroException;

    @GET("/processes/{uuid}/logs")
    public ProcessLog getProcessLog(@Path("uuid") String uuid) throws InternalServerErroException;

    @GET("/processes/{uuid}?stop")
    public Process stopProcess(@Path("uuid") String uuid) throws InternalServerErroException;

    @DELETE("/processes/{uuid}")
    public Process deleteProcess(@Path("uuid") String uuid) throws InternalServerErroException;

    @GET("/replication/{pid}")
    public ReplicatedObject getReplicatedObjectInfo(@Path("pid") String pid) throws InternalServerErroException;

    @GET("/replication/{pid}/tree")
    public ReplicationTree getReplicatedObjectTree(@Path("pid") String pid) throws InternalServerErroException;

    @GET("/processes")
    public List<Process> listProcesses() throws InternalServerErroException; // defaultně 25 procesů

    @GET("/processes")
    public List<Process> listProcesses(@Query("resultSize") int pocetProcesu) throws InternalServerErroException;

    @GET("/processes/?resultSize={pocet}&offset={offset}")
    public List<Process> listProcesses(@Query("pocet") int pocet, @Query("offset") int offset) throws InternalServerErroException;

//    @GET("/processes/?{field}={value}") // TODO: třídění podle víc parametrů - flexibilnější
//    public List<Item> filterProcesses(@Query("field") String field, @Query("value") String value) throws InternalServerErroException;
//
//    @GET("/processes/?{field}={value}&resultSize={pocet}&offset={offset}") // TODO: třídění podle víc parametrů - flexibilnější
//    public List<Item> filterProcesses(@Query("field") String field, @Query("value") String value, @Query("pocet") int pocet, @Query("offset") int offset) throws InternalServerErroException;
}
