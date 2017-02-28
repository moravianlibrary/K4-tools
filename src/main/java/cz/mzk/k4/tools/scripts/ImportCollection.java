package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by hradskam on 29.7.14.
 */
public class ImportCollection implements Script {
    private static final Logger LOGGER = Logger.getLogger(ImportCollection.class);
    private AccessProvider accessProvider = AccessProvider.getInstance();
    private ProcessRemoteApi krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
            accessProvider.getKrameriusHost(),
            accessProvider.getKrameriusUser(),
            accessProvider.getKrameriusPassword());

    public ImportCollection() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {

        /*if(args.size() < 2 || !args.get(0).contains("uuid:") || !args.get(1).contains("vc:")) {
            System.out.println("Musí být zadáno první uuid dokumentu a poté uuid sbírky.");
            return;
        }*/

        if(!args.get(1).contains("vc:")){
            System.out.print("Zlý formát sbírky.");
            return;
        }

        KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);

        LOGGER.info("Začátek přidávání do sbírky.");

        Path importList = Paths.get(args.get(0));

        //String pid = args.get(0).substring(("uuid:").length());
        String collectionPid = args.get(1); //toto tu pravdepodobne nemá čo robiť, debugovať, malo by vo výsledku byť vc:xxxx

        List<String> listOfObjects = null;
        try {
            listOfObjects = Files.readAllLines(importList);
        } catch (IOException e) {
            System.out.println("Could not open file.");
            return;
        }

        if(listOfObjects == null) return;
        String objectUuid;
        for(int i=0;i<listOfObjects.size();i++) {
            objectUuid = listOfObjects.get(i);
            if(objectUuid.isEmpty() || !objectUuid.contains("uuid:")){
                continue;
            }
            else {
                try {
                    krameriusApi.addToCollection(listOfObjects.get(i), collectionPid);
                } catch (K5ApiException e) {
                    LOGGER.error("Chyba pri pridávaní do zbierky.");
                }
                //krameriusUtils.addToCollection(listOfObjects.get(i), collectionPid);
            }
        }
        LOGGER.info("Konec přidávání do sbírky.");

    }

    @Override
    public String getUsage() {
        return "First argument is file with list of uuid-s to be imported to VC.\n" +
                "Second argument is uuid of virtual colletcion with format vc:xxxx\n" +
                "Script will import uuids from file to virtual collection.";
    }
}
