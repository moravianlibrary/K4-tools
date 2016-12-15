package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.exceptions.K4ToolsException;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.workers.AddOcrWorker;
import cz.mzk.k4.tools.workers.UuidWorker;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 11/26/13
 */
public class AddOcr implements Script {
    private static final Logger LOGGER = Logger.getLogger(AddOcr.class);
    private FedoraUtils fedoraUtils;
    private UuidWorker addOcr;

    public AddOcr() throws FileNotFoundException {
        fedoraUtils = new FedoraUtils(new AccessProvider());
        addOcr = new AddOcrWorker(true);
    }

    @Override
    public void run(List<String> args) {
        String listFileName = args.get(0);
        List<String> rootPids = GeneralUtils.loadUuidsFromFile(listFileName);
        rootPids.forEach(this::processRoot);
    }

    private void processRoot(String rootPid) {
        int existingOcrCounter = 0;
        List<String> pagePids;
        List<String> kopie;
        String serializedFile = "IO/" + rootPid + ".ser";

        // load pages (from serialized file or from fedora)
        if (new File(serializedFile).exists()) {
            try {
                pagePids = GeneralUtils.deserialize(serializedFile);
            } catch (FileNotFoundException e) {
                LOGGER.error("Could not deserialize file " + serializedFile);
                return;
            }
        } else {
            pagePids = fedoraUtils.getChildrenUuids(rootPid, DigitalObjectModel.PAGE);
            pagePids = GeneralUtils.makeUnique(pagePids);
            GeneralUtils.serialize(pagePids, serializedFile);
        }
        kopie = new ArrayList(pagePids); // pro odebírání během cyklu (odebírá se až při 2. běhu)
        LOGGER.info("Loaded " + pagePids.size() + " pages of " + rootPid);

        // ocr loop
        for (int i = 0; i < pagePids.size(); i++) {
            String pagePid = pagePids.get(i);
            try {
                addOcr.run(pagePid);
            } catch (K4ToolsException ocrExistsExeption) { // strana už má OCR
                LOGGER.debug(ocrExistsExeption.getMessage());
                existingOcrCounter++;
                // odstranit z kopie seznamu
                kopie.remove(pagePid);
                // serializace kopie
                GeneralUtils.serialize(kopie, "IO/" + rootPid + ".ser");
            }
            // průběžné info
            if (i % 100 == 0) {
                LOGGER.info(i + " out of " + pagePids.size() + " pages processed");
            }
        }
        LOGGER.info("Finished OCR of " + rootPid);
        LOGGER.info(existingOcrCounter + " pages skipped (OCR already exist)");
    }

    @Override
    public String getUsage() {
        return "Parametr: cesta k souboru se seznamem kořenových uuid\n" +
                "Skript rekurzivně načte všechny strany a postupně je posílá na abbyy recognition server MZK (ocr-abbyy.mzk.cz)";
    }
}
