package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.workers.RelationshipCounterWorker;
import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jan Holman
 * Date: 11/8/13
 * Time: 10:08 AM
 */
public class FindLonelyMonographs implements Script {

    private AccessProvider accessProvider;
    private KrameriusUtils krameriusUtils;
    private static UuidWorker worker = new RelationshipCounterWorker(false);
    private static final Logger LOGGER = Logger.getLogger(FindLonelyMonographs.class);

    /**
     * Spustí RelationshipCounterWorker nad všemi monografiemi
     * @param args - nebere argument
     */
    @Override
    public void run(List<String> args) {
        accessProvider = new AccessProvider();
        krameriusUtils = new KrameriusUtils(accessProvider);

        String model = args.get(0);
        List<String> uuidList = krameriusUtils.getUuidsByModelSolr(model); // argument

        LOGGER.info("Running " + this.getClass() + " on " + accessProvider.getLibraryPrefix());
        for (String uuid : uuidList) {
            worker.run(uuid);
        }

       // writeEnabled - mazat?
    }

    @Override
    public String getUsage() {
        return "vypisSmutneMonografie\n" +
                "Vypíše monografie s rozbitými vazbami v ritriplets\n" +
                "Argument: prohledávaný model (monograph, periodical,..)";
    }
}
