package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.api.ClientRemoteApi;
import cz.mzk.k4.tools.api.KrameriusClientRemoteApiFactory;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 12/5/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestScript implements Script {

    private static final Logger LOGGER = Logger.getLogger(TestScript.class);
    private static FedoraUtils fedoraUtils = new FedoraUtils(AccessProvider.getInstance());
    private static KrameriusUtils krameriusUtils = new KrameriusUtils(AccessProvider.getInstance());
    AccessProvider accessProvider = new AccessProvider();
    ClientRemoteApi krameriusApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());

    @Override
    public void run(List<String> args) {
        int counter = 0;
        List<String> uuids = GeneralUtils.loadUuidsFromFile("LN-strany");
        for (String uuid : uuids) {
            List<String> pageUuids = fedoraUtils.getChildrenUuids(uuid, DigitalObjectModel.PAGE);
            counter += pageUuids.size();
            LOGGER.debug("Ročník: " + uuid + " má " + pageUuids.size() + " stran.");
        }
        LOGGER.info("Zbytek LN: " + counter + " stran.");
    }

    @Override
    public String getUsage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
