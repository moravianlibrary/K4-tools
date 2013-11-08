package cz.mzk.k4.tools.scripts;

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

    private static FedoraUtils fu = new FedoraUtils();

    public static void run(DigitalObjectModel model) {
        fu.applyToAllUuidOfModel(model, new UuidWorker() {
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
