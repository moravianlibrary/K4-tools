package cz.mzk.k4.tools.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 4.3.15.
 */
public class SolrUtils {

    private static final Logger LOGGER = LogManager.getLogger(SolrUtils.class);
    private AccessProvider accessProvider;

    public SolrUtils(AccessProvider accessProvider) {
        this.accessProvider = accessProvider;
    }

    public List<String> solrQuery(String query) throws IOException, SolrServerException, InterruptedException {
        return solrQuery(query, "PID");
    }

//    public List<String> deepSolrQuery(String query, String returnField) throws MalformedURLException, SolrServerException {
//        return deepSolrQuery(query, returnField, null);
//    }

//    public List<String> deepSolrQuery(String query, String returnField, String cursorMark) throws MalformedURLException, SolrServerException {
//        HttpSolrServer solr = new HttpSolrServer("http://" + accessProvider.getSolrHost());
//        SolrQuery parameters = new SolrQuery();
//
////        query += "&sort=id+asc&cursorMark=" + cursorMark;
//        parameters.setQuery(query);
//        parameters.setFields(returnField);
//        // deep paging: https://cwiki.apache.org/confluence/display/solr/Pagination+of+Results
//        parameters.set("rows", "10");
//        if (cursorMark != null )
//            parameters.set("cursorMark", cursorMark);
//        else
//            parameters.set("cursorMark", "*");
//
//        LOGGER.info("Calling solr url: " + solr.getBaseURL() + " query: " + parameters.getQuery() + " return field: " + returnField);
//        QueryResponse response = solr.query(parameters);
//
//        // TODO: not tested (solr 4.7 or higher needed for deep pagination)
//        SolrDocumentList results = response.getResults();
//        cursorMark = response.getNextCursorMark();
//
//        List<String> pidList = new ArrayList<>();
//        LOGGER.info("Response contains " + results.size() + " items");
//        for (int i = 0; i < results.size(); ++i) {
//            pidList.add((String) results.get(i).get(returnField));
//        }
//
//        writeToFile(pidList);
//        return pidList;
//    }


    public List<String> solrQuery(String query, String returnField) throws MalformedURLException, SolrServerException {
        HttpSolrServer solr = new HttpSolrServer("http://" + accessProvider.getSolrHost() + "/kramerius");
        SolrQuery parameters = new SolrQuery();
        parameters.setQuery(query);
        parameters.setFields(returnField);
        // deep paging: https://cwiki.apache.org/confluence/display/solr/Pagination+of+Results
        parameters.set("rows", "1000000000");
        parameters.setRequestHandler("select");

        LOGGER.info("Calling solr url: " + solr.getBaseURL() + " query: " + parameters.getQuery() + " return field: " + returnField);
        QueryResponse response = solr.query(parameters);
        // dá se použít i List<Item> beans = rsp.getBeans(Item.class);
        SolrDocumentList results = response.getResults();

        List<String> resultList = new ArrayList<>();
        LOGGER.debug("Response contains " + results.size() + " items");
        for (int i = 0; i < results.size(); ++i) {
            // TODO: vyměnit podle potřeby
            resultList.add((String) results.get(i).get(returnField));
//            if (results.get(0).size() == 0) {
//                return null;
//            }
//            resultList.addAll((ArrayList) results.get(0).get(returnField));
        }

        writeToFile(resultList);
        return resultList;
    }

    private void writeToFile(List pidList) {
        Path resultFile = Paths.get("IO/solr-result");
        try {
            Files.write(resultFile, pidList, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }
}
