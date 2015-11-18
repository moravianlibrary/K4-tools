package cz.mzk.k5.api.remote;

import cz.mzk.k5.api.common.InternalServerErroException;
import cz.mzk.k5.api.remote.domain.Parameters;
import cz.mzk.k5.api.remote.domain.Process;
import cz.mzk.k5.api.remote.domain.ProcessLog;
import cz.mzk.k5.api.remote.domain.ReplicatedObject;
import cz.mzk.k5.api.remote.domain.ReplicationTree;

import java.util.List;

/**
 * Created by holmanj on 16.11.15.
 */
public class ProcessRemoteApi {

    ProcessRemoteApiInterface api;

    public ProcessRemoteApi(ProcessRemoteApiInterface api) {
        this.api = api;
    }

    // TODO: zkontrolovat parametry (zobrazování ve výpisu)
    // TODO: zkontrolovat reindexaci

    // TODO: searchByParams
    // TODO: addToCollection
    // TODO: replikace - get foxml - vracení XML nebo JSON: https://github.com/ceskaexpedice/kramerius/wiki/RemoteAPI#z%C3%ADsk%C3%A1n%C3%AD-foxml-objektu

    public Process deleteObject(String pid_path) throws InternalServerErroException {
        return api.planProcess("delete", new Parameters(pid_path, pid_path));
    }

    public Process setPublic(String pid_path) throws InternalServerErroException {
        return api.planProcess("setpublic", new Parameters(pid_path, pid_path));
    }

    public Process setPrivate(String pid_path) throws InternalServerErroException {
        return api.planProcess("setprivate", new Parameters(pid_path, pid_path));
    }

    public Process export(String pid_path) throws InternalServerErroException {
        return api.planProcess("export", new Parameters(pid_path));
    }

    // TODO parametry indexace: https://github.com/ceskaexpedice/kramerius/wiki/MenuAdministrace#p%C5%99ehled-parametr%C5%AF-vol%C3%A1n%C3%AD-indexeru
    public Process reindexRecursive(String pid_path) throws InternalServerErroException {
        return api.planProcess("reindex", new Parameters("fromKrameriusModel", pid_path, pid_path));
    }

    public Process reindexNewBranches(String pid_path) throws InternalServerErroException {
        return api.planProcess("reindex", new Parameters("reindexDoc", pid_path));
    }

    public Process getProcess(String processUuid) throws InternalServerErroException {
        return api.getProcess(processUuid);
    }

    public ProcessLog getProcessLog(String processUuid) throws InternalServerErroException {
        return api.getProcessLog(processUuid);
    }

    public Process stopProcess(String processUuid) throws InternalServerErroException {
        return api.stopProcess(processUuid);
    }

    public void deleteProcess(String processUuid) throws InternalServerErroException {
        api.deleteProcess(processUuid);
    }

    public ReplicatedObject getReplicatedObjectInfo(String processPid) throws InternalServerErroException {
        return api.getReplicatedObjectInfo(processPid);
    }

    public ReplicationTree getReplicatedObjectTree(String processPid) throws InternalServerErroException {
        return api.getReplicatedObjectTree(processPid);
    }

    public List<Process> listProcesses() throws InternalServerErroException {
        return api.listProcesses();
    }
}
