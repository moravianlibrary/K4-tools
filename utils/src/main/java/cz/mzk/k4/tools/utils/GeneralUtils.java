package cz.mzk.k4.tools.utils;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 11.3.15.
 */
public class GeneralUtils {
    private static final Logger LOGGER = Logger.getLogger(GeneralUtils.class);

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
                if (!uuid.contains("uuid")) {
                    LOGGER.warn("Not a valid pid: " + uuid);
                } else {
                    uuid = uuid.substring(uuid.indexOf("uuid:"));
                    uuidList.add(uuid);
                }
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

    public static String toString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

}
