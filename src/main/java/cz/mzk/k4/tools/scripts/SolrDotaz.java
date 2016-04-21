package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.SolrUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by holmanj on 2.3.15.
 */
public class SolrDotaz implements Script {
    private static final Logger LOGGER = Logger.getLogger(SolrDotaz.class);

    @Override
    public void run(List<String> args) throws FileNotFoundException {
        SolrUtils solr = new SolrUtils(new AccessProvider());

        String query = args.get(0);
        String returnField = args.get(1);
        List<String> pidList = null;
        try {
            pidList = solr.solrQuery(query, returnField);
        } catch (MalformedURLException e) {
            LOGGER.error("Špatné url solr serveru");
            e.printStackTrace();
        } catch (SolrServerException e) {
            LOGGER.error("Chyba solr serveru");
            e.printStackTrace();
        }

        Path resultFile = Paths.get("IO/roots-without-ocr");
        try {
            Files.write(resultFile, pidList, Charset.forName("UTF-8"));
        } catch (IOException e) {
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
                "Argumenty: solr dotaz, id vráceného pole (jen 1) - př: PID, root_pid,..";
    }
}
