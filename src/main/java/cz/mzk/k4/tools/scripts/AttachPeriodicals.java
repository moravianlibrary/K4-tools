package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.api.ClientRemoteApi;
import cz.mzk.k4.tools.api.KrameriusClientRemoteApiFactory;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import domain.Context;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by holmanj on 2.3.15.
 */
public class AttachPeriodicals implements Script {
    private static final Logger LOGGER = Logger.getLogger(AttachPeriodicals.class);
    private static AccessProvider access = new AccessProvider();

    @Override
    public void run(List<String> args) {
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
        Map<String,String> uuidMap = findParents(childList); // key = child, value = parent
        for (String child : childList) {
            String parent = uuidMap.get(child);
            System.out.println("child " + child + ", parent " + parent);
            // atd
        }
        // TODO možná u některých jen reindexovat rodiče - skoro určitě: ty, co to najde bez hvězdičky zařadit; ty, co to potom najde jen s hvězdičkou stačí reindexovat (nemusí se ani rodič - možná spíš přes gui)
        // přidá uuid do RELS-EXT rodiče
        // reindexuje rodiče
    }

    // specifický dotaz pro tenhle skript - vlastní metoda místo té v utils
    public List<String> solrQuery(String query) throws MalformedURLException, SolrServerException {
        HttpSolrServer solr = new HttpSolrServer("http://" + access.getSolrHost());
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

    private Map<String,String> findParents(List<String> uuidList) {
        Map<String,String> uuidMap = new HashMap<>(); // key = child, value = parent
        ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi("kramerius4.nkp.cz", "", "");
        for (String child : uuidList) {
            Context[] contexts = k5Api.getItem(child).getContext()[0];
            int hloubka = contexts.length;
            // poslední kontext je daný objekt, kontext před ním je jeho rodič (indexace od 0)
            String parent = contexts[hloubka-2].getPid();
            // TODO co chyby?
            uuidMap.put(child, parent);
        }
        return uuidMap;
    }

    @Override
    public String getUsage() {
        return "pripojeniPeriodik\n" +
                "Projde nepřipojené objekty daného modelu, podívá se, kde jsou v NK a případně je podle toho připojí.\n" +
                "Argument: model";
    }
}
