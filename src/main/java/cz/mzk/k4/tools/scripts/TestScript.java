package cz.mzk.k4.tools.scripts;

import com.google.gson.JsonObject;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.client.domain.Streams;
import cz.mzk.k5.api.common.InternalServerErroException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import cz.mzk.k5.api.remote.domain.ProcessLog;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());

    @Override
    public void run(List<String> args) {

        // remote
//        import?
//        reindex - různý parametry
//        reindexNewBranches
//        stopProcess
//        deleteProcess
//        getReplicatedObjectInfo
//        getReplicatedObjectTree
//        listProcesses - počet, offset, filtrování
//        replikace - spuštění
//        třídění
//         addToCollection
//        ještě něco jde?
        // serializace domain věcí (toString)

        // client
        // item včetně title
        // všechny datastreamy
        // children
        // siblings
        // jiná třída na listed item
        // /streams
        // /streams/stream_id
        // foxml

        Document mods = null;
        try {
            InputStream input = clientApi.getImgFull("uuid:afdd8ea1-ad6f-474c-9611-152cfd3a14b3");
            FileUtils.copyInputStreamToFile(input, new File("full.jpg"));

//            input = clientApi.getImgPreview("uuid:afdd8ea1-ad6f-474c-9611-152cfd3a14b3");
//            FileUtils.copyInputStreamToFile(input, new File("preview.jpg"));

            input = clientApi.getImgThumb("uuid:afdd8ea1-ad6f-474c-9611-152cfd3a14b3");
            FileUtils.copyInputStreamToFile(input, new File("thumb.jpg"));

            input = clientApi.getRecordingMp3("uuid:9dc2a9bc-24d0-4614-aa89-85824aa20720");
            FileUtils.copyInputStreamToFile(input, new File("sound.mp3"));

            input = clientApi.getRecordingOgg("uuid:9dc2a9bc-24d0-4614-aa89-85824aa20720");
            FileUtils.copyInputStreamToFile(input, new File("sound.ogg"));

            input = clientApi.getRecordingWav("uuid:9dc2a9bc-24d0-4614-aa89-85824aa20720");
            FileUtils.copyInputStreamToFile(input, new File("sound.wav"));

        } catch (InternalServerErroException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }


//

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
