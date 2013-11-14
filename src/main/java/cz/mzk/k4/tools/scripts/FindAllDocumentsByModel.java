package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedoraUtils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;

import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 10/3/13
 */
public class FindAllDocumentsByModel implements Script {

    private static FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());

    public static void run(DigitalObjectModel model) {
        fedoraUtils.applyToAllUuidOfModel(model, new UuidWorker(false) {
            @Override
            public void run(String uuid) {
                System.out.println(uuid);
            }
        });


    }

    @Override
    public void run(List<String> args) {
        //run(args.get(0));
    }

    @Override
    public String getUsage() {
        return null;
    }
}
