package cz.mzk.k4.tools.scripts;

import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.SolrUtils;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created by holmanj on 13.7.15.
 */
public class RepairTrees implements Script {

    private  AccessProvider accessProvider = AccessProvider.getInstance();
    private  ProcessRemoteApi krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
                        accessProvider.getKrameriusHost(),
                        accessProvider.getKrameriusUser(),
                        accessProvider.getKrameriusPassword());
    private  FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);
    private  KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);
    private  SolrUtils solr = new SolrUtils(accessProvider);
    private  ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    public static final Logger LOGGER = Logger.getLogger(RepairTrees.class);

    public RepairTrees() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        try {
            // get all top level uuids
//            List<String> topLevelUuids = new ArrayList<>();
            List<String> topLevelUuids = solr.solrQuery("model_path:periodical");

            // repair trees
            for (String topLevelUuid : topLevelUuids) {
                LOGGER.info("Prochází se strom " + topLevelUuid);
                boolean changed = repairTree(topLevelUuid);

                // indexace při změně
                if (changed) {
                    LOGGER.info("Došlo ke změně dostupnosti, plánuje se indexace dokumentu " + topLevelUuid);
                    krameriusApi.reindex(topLevelUuid);
                } else {
                    LOGGER.info("Strom " + topLevelUuid + " je OK");
                }
            }
            LOGGER.info("Prolezeno.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (K5ApiException e) {
            LOGGER.error("Selhalo plánování reindexace dokumentu");
            LOGGER.error(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    @Override
    public String getUsage() {
        return "Projde (všechny) dokumenty v K5 a opraví dostupnosti tak, aby pod uzlem s dostupností private byly zase jen provate uzly (nastavuje public směrem nahoru).";
    }
}
