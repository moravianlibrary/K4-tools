package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hradskam on 29.7.14.
 */
public class ImportCollection implements Script {
    private static final Logger LOGGER = Logger.getLogger(IndexList.class);

    @Override
    public void run(List<String> args) {

        if(args.size() < 2 || !args.get(0).contains(".txt") || !args.get(1).contains("vc:")) {
            System.out.println("Musí být zadáno první uuid dokumentu a poté uuid sbírky.");
            return;
        }

        AccessProvider accessProvider = AccessProvider.getInstance();
        KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(args.get(0))));
            List<String> allPids = new ArrayList<>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                allPids.add(line);
            }

            String collectionPid = args.get(1).substring(("vc:").length());

            LOGGER.info("Začátek přidávání do sbírky.");

            String uuid = null;
            for (String pid : allPids){
                uuid = pid.substring(pid.indexOf("uuid:") + ("uuid:").length());
                krameriusUtils.addToCollection(uuid, collectionPid);
            }

            LOGGER.info("Konec přidávání do sbírky.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public String getUsage() {
        return "";
    }
}
