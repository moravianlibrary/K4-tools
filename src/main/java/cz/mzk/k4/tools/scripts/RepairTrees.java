package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.*;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 13.7.15.
 */
public class RepairTrees implements Script {

    private AccessProvider accessProvider = AccessProvider.getInstance();
    private ProcessRemoteApi krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
            accessProvider.getKrameriusHost(),
            accessProvider.getKrameriusUser(),
            accessProvider.getKrameriusPassword());
    private FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);
    private KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);
    private SolrUtils solr = new SolrUtils(accessProvider);
    private ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    public static final Logger LOGGER = LogManager.getLogger(RepairTrees.class);

    public RepairTrees() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
//        Path doneFile = Paths.get("IO/repaitTrees-hotovo");
        File doneFile = new File("IO/repairTrees-hotovo");
        if (!doneFile.exists()) {
            try {
                doneFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Seznam hotových nemůže být vytvořen: " + e.getMessage());
                return;
            }
        }
//        Path errorsFile = Paths.get("IO/repairTrees-errors");
        File errorsFile = new File("IO/repairTrees-errors");
        if (!errorsFile.exists()) {
            try {
                errorsFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Seznam chyb nemůže být vytvořen: " + e.getMessage());
                return;
            }
        }
        List<String> done;
        List<String> errors;

        // get all top level uuids
        List<String> topLevelUuids;
        try {
            topLevelUuids = solr.solrQuery("model_path:periodical");
        } catch (IOException e) {
            LOGGER.error("Chyba při získávání kořenových uuid: " + e.getMessage());
            return;
        } catch (SolrServerException e) {
            LOGGER.error("Chyba při získávání kořenových uuid: " + e.getMessage());
            return;
        } catch (InterruptedException e) {
            LOGGER.error("Chyba při získávání kořenových uuid: " + e.getMessage());
            return;
        }

        // repair trees
        for (String topLevelUuid : topLevelUuids) {
            done = GeneralUtils.loadUuidsFromFile(doneFile.getPath());
            errors = GeneralUtils.loadUuidsFromFile(errorsFile.getPath());
            if (done.contains(topLevelUuid)) {
                LOGGER.debug(topLevelUuid + " už je zkontrolované");
                continue;
            } else if (errors.contains(topLevelUuid)) {
                LOGGER.warn(topLevelUuid + " je vadné");
                continue;
            }

            LOGGER.info("Prochází se strom " + topLevelUuid);
            boolean changed = false;
            try {
                changed = repairTree(topLevelUuid);
            } catch (IOException e) {
                LOGGER.error("Chyba při komunikaci s fedorou " + topLevelUuid + ": " + e.getMessage());
                writeToErrorFile(topLevelUuid, errorsFile);
                continue;
            } catch (K5ApiException e) {
                LOGGER.error("Chyba při získávání potomků " + topLevelUuid + ": " + e.getMessage());
                writeToErrorFile(topLevelUuid, errorsFile);
                continue;
            }

            // indexace při změně
            if (changed) {
                LOGGER.info("Došlo ke změně dostupnosti, plánuje se indexace dokumentu " + topLevelUuid);
                try {
                    krameriusApi.reindex(topLevelUuid);
                    Files.write(doneFile.toPath(), topLevelUuid.getBytes(), StandardOpenOption.APPEND);
                    Files.write(doneFile.toPath(), "\n".getBytes(), StandardOpenOption.APPEND);
                } catch (K5ApiException e) {
                    LOGGER.error("Selhalo plánování reindexace dokumentu: " + e.getMessage());
                    writeToErrorFile(topLevelUuid, errorsFile);
                    continue;
                } catch (IOException e) {
                    LOGGER.error("Selhal zápis do seznamu dokončených: " + e.getMessage());
                    return;
                }
            } else {
                LOGGER.info("Strom " + topLevelUuid + " je OK");
                try {
                    Files.write(doneFile.toPath(), topLevelUuid.getBytes(), StandardOpenOption.APPEND);
                    Files.write(doneFile.toPath(), "\n".getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    LOGGER.error("Selhal zápis do seznamu dokončených: " + e.getMessage());
                    return;
                }
            }
        }
        LOGGER.info("Prolezeno.");
    }

    public boolean repairTree(String uuid) throws IOException, K5ApiException {
//        if (isPublic(uuid)) {
//            // nebo ne? má cenu prolézat všechny stromy kompletně? Třeba když je špatně jen 1 ročník..?
//            LOGGER.debug("Uzel " + uuid + " už je public.");
//            return false;
//        }

        // tahle úroveň je zatím private (pokud se public větve přeskakují)
        // 2 různé prom. pro lepší čitelnost (teoreticky by stačila 1, ale neměla by vždycky stejný význam)
        boolean changed = false;
        boolean shouldBePublic = false;

        List<Item> children = k5Api.getChildren(uuid);
        for (Item child : children) {
            // rekurze
            changed |= repairTree(child.getPid()); // změnilo se něco v podstromu? (propagace nahoru, pak indexace)
            shouldBePublic |= isPublic(child); // je některý z (opravených) přímých potomků public? (propagace změny na public, nastavení změny na true)
            if (child.getModel().equals("page")) { // stačí koukat na 1 stránku, zbytek se předpokládá stejný
                break;
            }
        }

        if (shouldBePublic && !isPublic(uuid)) {
            LOGGER.info("Nastavuje se public u uzlu " + uuid);
            fedoraUtils.setPublic(uuid);
            changed = true; // došlo ke změně
        }
        return changed; // propagace změny
    }

    private boolean isPublic(Item item) throws IOException {
        return isPublic(item.getPid());
    }

    private boolean isPublic(String uuid) throws IOException {
        return fedoraUtils.isPublic(uuid);
    }

    private void writeToErrorFile(String uuid, File errorsFile) {
        try {
            Files.write(errorsFile.toPath(), uuid.getBytes(), StandardOpenOption.APPEND);
            Files.write(errorsFile.toPath(), "\n".getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.error("Selhal zápis do seznamu chyb: " + e.getMessage());
            return;
        }
    }

    @Override
    public String getUsage() {
        return "Projde (všechny) dokumenty v K5 a opraví dostupnosti tak, aby pod uzlem s dostupností private byly zase jen private uzly (nastavuje public směrem nahoru).";
    }
}
