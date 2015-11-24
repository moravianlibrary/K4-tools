package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by hradskam on 23.7.14.
 */
public class IndexList implements Script {
    private static final Logger LOGGER = Logger.getLogger(IndexList.class);

    @Override
    public void run(List<String> args) {

        if(args.isEmpty()) {
            System.out.println("Chybí zadat pid dokumentů");
            return;
        }
        String[] allPids = args.get(0).split(";");

        AccessProvider accessProvider = AccessProvider.getInstance();
        ProcessRemoteApi krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
                accessProvider.getKrameriusHost(),
                accessProvider.getKrameriusUser(),
                accessProvider.getKrameriusPassword());

        LOGGER.info("Začátek reindexace.");

        for (String pid : allPids){
            try {
                krameriusApi.reindex(pid);
            } catch (K5ApiException e) {
                LOGGER.error("Selhalo plánování reindexace dokumentu " + pid);
                LOGGER.error(e.getMessage());
            }
        }

        LOGGER.info("Konec reindexace.");

    }

    @Override
    public String getUsage() {
        return "indexaceZeSeznamu\n" +
                "Provede reindexaci pro seznam uuid oddělených pomocí \\';\\'\n" +
                "Zastaralé - požívat uuid na řádek, načítání přes GeneraUtils";
    }
}
