package cz.mzk.k5.api.remote;

import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.domain.Parameters;
import cz.mzk.k5.api.remote.domain.Process;
import cz.mzk.k5.api.remote.domain.ProcessLog;
import cz.mzk.k5.api.remote.domain.ReplicatedObject;
import cz.mzk.k5.api.remote.domain.ReplicationTree;

import java.util.List;
import java.util.Map;

/**
 * Created by holmanj on 16.11.15.
 */
public class ProcessRemoteApi {

    private final String RUNNING = "RUNNING";
    private final String BATCH_STARTED = "BATCH_STARTED";
    private final String PLANNED = "PLANNED";

    ProcessRemoteApiRetrofit api;

    public ProcessRemoteApi(ProcessRemoteApiRetrofit api) {
        this.api = api;
    }

    // TODO: addToCollection

    public Process deleteObject(String pid_path) throws K5ApiException {
        return api.planProcess("delete", new Parameters(pid_path, pid_path));
    }

    public Process setPublic(String pid_path) throws K5ApiException {
        return api.planProcess("setpublic", new Parameters(pid_path, pid_path));
    }

    public Process setPrivate(String pid_path) throws K5ApiException {
        return api.planProcess("setprivate", new Parameters(pid_path, pid_path));
    }

    public Process export(String pid_path) throws K5ApiException {
        return api.planProcess("export", new Parameters(pid_path));
    }

    public Process sortRelations(String pid_path) throws K5ApiException {
        return api.planProcess("sort", new Parameters(pid_path, pid_path));
    }

    // parametry indexace viz https://github.com/ceskaexpedice/kramerius/wiki/MenuAdministrace#p%C5%99ehled-parametr%C5%AF-vol%C3%A1n%C3%AD-indexeru
    public Process reindex(String pid_path) throws K5ApiException {
        return api.planProcess("reindex", new Parameters("fromKrameriusModel", pid_path, pid_path));
    }

    // indexuje rekursivně dokument, který před tím nesmaže rekursivně z indexu
    public Process reindexWithoutRemoving(String pid_path) throws K5ApiException {
        return api.planProcess("reindex", new Parameters("fromKrameriusModelNoCheck", pid_path, pid_path));
    }

    // indexuje jen jednu úroveň dokumentu
    public Process reindexNonRecursive(String pid_path) throws K5ApiException {
        return api.planProcess("reindex", new Parameters("fromPid", pid_path, pid_path));
    }

    public Process reindexNewBranches(String pid_path) throws K5ApiException {
        return api.planProcess("reindex", new Parameters("reindexDoc", pid_path));
    }

    public Process deleteFromIndex(String pid_path) throws K5ApiException {
        return api.planProcess("reindex", new Parameters("deleteDocument", pid_path));
    }

    public Process reindexPid(String pid_path) throws K5ApiException {
        return api.planProcess("reindex", new Parameters("fromPid", pid_path));
    }

    public Process deletePidFromIndex(String pid_path) throws K5ApiException {
        return api.planProcess("reindex", new Parameters("deletePid", pid_path));
    }

    public Process optimizeSolr(String pid_path) throws K5ApiException {
        return api.planProcess("reindex", new Parameters("optimize", pid_path));
    }

    public Process reindexCollectionObjects(String pid_path) throws K5ApiException {
        return api.planProcess("reindex", new Parameters("reindexCollection", pid_path));
    }

//    public Process addToVirtualCollection(String object_pid, String vc_pid) throws K5ApiException {
//        return api.planProcess("??", new Parameters(??));
//    }

    public Process getProcess(String processUuid) throws K5ApiException {
        return api.getProcess(processUuid);
    }

    public ProcessLog getProcessLog(String processUuid) throws K5ApiException {
        return api.getProcessLog(processUuid);
    }

    public Process stopProcess(String processUuid) throws K5ApiException {
        return api.stopProcess(processUuid, new Parameters(""));
    }

    public void deleteProcessLog(String processUuid) throws K5ApiException {
        api.deleteProcessLog(processUuid);
    }

    public ReplicatedObject getReplicatedObjectInfo(String processPid) throws K5ApiException {
        return api.getReplicatedObjectInfo(processPid);
    }

    public ReplicationTree getReplicatedObjectTree(String processPid) throws K5ApiException {
        return api.getReplicatedObjectTree(processPid);
    }

    // endpoint replication umí vracet foxml, ale to už dělá clientApi

    // defaultně 50 procesů (asi)
    public List<Process> listProcesses() throws K5ApiException {
        return api.listProcesses();
    }

    public List<Process> listProcesses(int pocet) throws K5ApiException {
        return api.listProcesses(pocet);
    }

    // offset je od 0
    public List<Process> listProcesses(int pocet, int offset) throws K5ApiException {
        return api.listProcesses(pocet, offset);
    }

    /*
    Map<String, String> params = new HashMap<>();
    params.put("state", "KILLED");
    List<Process> processes = remoteApi.filterProcesses(params);
    processes.forEach(System.out::println);
     */
    // asi nefunguje v API
    public List<Process> filterProcesses(Map<String, String> filterMap) throws K5ApiException {
        return api.filterProcesses(filterMap);
    }

    public boolean isProcessRunning(String processUuid) throws K5ApiException {
        Process process = this.getProcess(processUuid);
        return process.getPid() != null && (RUNNING.equals(process.getState()) || BATCH_STARTED.equals(process.getBatchState()));
    }

    public boolean hasProcessEnded(String processUuid) throws K5ApiException {
        Process process = this.getProcess(processUuid);
        return !(RUNNING.equals(process.getState()) || BATCH_STARTED.equals(process.getBatchState()) || PLANNED.equals(process.getState()));
    }
}
