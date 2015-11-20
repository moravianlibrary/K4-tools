package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import cz.mzk.k5.api.remote.domain.Process;
import cz.mzk.k5.api.remote.domain.ReplicatedObject;
import cz.mzk.k5.api.remote.domain.ReplicationTree;
import org.apache.log4j.Logger;

import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());

    @Override
    public void run(List<String> args) {

        try {
            // asi nefunguje v K5 API
            Map<String, String> params = new HashMap<>();
            params.put("state", "KILLED");
            List<Process> processes = remoteApi.filterProcesses(params);
            processes.forEach(System.out::println);
        } catch (K5ApiException e) {
            e.printStackTrace();
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
