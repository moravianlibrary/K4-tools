package cz.mzk.k4.tools.workers;

import com.sun.jersey.api.client.WebResource;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Jan Holman
 * Date: 11/7/13
 * Time: 10:13 AM
 */
public class RelationshipCounterWorker extends UuidWorker {

    private static FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());
    private int counter;

    public RelationshipCounterWorker(boolean writeEnabled) {
        super(writeEnabled);
        counter = 0;
    }

    /**                                                                       git add
     * Prohledá monografie a vypíše ty, u kterých ve fedoře (ritriplets) chybí vazby
     *
     * @param uuid
     * @return list of uuids w/o pages
     */
    @Override
    public void run(String uuid) {

        AccessProvider accessProvider = new AccessProvider();
        String query = "%3Cinfo:fedora/" + uuid + "%3E%20*%20*";
        WebResource resource = accessProvider.getFedoraWebResource("/risearch?type=triples&lang=spo&format=N-Triples&query="
                + query);
        String result = resource.get(String.class);

        if (result.equals("")) {
            System.out.println(uuid);
        }
        counter++;
        if ((counter % 100) == 0 && counter > 1) {
            System.out.println("Prohledáno " + counter + " monografií");
        }
    }
}

