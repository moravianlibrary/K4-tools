package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 12.7.16.
 */
public class Sorter implements Script {

    private static final Logger LOGGER = LogManager.getLogger(Sorter.class);
    private FedoraUtils fedoraUtils = new FedoraUtils(AccessProvider.getInstance());
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());

    public Sorter() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        List<String> uuids = GeneralUtils.loadUuidsFromFile("IO/solr-result");
        List<String> copy = new ArrayList<>(uuids);
        Path working = Paths.get("IO/sort/working");
        Path mzk = Paths.get("IO/sort/mzk-BOA001");
        Path ndk = Paths.get("IO/sort/ndk");
        Path other = Paths.get("IO/sort/other");
        Path pdf = Paths.get("IO/sort/pdf");
        Path childless = Paths.get("IO/sort/childless");
        Path unsure = Paths.get("IO/sort/mzk-jine-sigly");

        for (String uuid : uuids) {
            try {
//                String sigla = fedoraUtils.getModsLocationElement(uuid);
//                String entry = uuid + "\n";
//                if (sigla.equals("BOA001")) {
//                    Files.write(mzk, entry.getBytes(), StandardOpenOption.APPEND);
//                } else {
//                    entry = uuid + " - " + sigla + "\n";
//                    Files.write(unsure, entry.getBytes(), StandardOpenOption.APPEND);
//                }



                // sort uuid
                String origin = fedoraUtils.getOrigin(uuid);
                String entry = uuid + "\n";
                if (origin == null) {
                    Files.write(childless, entry.getBytes(), StandardOpenOption.APPEND);
                } else if (origin.equals(fedoraUtils.MZK)) {
                    Files.write(mzk, entry.getBytes(), StandardOpenOption.APPEND);
                } else if (origin.equals(fedoraUtils.NDK)) {
                    Files.write(ndk, entry.getBytes(), StandardOpenOption.APPEND);
                } else if (origin.equals(fedoraUtils.OTHER)) {
                    Files.write(other, entry.getBytes(), StandardOpenOption.APPEND);
                } else if (origin.equals(fedoraUtils.UNKNOWN)) {
                    Files.write(pdf, entry.getBytes(), StandardOpenOption.APPEND);
                }
                // save progress
                copy.remove(uuid);
                Files.write(working, copy, Charset.forName("UTF-8"));
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                LOGGER.error("Could no get document origin: " + uuid);
            } catch (IllegalArgumentException ex) {
                LOGGER.warn(ex);
            }
        }
    }

//    @Override
//    public void run(List<String> args) throws FileNotFoundException {
//        List<String> uuids = GeneralUtils.loadUuidsFromFile("IO/working");
//        List<String> copy = new ArrayList<>(uuids);
//        Path working = Paths.get("IO/sort/working");
//        Path mzk = Paths.get("IO/sort/mzk");
//        Path ndk = Paths.get("IO/sort/ndk");
//        Path other = Paths.get("IO/sort/other");
//        Path childless = Paths.get("IO/sort/childless");
//
//        for (String uuid : uuids) {
//            try {
//                // sort uuid
//                String origin = fedoraUtils.getOrigin(uuid);
//                String entry = uuid + "\n";
//                if (origin == null) {
//                    Files.write(childless, entry.getBytes(), StandardOpenOption.APPEND);
//                } else if (origin.equals(fedoraUtils.MZK)) {
//                    Files.write(mzk, entry.getBytes(), StandardOpenOption.APPEND);
//                } else if (origin.equals(fedoraUtils.NDK)) {
//                    Files.write(ndk, entry.getBytes(), StandardOpenOption.APPEND);
//                } else if (origin.equals(fedoraUtils.OTHER)) {
//                    Files.write(other, entry.getBytes(), StandardOpenOption.APPEND);
//                }
//                // save progress
//                copy.remove(uuid);
//                Files.write(working, copy, Charset.forName("UTF-8"));
//            } catch (IOException e) {
//                LOGGER.error(e.getMessage());
//                LOGGER.error("Could no get document origin: " + uuid);
//            }
//        }
//    }

    @Override
    public String getUsage() {
        return "Sorts a list of uuids to 3 groups - MZK/NDK/other\n" +
                "other = other + unknown";
    }
}
