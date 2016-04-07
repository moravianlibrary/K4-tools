package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by jan on 7.2.16.
 */
public class BatchK5Process implements Script {

    private static final Logger LOGGER = Logger.getLogger(BatchK5Process.class);
    private static FedoraUtils fedoraUtils = new FedoraUtils(AccessProvider.getInstance());
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    private final static int DELETE = 0;
    private final static int MAKE_PUBLIC = 1;
    private final static int MAKE_PRIVATE = 2;
    private final static int REINDEX = 3;

    @Override
    public void run(List<String> args) {
        // todo: parametry
        String filename = "IO/reindex";
        int process = DELETE;
        List<String> uuids = GeneralUtils.loadUuidsFromFile(filename);
        for (String uuid : uuids) {
            try {
                switch (process) {
                    case DELETE: remoteApi.deleteObject(uuid);
                        LOGGER.debug("Process DELETE planned for " + uuid);
                        break;
                    case MAKE_PUBLIC: remoteApi.setPublic(uuid);
                        LOGGER.debug("Process SET PUBLIC planned for " + uuid);
                        break;
                    case MAKE_PRIVATE: remoteApi.setPrivate(uuid);
                        LOGGER.debug("Process SET PRIVATE planned for " + uuid);
                        break;
                    case REINDEX: remoteApi.reindex(uuid);
                        LOGGER.debug("Process REINDEX planned for " + uuid);
                        break;
                }
            } catch (K5ApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getUsage() {
        return "Hromadné spuštění procesu v K5";
    }
}
