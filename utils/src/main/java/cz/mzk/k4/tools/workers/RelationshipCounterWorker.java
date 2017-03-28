package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA.
 * User: Jan Holman
 * Date: 11/7/13
 * Time: 10:13 AM
 */
public class RelationshipCounterWorker extends UuidWorker {

    private static final Logger LOGGER = LogManager.getLogger(RelationshipCounterWorker.class);
    private FedoraUtils fedoraUtils;
    private int counter;

    public RelationshipCounterWorker(boolean writeEnabled) throws FileNotFoundException {
        super(writeEnabled);
        fedoraUtils = new FedoraUtils(new AccessProvider());
        counter = 0;
    }

    /**
     * git add
     * Prohledá monografie a vypíše ty, u kterých ve fedoře (ritriplets) chybí vazby
     *
     * @param uuid
     * @return list of uuids w/o pages
     */
    @Override
    public void run(String uuid) {

        String result = fedoraUtils.getAllRelationships(uuid);

        if (result.equals("")) {
            LOGGER.error("Objekt bez vazeb: " + uuid);
        }
        counter++;
        if ((counter % 100) == 0 && counter > 1) {
            LOGGER.debug("Prohledáno " + counter + " objektů");
        }
    }
}

