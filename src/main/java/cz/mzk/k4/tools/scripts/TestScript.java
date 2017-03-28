package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.fedoraApi.FedoraFactoryService;
import cz.mzk.k4.tools.fedoraApi.RisearchService;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 12/5/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestScript implements Script {

    private static final Logger LOGGER = LogManager.getLogger(TestScript.class);
    private FedoraUtils fedoraUtils = new FedoraUtils(AccessProvider.getInstance());
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    RisearchService risearch = FedoraFactoryService.getService(accessProvider.getFedoraHost());


    public TestScript() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        String topUuid = "uuid:0bc12264-8b96-4ecb-bb20-8880f770fa1a";
        try {
            List<Item> volumes = clientApi.getChildren(topUuid);
            for (Item volume : volumes) {
                String year = volume.getDetails().getYear();
                if (year.equals("1969")) {
                    remoteApi.deleteObject(volume.getPid());
                }
            }
        } catch (K5ApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getUsage() {
        return null;
    }

}
