package cz.mzk.k4.tools.workers.readOnly;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.workers.UuidWorker;
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
                counter++;
                System.out.println(uuid);
                if ((counter % 100) == 1) {
                    System.out.println("Prohledáno " + counter + " monografií");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

