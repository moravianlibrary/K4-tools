package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by hradskam on 29.7.14.
 */
public class ImportCollection implements Script {
    private static final Logger LOGGER = Logger.getLogger(IndexList.class);

    @Override
    public void run(List<String> args) {

        if(args.size() < 2 || !args.get(0).contains("uuid:") || !args.get(1).contains("vc:")) {
            System.out.println("Musí být zadáno první uuid dokumentu a poté uuid sbírky.");
            return;
        }

        AccessProvider accessProvider = AccessProvider.getInstance();
        KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);

        LOGGER.info("Začátek přidávání do sbírky.");

        String pid = args.get(0).substring(("uuid:").length());
        String collectionPid = args.get(1).substring(("vc:").length());

        krameriusUtils.addToCollection(pid, collectionPid);

        LOGGER.info("Konec přidávání do sbírky.");

    }

    @Override
    public String getUsage() {
        return "";
    }
}
