package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k4.tools.workers.XMLStarletWorker;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by rumanekm on 3/10/15.
 */
public class XMLStarlet implements Script {

    private AccessProvider accessProvider = new AccessProvider();
    private FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);

    public XMLStarlet() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        if (args.size() > 0) {
            if (args.get(args.size()-1).startsWith("model:")) {
                DigitalObjectModel model = DigitalObjectModel.parseString(args.get(args.size()-1).substring(6));
                UuidWorker xmlStarletWorkerWorker = new XMLStarletWorker(fedoraUtils, args.subList(0, args.size()-1));
                fedoraUtils.applyToAllUuidOfModel(model, xmlStarletWorkerWorker);
            } else if (args.get(args.size()-1).startsWith("uuid:")) {
                XMLStarletWorker xmlStarletWorker = new XMLStarletWorker(fedoraUtils, args.subList(0, args.size()-1));
                xmlStarletWorker.run(args.get(args.size()-1));
            }


        }
    }


    @Override
    public String getUsage() {
        return null;
    }
}
