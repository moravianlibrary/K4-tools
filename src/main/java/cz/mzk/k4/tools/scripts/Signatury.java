package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jan on 28.1.16.
 */
public class Signatury implements Script {
    private static final Logger LOGGER = LogManager.getLogger(Signatury.class);
    private AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(
            accessProvider.getKrameriusHost(),
            accessProvider.getKrameriusUser(),
            accessProvider.getKrameriusPassword());
    ProcessRemoteApi k5RemoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
            accessProvider.getKrameriusHost(),
            accessProvider.getKrameriusUser(),
            accessProvider.getKrameriusPassword());
    FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);

    public Signatury() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        // load sig. list
        List<String> signatury = null;
        try {
            signatury = FileUtils.readLines(new File("IO/signatury-odkazy-sbirka"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String signatura : signatury) {
            List<String> uuidList = null;
            try {
                uuidList = getUuidFromSignatura(signatura);
            } catch (FileNotFoundException e) {
                LOGGER.warn(e.getMessage());
                continue;
            }
            if (uuidList.size() != 1) {
                LOGGER.warn("Signatuře " + signatura + " odpovídá několik záznamů");
                for (String uuid : uuidList) {
                    System.out.println("http://kramerius.mzk.cz/search/handle/" + uuid);
                }
            } else {
                System.out.println("http://kramerius.mzk.cz/search/handle/" + uuidList.get(0) + ", " + signatura);
            }
        }
    }

    private List<String> getUuidFromSignatura(String signatura) throws FileNotFoundException {
        // dc.identifier:"signature:PK-0047.479"&fl=PID
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("dc.identifier",signatura);
        try {
            List<String> result = k5Api.solrSimpleSearch(queryMap);
            if (!result.isEmpty()) {
                return result;
            } else if (signatura.contains("signature:")) {
                throw new FileNotFoundException("Signatura " + signatura + " nenalezena");
            } else {
                return getUuidFromSignatura("\"signature:" + signatura + "\"");
            }
        } catch (K5ApiException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    @Override
    public String getUsage() {
        return null;
    }
}
