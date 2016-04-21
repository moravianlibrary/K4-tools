package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.workers.AddOcrWorker;
import cz.mzk.k4.tools.workers.UuidWorker;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 11/26/13
 */
public class AddOcr implements Script {
    private static final Logger LOGGER = Logger.getLogger(AddOcr.class);


    @Override
    public void run(List<String> args) throws FileNotFoundException {
        FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());
        UuidWorker addOcr = new AddOcrWorker(true);
        String uuid = args.get(0);

        List<String> list;
        list = fedoraUtils.getChildrenUuids(uuid, DigitalObjectModel.PAGE);

        for (String uuidChild : list) {
            addOcr.run(uuidChild);
        }
    }

    @Override
    public String getUsage() {
        return null;
    }
}
