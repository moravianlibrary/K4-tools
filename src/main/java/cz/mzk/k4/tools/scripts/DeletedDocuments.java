package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.workers.UuidWorker;

import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 11/11/13
 */
public class DeletedDocuments implements Script {

    private static FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());
    public static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(DeletedDocuments.class);

    @Override
    /**
     * Vypisuje objekty ve Fedore oznacene jako DELETED
     * @param writeEnabled - pokud je false, pouze vypíše uuid dokumentů se stavem DELETED
     *                     - pokud je true, DELETED dokumenty trvale odstraní
     */
    public void run(List<String> args) {
        // TODO: chyba při nulovém výsledku v risearch
        fedoraUtils.applyToAllUuidOfStateDeleted(new UuidWorker(args.contains("writeEnabled")) {
            @Override
            public void run(String uuid) {
                if (isWriteEnabled()) {
                    try {
                        fedoraUtils.purgeObject(uuid);
                        LOGGER.info("Deleted: " + uuid + " from fedora NDK");
                    } catch (RuntimeException e) {
                        LOGGER.error("NDK: " + e.getMessage());
                    }
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
