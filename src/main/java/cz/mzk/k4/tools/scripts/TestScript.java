package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.api.ClientRemoteApi;
import cz.mzk.k4.tools.api.KrameriusClientRemoteApiFactory;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.log4j.Logger;

import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 12/5/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestScript implements Script {

    private static final Logger LOGGER = Logger.getLogger(TestScript.class);
    private static FedoraUtils fedoraUtils = new FedoraUtils(AccessProvider.getInstance());
    private static KrameriusUtils krameriusUtils = new KrameriusUtils(AccessProvider.getInstance());
    AccessProvider accessProvider = new AccessProvider();
    ClientRemoteApi krameriusApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());

    @Override
    public void run(List<String> args) {
//        int counter = 0;
//        repair("Notizen-Blatt");

//        for (String issue : map.keySet()) {
//            try {
//                fedoraUtils.addChild(map.get(issue), issue);
//            } catch (CreateObjectException e) {
//                e.printStackTrace();
//            } catch (TransformerException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            LOGGER.info("Číslo " + issue + " bylo zařazeno do ročníku " + map.get(issue) + ".");
//        }
//        LOGGER.info("Zbytek LN: " + counter + " stran.");



//        // muzeum - obrázky
//        int counter = 1;
//        List<String> topUuids = GeneralUtils.loadUuidsFromFile("muzeum");
//        for (String topUuid : topUuids) {
//            List<String> pageUuids = fedoraUtils.getChildrenUuids(topUuid, DigitalObjectModel.PAGE);
//            for (String pageUuid : pageUuids) {
//                try {
//                    fedoraUtils.repairImageserverTilesRelation(pageUuid);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (CreateObjectException e) {
//                    e.printStackTrace();
//                } catch (TransformerException e) {
//                    e.printStackTrace();
//                }
//            }
//            LOGGER.info("Repaired " + topUuid);
//            LOGGER.info(counter + " out of " + topUuids.size());
//            counter++;
//        }
        List<String> uuids = GeneralUtils.loadUuidsFromFile("null-indexace");
        for (String uuid : uuids) {
            krameriusUtils.reindex(uuid);
            LOGGER.info(uuid + " zařazeno k indexaci");
        }

    }

    @Override
    public String getUsage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void repair(String filePath) {

        LOGGER.debug("Otvírání souboru " + filePath);
        File inputFile = new File(filePath);
        BufferedReader reader = null;
        int counter = 0;

        try {
            //Open file and load content
            reader = new BufferedReader(new FileReader(inputFile));
            String volume;
            String issue;
            String empty;

            //Parse file by line
            while ((issue = reader.readLine()) != null) {
                issue = "uuid:" + issue;
                volume = "uuid:" + reader.readLine();
                empty = reader.readLine();
                if (empty != null && !"".equals(empty)) {
                    System.out.println("Chyba");
                }
                try {
                    fedoraUtils.addChild(volume, issue);
                } catch (CreateObjectException e) {
                    e.printStackTrace();
                } catch (TransformerException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LOGGER.info("Číslo " + issue + " bylo zařazeno do ročníku " + volume + ".");
                counter++;
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
        LOGGER.info("Zpracováno " + counter + " čísel.");
    }
}
