package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by hradskam on 1.7.14.
 */
public class ExportFromFile implements Script {
    private static final Logger LOGGER = Logger.getLogger(ExportFromFile.class);
    private static KrameriusUtils krameriusUtils = new KrameriusUtils(AccessProvider.getInstance());

    @Override
    public void run(List<String> args) {
        if(args.size() != 1) {
            System.out.println("Součástí příkazu musí být i název souboru. (A nic dalšího)");
            return;
        }

        String filePath = args.get(0);
        List<String> uuidList = GeneralUtils.loadUuidsFromFile(filePath);
        for (String uuid : uuidList) {
            krameriusUtils.export(uuid);
            LOGGER.debug("Export planned for object " + uuid);
        }
    }

    @Override
    public String getUsage() {
        return "Export všech dokumentů ze zadaného souboru.";
    }
}
