package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
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
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());

    @Override
    public void run(List<String> args) {
//        String filename = "child-count";
//        List<String> uuids = GeneralUtils.loadUuidsFromFile(filename);
//        delete();
//        fedoraUtils.getAllChildren("uuid:4eac74b0-e92c-11dc-9fa1-000d606f5dc7");
        List<String> parents = fedoraUtils.getParentUuids("uuid:8f3de2b0-e92d-11dc-a565-000d606f5dc6");
        parents.forEach(System.out::println);
//        System.out.println();
//        fedoraUtils.purgeObject("uuid:4eac74b0-e92c-11dc-9fa1-000d606f5dc6");
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        parents = fedoraUtils.getParentUuids("uuid:8f3de2b0-e92d-11dc-a565-000d606f5dc6");
//        parents.forEach(System.out::println);

//        for (String uuid : uuids) {
//            try {
////                Item item = clientApi.getItem(uuid);
////                item.getContext().
//                if (clientApi.getItem(uuid) != null) {
//                    ArrayList<ArrayList<String>> children = fedoraUtils.getAllChildren(uuid);
//                    int totalCount = 0;
//                    for (ArrayList<String> child : children) {
//                        if (child != null) {
//                            totalCount += child.size();
//                        }
//                    }
//                    System.out.println(uuid + " " + totalCount);
//                }
//            } catch (K5ApiException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void delete() {
        List<String> uuids = GeneralUtils.loadUuidsFromFile("to-remove");
        for (String uuid : uuids) {
            try {
                remoteApi.deleteObject(uuid);
            } catch (K5ApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getUsage() {
        return null;
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
