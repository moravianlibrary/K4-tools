package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.FedoraUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jan Holman
 * Date: 11/7/13
 * Time: 10:13 AM
 */
public class ChildCounterWorker extends UuidWorker {

    private static FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());
    private int counter;

    public ChildCounterWorker(boolean writeEnabled) {
        super(writeEnabled);
        counter = 0;
    }

    /**
     * Prohledá monografie a vypíše ty, u kterých ve fedoře (ritriplets) chybí vazby
     * @param uuid
     * @return list of uuids w/o pages
     */
    @Override
    public void run(String uuid) {

        List<String> listOfChildren;
        try {
            // všechny děti (rekurzivně se zanořuje)
            listOfChildren = fedoraUtils.getChildrenUuids(uuid);
            if (listOfChildren.isEmpty()) {
                System.out.println(uuid);
            }
            counter++;
            if ((counter % 100) == 0 && counter > 1) {
                System.out.println("Prohledáno " + counter + " monografií");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

