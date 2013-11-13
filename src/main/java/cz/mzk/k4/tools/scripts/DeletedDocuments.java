package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.workers.UuidWorker;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 11/11/13
 */
public class DeletedDocuments implements Script {

    private static FedoraUtils fedoraUtils = new FedoraUtils();

    @Override
    /**
     * Vypisuje objekty ve Fedore oznacene jako deleted
     * @param writeEnabled - pokud je false, pouze vypíše uuid dokumentů se stavem DELETED
     *                     - pokud je true, DELETED dokumenty trvale odstraní
     */
    public void run(List<String> args) {
        fedoraUtils.applyToAllUuidOfStateDeleted(new UuidWorker(args.contains("writeEnabled")) {
            @Override
            public void run(String uuid) {
                if (isWriteEnabled()) {
                    fedoraUtils.purgeObject(uuid);
                    System.out.println("Deleted: " + uuid);
                } else {
                    System.out.println(uuid);
                }
            }
        });
    }

    @Override
    public String getUsage() {
        return "Vypisuje (a pripadne maze) objekty ve Fedore oznacene jako deleted";
    }
}
