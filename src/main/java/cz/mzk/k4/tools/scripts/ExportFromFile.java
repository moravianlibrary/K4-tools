package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by hradskam on 1.7.14.
 */
public class ExportFromFile implements Script {
    private static final Logger LOGGER = LogManager.getLogger(ExportFromFile.class);
    private AccessProvider accessProvider = AccessProvider.getInstance();
//    private static KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);
    private ProcessRemoteApi krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());

    public ExportFromFile() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        if(args.size() != 1) {
            System.out.println("Součástí příkazu musí být i název souboru. (A nic dalšího)");
            return;
        }

        String filePath = args.get(0);
        List<String> uuidList = GeneralUtils.loadUuidsFromFile(filePath);
        for (String uuid : uuidList) {
            try {
                krameriusApi.export(uuid);
            } catch (K5ApiException e) {
                LOGGER.error(e.getMessage());
                LOGGER.error("Selhalo plánováho procesu export u objektu " + uuid);
            }
            LOGGER.debug("Export planned for object " + uuid);
        }
    }

    @Override
    public String getUsage() {
        return "Export všech dokumentů ze zadaného souboru.";
    }
}
