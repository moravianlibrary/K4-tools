package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k4.tools.utils.FedoraUtils;

import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 10/3/13
 */
public class FindAllDocumentsByModel implements Script {

    private static FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());

    /**
     *
     * @param model
     */
    public static void run(DigitalObjectModel model) {
        fedoraUtils.applyToAllUuidOfModel(model, new UuidWorker(false) {
            @Override
            public void run(String uuid) {
                System.out.println(uuid);
            }
        });


    }

    //TODO: run FindAllDocumentsByModel
    @Override
    public void run(List<String> args) {
        //run(args.get(0));
    }

    // TODO: getUsage FindAllDocumentsByModel
    @Override
    public String getUsage() {
        return null;
    }
}
