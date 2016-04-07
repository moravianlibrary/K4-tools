package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.workers.RelationshipCounterWorker;
import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k4.tools.workers.ValidateWorker;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 12/12/13.
 */
public class WtfSearch implements Script {
    private static final Logger LOGGER = Logger.getLogger(WtfSearch.class);
    private static UuidWorker worker = new RelationshipCounterWorker(false);
    private AccessProvider accessProvider;
    private KrameriusUtils krameriusUtils;
//    private ProcessRemoteApi krameriusApi;
    private FedoraUtils fedoraUtils;

    /**
     * Spustí RelationshipCounterWorker nad všemi monografiemi
     *
     * @param args - nebere argument
     */
    @Override
    public void run(List<String> args) {
        accessProvider = AccessProvider.getInstance();
//        krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
//                accessProvider.getKrameriusHost(),
//                accessProvider.getKrameriusUser(),
//                accessProvider.getKrameriusPassword());
        krameriusUtils = new KrameriusUtils(accessProvider);
        fedoraUtils = new FedoraUtils(accessProvider);

        if (args.size() == 1) {
            // validuj konkrétní model
            if (args.get(0).startsWith("model:")) {
                DigitalObjectModel model = DigitalObjectModel.parseString(args.get(0).substring(6));
                UuidWorker validateWorker = new ValidateWorker(accessProvider);
//                validateWorker.run(args.get(1));
                fedoraUtils.applyToAllUuidOfModel(model, validateWorker);
            } else {

                String uuid = args.get(0);
                LOGGER.info("Running " + this.getClass() + " on " + accessProvider.getLibraryPrefix() + ", uuid: " + uuid);

                // porovnání s triplety
                // není rekurzivní (každý model zvlášť)
                worker.run(uuid);

                // kontrola existence obrázků u stránek
                try {
                    List<String> uuidList = fedoraUtils.getChildrenUuids(uuid);

                    for (String childUuid : uuidList) {
                        DigitalObjectModel model = fedoraUtils.getModel(uuid);
                        //                 if (fedoraUtils.getModel(uuid).equals(DigitalObjectModel.PAGE)) {
                        checkImageExistence(childUuid);
                        //               }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // kontrola závislostí ve fedoře (rekurzivní prohledání stromu)
                // + kontrola konzistence ORC
                fedoraUtils.checkChildrenAndOcrExistence(uuid);
            }

        } else {

            // SOLR - porovnat s tripletama (chybějící vazby)
            List<DigitalObjectModel> modely = new ArrayList<DigitalObjectModel>();
           modely.add(DigitalObjectModel.MONOGRAPH);
            modely.add(DigitalObjectModel.PERIODICAL);
//            modely.add(DigitalObjectModel.PAGE);
//        modely.add(DigitalObjectModel.ARCHIVE);
//        modely.add(DigitalObjectModel.ARTICLE);
//        modely.add(DigitalObjectModel.GRAPHIC);
//        modely.add(DigitalObjectModel.INTERNALPART);
//        modely.add(DigitalObjectModel.MANUSCRIPT);
//        modely.add(DigitalObjectModel.MAP);
//        modely.add(DigitalObjectModel.MONOGRAPHUNIT);
//        modely.add(DigitalObjectModel.PERIODICALITEM);
//        modely.add(DigitalObjectModel.PERIODICALVOLUME);
//        modely.add(DigitalObjectModel.SOUND_UNIT);
//        modely.add(DigitalObjectModel.SOUNDRECORDING);
//        modely.add(DigitalObjectModel.SUPPLEMENT);
//        modely.add(DigitalObjectModel.TRACK);

            for (DigitalObjectModel model : modely) {
                List<String> uuidList = krameriusUtils.getUuidsByModelSolr(model.getValue());
                // všechna uuid ve fedoře? =)

                LOGGER.info("Running " + this.getClass() + " on " + accessProvider.getLibraryPrefix() + ", model: " + model.getValue());      // new thread?

                for (int i = 0; i < uuidList.size(); i++) {
                    String uuid = uuidList.get(i);

                    // porovnání s triplety
                    // není rekurzivní (každý model zvlášť)
                    // časem spustit na stránkách
                    worker.run(uuid);

                    // kontrola závislostí ve fedoře (rekurzivní prohledání stromu)
                    fedoraUtils.checkChildrenExistence(uuid);

                    try {
                        if (fedoraUtils.getModel(uuid).equals(DigitalObjectModel.PAGE)) {
                            checkImageExistence(uuid);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if ((i % 1000) == 0 && i > 1) {
                        LOGGER.info("Prohledáno " + i + " objektů");
                    }

                }
            }
        }
    }

    private void checkImageExistence(String uuid) {
        try {
            fedoraUtils.getImgFull(uuid, "image/jpeg");
   //         LOGGER.info("V pořádku: " + uuid);
        } catch (IOException e) {
            LOGGER.info("Chybí obrázek u strany " + uuid);
        }
    }

    @Override
    public String getUsage() {
        return "hledaniHaluzi\n" +
                "Projde solr, triplety a foxml stromy ve fedoře a vypíše wtf objekty.\n" +
                "Bez argumentů.";
    }
}
