package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.utils.fedoraUtils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedoraUtils.exception.CreateObjectException;
import cz.mzk.k4.tools.workers.AddOcrWorker;
import cz.mzk.k4.tools.workers.UuidWorker;

import java.io.IOException;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 11/26/13
 */
public class AddOcr implements Script {
    private static FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());

    @Override
    public void run(List<String> args) {
        UuidWorker addOcr = new AddOcrWorker(true);
        String uuid = args.get(0);
        List<String> list;
        try {
            list = fedoraUtils.getChildrenUuids(uuid, DigitalObjectModel.PAGE);

            for (String uuidChild : list) {
                addOcr.run(uuidChild);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getUsage() {
        return null;
    }
}
