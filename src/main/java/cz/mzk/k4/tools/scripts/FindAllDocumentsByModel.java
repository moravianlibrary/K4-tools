package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;

/**
 * @author: Martin Rumanek
 * @version: 10/3/13
 */
public class FindAllDocumentsByModel {

    private static FedoraUtils fu = new FedoraUtils();

    public static void run(String model) {
        fu.applyToAllUuidOfModel(model, new UuidWorker() {
            @Override
            public void run(String uuid) {
                System.out.println(uuid);
            }
        });


    }
}
