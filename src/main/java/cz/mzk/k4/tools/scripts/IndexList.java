package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by hradskam on 23.7.14.
 */
public class IndexList implements Script {
    private static final Logger LOGGER = Logger.getLogger(IndexList.class);

    @Override
    public void run(List<String> args) {

        if(args.isEmpty()) {
            System.out.println("Chybí zadat pid dokumentů");
            return;
        }
        String[] allPids = args.get(0).split(";");

        AccessProvider accessProvider = AccessProvider.getInstance();
        KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);

        LOGGER.info("Začátek reindexace.");

        for (String pid : allPids){
            krameriusUtils.reindex(pid);
        }

        LOGGER.info("Konec reindexace.");

    }

    @Override
    public String getUsage() {
        return "indexaceZeSeznamu\n" +
                "Provede reindexaci pro seznam uuid oddělených pomocí \\';\\'\n";
    }
}
