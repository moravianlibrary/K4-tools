package cz.mzk.k4.tools.utils;

import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 11.3.15.
 */
public class GeneralUtils {
    private static final Logger LOGGER = Logger.getLogger(KrameriusUtils.class);

    public static List<String> loadUuidsFromFile(String filePath) {
        List<String> uuidList = new ArrayList<>();

        LOGGER.debug("Otvírání souboru " + filePath);
        File inputFile = new File(filePath);
        BufferedReader reader = null;

        try {
            //Open file and load content
            reader = new BufferedReader(new FileReader(inputFile));
            String uuid = null;

            //Parse file by line
            while ((uuid = reader.readLine()) != null) {
                uuid = uuid.substring(uuid.indexOf("uuid:"));
                uuidList.add(uuid);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Chyba při otvírání souboru: " + filePath + ".");
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("Chyba při čtení souboru: ");
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("Chyba při zavírání souboru: " + e.getStackTrace());
                }
            }
        }

        LOGGER.info("Loaded " + uuidList.size() + " uuids from file " + filePath);
        return uuidList;
    }

}
