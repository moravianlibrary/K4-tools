package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.FedoraUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 11/19/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class SetPolicyWorker extends UuidWorker {

    private FedoraUtils fedoraUtils;

    public SetPolicyWorker(boolean writeEnabled, AccessProvider accessProvider) {
        super(writeEnabled);
        fedoraUtils = new FedoraUtils(accessProvider); // jen access provider a REST?
    }

    @Override
    public void run(String uuid) {

        List<String> listOfChildren = null;
        try {
            listOfChildren = fedoraUtils.getChildrenUuids(uuid);
            // String rok = fedoraUtils.getMetadata().getYear();
            // if (rok > ?? ) { setPublic } else { setPrivate }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
