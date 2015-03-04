package cz.mzk.k4.tools.utils;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 4.3.15.
 */
public class SolrUtils {

    private static final Logger LOGGER = Logger.getLogger(SolrUtils.class);
    private AccessProvider accessProvider;

    public SolrUtils(AccessProvider accessProvider) {
        this.accessProvider = accessProvider;
    }

    public List<String> solrQuery(String query) throws MalformedURLException, SolrServerException {
        HttpSolrServer solr = new HttpSolrServer("http://" + accessProvider.getSolrHost());
        SolrQuery parameters = new SolrQuery();
        parameters.setQuery(query);
        parameters.setFields("PID");
        // místo iterací: http://heliosearch.org/solr/paging-and-deep-paging/
        parameters.set("rows", "1000000000");

        LOGGER.info("Calling solr url: " + solr.getBaseURL() + " query: " + parameters.getQuery());
        QueryResponse response = solr.query(parameters);
        // dá se použít i List<Item> beans = rsp.getBeans(Item.class);
        SolrDocumentList results = response.getResults();

        List<String> pidList = new ArrayList<>();
        LOGGER.info("Response contains " + results.size() + " items");
        for (int i = 0; i < results.size(); ++i) {
            pidList.add((String) results.get(i).get("PID"));
        }
        return pidList;
    }

}
