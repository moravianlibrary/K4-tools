package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.SolrUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by holmanj on 2.3.15.
 */
public class SolrDotaz implements Script {
    private static final Logger LOGGER = Logger.getLogger(SolrDotaz.class);

    @Override
    public void run(List<String> args) {
        SolrUtils solr = new SolrUtils(new AccessProvider());

        String query = args.get(0);
        List<String> pidList = null;
        try {
            pidList = solr.solrQuery(query);
        } catch (MalformedURLException e) {
            LOGGER.error("Špatné url solr serveru");
            e.printStackTrace();
        } catch (SolrServerException e) {
            LOGGER.error("Chyba solr serveru");
            e.printStackTrace();
        }

        for (String pid : pidList) {
            System.out.println(pid);
        }
    }

    @Override
    public String getUsage() {
        return "solr\n" +
                "Zavolá solr dotaz v parametru a vrátí seznam UUID vrácených sokumentů.\n" +
                "Argumenty: solr dotaz";
    }
}
