package cz.mzk.k4.tools.scripts.standalone;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * This script is used to replicate batches of uuids stored in .txt file from one Kramerius to another.
 * Setup lp.xml for replication process without any templates.
 *
 * <processes>
 *     <process>
 *         <id>k4_replication</id>
 *         <description>K4 Replication process</description>
 *         <mainClass>org.kramerius.replications.K4ReplicationProcess</mainClass>
 *         <standardOs>lrOut</standardOs>
 *         <errOs>lrErr</errOs>
 *         <javaProcessParameters>-Xmx1024m -Xms512m</javaProcessParameters>
 *         <securedaction>import_k4_replications</securedaction>
 *     </process>
 * </processes>
 *
 * @author Aleksei Ermak
 */
public class Replicate {

    public static final Logger logger = LogManager.getLogger(Replicate.class);

    private static String handlePrefix = "http://krameriusSrcHost/search/handle/";
    private static String filename = "IO/replicate.txt";

    private static String srcKrameriusUser = "krameriusUser";
    private static String srcKrameriusPswd = "krameriusPswd";

    public static void main(String[] args) throws FileNotFoundException {

        AccessProvider accessProvider = AccessProvider.getInstance();
        ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
                accessProvider.getKrameriusHost(),
                accessProvider.getKrameriusUser(),
                accessProvider.getKrameriusPassword()
        );

        List<String> uuids = GeneralUtils.loadUuidsFromFile(filename);

        for (String uuid : uuids) {
            try {
                logger.info("processing " + uuid + "...");
                String handleUrl = handlePrefix + uuid;
                remoteApi.replicate(handleUrl, srcKrameriusUser, srcKrameriusPswd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
