package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 15.12.15.
 */
public class GetRoots implements Script {
    public static final Logger LOGGER = Logger.getLogger(GetRoots.class);
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    ProcessRemoteApi k5RemoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);

    public GetRoots() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        String listFileName = args.get(0);
        List<String> uuids = GeneralUtils.loadUuidsFromFile(listFileName);
        Path rootsFile = Paths.get("IO/roots");
        Path pairsFile = Paths.get("IO/pairs");
        Path errorsFile = Paths.get("IO/errors");
        Path sourceFile = Paths.get(listFileName);
        List<String> roots = new ArrayList<>();
        List<String> pairs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> source = new ArrayList<>(uuids);
        long counter = 0;
        for (String uuid : uuids) {
            try {
                String rootPid = k5Api.getItem(uuid).getRoot_pid();
                String pairing = "child: " + uuid + " -> root: " + rootPid;
                if (rootPid == null) {
                    DigitalObjectModel model = fedoraUtils.getModel(uuid);
                    pairing += " model: " + model.toString();
                }
                LOGGER.info(pairing);
                pairs.add(pairing);
                roots.add(rootPid);
                source.remove(uuid);
                counter++;
                if (counter % 1000 == 0) {
                    LOGGER.debug("Zpracováno " + counter + " z " + uuids.size() + " dokumentů.");
                    Files.write(rootsFile, roots, Charset.forName("UTF-8"));
                    Files.write(pairsFile, pairs, Charset.forName("UTF-8"));
                    Files.write(errorsFile, errors, Charset.forName("UTF-8"));
                    Files.write(sourceFile, source, Charset.forName("UTF-8"));
                }
            } catch (K5ApiException e) {
                LOGGER.error(e.getMessage());
                errors.add(uuid);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
        }
        try {
            Files.write(rootsFile, roots, Charset.forName("UTF-8"));
            Files.write(pairsFile, pairs, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public String getUsage() {
        return "Finds root uuids for a given list of child uuids\n" +
                "usage: roots [child_list_file_name]";
    }
}
