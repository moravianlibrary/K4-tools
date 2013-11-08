package cz.mzk.k4.tools.workers.readOnly;

import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.utils.fedoraUtils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.workers.UuidWorker;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jan Holman
 * Date: 11/7/13
 * Time: 10:13 AM
 */
public class HasPagesWorker implements UuidWorker {

    private static FedoraUtils fedoraUtils = new FedoraUtils();

    /**
     * @param uuid
     * @return list of uuids w/o pages
     */
    @Override
    public void run(String uuid) {

        List<String> listOfPages;
        try {
            // všechny děti typu Page (rekurzivně se zanořuje)
            listOfPages = fedoraUtils.getChildren(uuid, DigitalObjectModel.PAGE);
            if (listOfPages.isEmpty()) {
                System.out.println(uuid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

