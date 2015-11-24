package cz.mzk.k4.tools.scripts;

import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.domain.Context;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by holmanj on 2.3.15.
 */
public class AttachPeriodicalsNDK implements Script {
    private static final Logger LOGGER = Logger.getLogger(AttachPeriodicalsNDK.class);
    private static AccessProvider accessProvider = AccessProvider.getInstance();
    private static ProcessRemoteApi krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
            accessProvider.getKrameriusHost(),
            accessProvider.getKrameriusUser(),
            accessProvider.getKrameriusPassword());
    private static FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);
//    private static KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);

    @Override
    public void run(List<String> args) {  // periodicalitem, periodicalvolume, volume (?), supplement, page (?), monographunit
        // najde seznam všech nepřipojených urč. modelu
        // http://krameriusndktest.mzk.cz/solr/select/?q=model_path%3Aperiodicalvolume*
        // narozdíl od document_type bere jen nejvyšší model ve stromu (připojené nevypíše)
        String model = args.get(0);
        model = model.replace("model:", ""); // parametr může být včetně model: i bez
        // model_path:periodicalvolume
        String query = "model_path:" + model + "*";
        List<String> childList = null;
        try {
            childList = solrQuery(query);
        } catch (MalformedURLException e) {
            LOGGER.error("Špatné url solr serveru");
            e.printStackTrace();
        } catch (SolrServerException e) {
            LOGGER.error("Chyba solr serveru");
            e.printStackTrace();
        }

        for (String pid : childList) {
            System.out.println(pid);
        }
        // zavolá API v Praze a najde rodiče
        Map<String, String> uuidMap = findParents(childList); // key = child, value = parent
        for (String child : childList) {
            String parent = uuidMap.get(child);
            System.out.println("child: " + child + ", parent: " + parent);

            if (args.contains("repair")) {
                if (parent == null) {
                    LOGGER.warn("Rodič objektu " + child + " je null.");
                } else {
                    LOGGER.info("Přiřazování objektu " + child + " rodiči " + parent);
                    try {
                        fedoraUtils.addChild(parent, child);
                    } catch (CreateObjectException e) {
                        e.printStackTrace();
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (args.contains("repair") || args.contains("reindex")) {
            // reindexovat rodiče (každého jen 1x)
            // při hledání bez hvězdičky - mělo by stačit indexovat rodiče
            // tak asi ne..
            for (String parent : makeUnique(uuidMap.values())) {
                LOGGER.info("Reindexace rodiče " + parent);
                try {
                    krameriusApi.reindex(parent);
                } catch (K5ApiException e) {
                    LOGGER.error("Selhalo plánování reindexace dokumentu " + parent);
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    // specifický dotaz pro tenhle skript - vlastní metoda místo té v utils
    public List<String> solrQuery(String query) throws MalformedURLException, SolrServerException {
        HttpSolrServer solr = new HttpSolrServer("http://" + accessProvider.getSolrHost());
        SolrQuery parameters = new SolrQuery();
        parameters.setQuery(query);
        parameters.setFields("root_pid");
        parameters.set("rows", "1000000000");

        LOGGER.info("Calling solr url: " + solr.getBaseURL() + " query: " + parameters.getQuery());
        QueryResponse response = solr.query(parameters);
        SolrDocumentList results = response.getResults();

        List<String> pidList = new ArrayList<>();
        LOGGER.info("Response contains " + results.size() + " items");
        for (int i = 0; i < results.size(); ++i) {
            String uuid = (String) results.get(i).get("root_pid");
            if (!pidList.contains(uuid))
                pidList.add(uuid);
        }
        LOGGER.info("Result contains " + pidList.size() + " items");
        return pidList;
    }

    private Map<String, String> findParents(List<String> uuidList) {
        Map<String, String> uuidMap = new HashMap<>(); // key = child, value = parent
        ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi("kramerius4.nkp.cz", "", "");
        for (String child : uuidList) {
            try {
                Context[] contexts = k5Api.getItem(child).getContext()[0];
                int hloubka = contexts.length;
                // poslední kontext je daný objekt, kontext před ním je jeho rodič (indexace od 0)
                String parent = contexts[hloubka - 2].getPid();
                uuidMap.put(child, parent);
            } catch (K5ApiException ex) {
                LOGGER.warn("Error " + ex.getMessage() + ": " + child);
            }

        }
        return uuidMap;
    }

    private List<String> makeUnique(Collection<String> pids) {
        Set<String> hashSet = new HashSet<>(pids);
        List pidsUnique = new ArrayList<String>();
        pidsUnique.addAll(hashSet);
        return pidsUnique;
    }

    @Override
    public String getUsage() {
        return "pripojeniPeriodik\n" +
                "Projde nepřipojené objekty daného modelu, podívá se, kde jsou v NK a případně je podle toho připojí.\n" +
                "Povinný argument: model potomků \n" +
                "Nepovinné argumenty: slovo \"reindex\" pro indexaci rodičů bez opravy, slovo \"repair\" pro opravu včetně indexace rodičů.\n" +
                "Použití jen modelu pouze dohledá vypíše rodiče odpojených částí. \n" +
                "Je lepší nejdřív jen indexovat rodiče (když není jistota, že je to fakt odpojené - občas je fedora ok a jen to není v indexu).";
    }
}
