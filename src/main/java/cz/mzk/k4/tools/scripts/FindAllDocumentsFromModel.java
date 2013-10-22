package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.UuidWorker;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import org.fedora.api.RelationshipTuple;

import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 10/3/13
 */
public class FindAllDocumentsFromModel {

    private static FedoraUtils fu = new FedoraUtils();

    public static void run(String model) {
        fu.applyToAllUuidFromModel(model, new UuidWorker() {
            @Override
            public void run(String uuid) {
                System.out.println(uuid);
            }
        });


    }
}
