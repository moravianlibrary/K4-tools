package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k4.tools.workers.RelationshipCounterWorker;
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
    private FedoraUtils fedoraUtils;
    private static UuidWorker worker = new RelationshipCounterWorker(false);
    private static final Logger LOGGER = Logger.getLogger(FindLonelyMonographs.class);

    /**
     * Spustí RelationshipCounterWorker nad všemi monografiemi
     * @param args - nebere argument
     */
    @Override
    public void run(List<String> args) {
        accessProvider = new AccessProvider();
        fedoraUtils = new FedoraUtils(accessProvider);
        LOGGER.info("Running " + this.getClass() + " on " + accessProvider.getLibraryPrefix());
        fedoraUtils.applyToAllUuidOfModel(DigitalObjectModel.MONOGRAPH, worker, 5);
    }

    @Override
    public String getUsage() {
        return "vypisSmutneMonografie - Vypíše monografie s rozbitými vazbami v ritriplets";
    }
}
