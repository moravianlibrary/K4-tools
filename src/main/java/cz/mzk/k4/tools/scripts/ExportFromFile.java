package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;

/**
 * Created by hradskam on 1.7.14.
 */
public class ExportFromFile implements Script {
    private static final Logger LOGGER = Logger.getLogger(ExportFromFile.class);

    @Override
    public void run(List<String> args) {
        if(args.size() != 1) {
            System.out.println("Součástí příkazu musí být i název souboru. (A nic dalšího)");
            return;
        }

        LOGGER.info("Otvírání souboru.");
        File inputFile = new File(args.get(0));
        BufferedReader reader = null;

        AccessProvider accessProvider = AccessProvider.getInstance();
        KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);

        try {
            //Open file and load content
            reader = new BufferedReader(new FileReader(inputFile));
            String uuid = null;

            LOGGER.info("Export ze souboru zahájen.");
            //Parse file by line
            while ((uuid = reader.readLine()) != null) {
                uuid = parseUuid(uuid);
                //Export each file with Kramerius API
                krameriusUtils.export(uuid);
            }
            LOGGER.info("Export ze souboru ukončen.");

        } catch (FileNotFoundException e) {
            System.out.println("Špatně zadána cesta k souboru.");
            LOGGER.error("Chyba při otvírání souboru: " + args.get(0) + ". ");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Chyba při čtení souboru.");
            LOGGER.error("Chyba při čtení souboru: ");
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("Chyba při zavírání souboru: " + e.getStackTrace());
                }
            }
        }
    }

    @Override
    public String getUsage() {
        return "Export všech dokumentů ze zadaného souboru.";
    }

    private String parseUuid(String uuid) {
        return uuid.substring(uuid.indexOf("uuid:"));
    }
}
