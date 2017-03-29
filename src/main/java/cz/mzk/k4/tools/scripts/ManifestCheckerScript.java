package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.Script;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.List;

/**
 * ManifestCheckerScript
 *
 * @author Martin Rumanek
 */
public class ManifestCheckerScript implements Script {

    private static final Logger LOGGER = LogManager.getLogger(ManifestCheckerScript.class);

    @Override
    public void run(List<String> args) {
        try {
            NodeList nodes = null;
            int start = 0;
            do {
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet("http://kramerius.mzk.cz/search/api/v5.0/" +
                        "search?q=fedora.model:monograph&fl=PID&rows=100&start=" + start);
                start += 100;
                InputStream is = httpclient.execute(httpGet).getEntity().getContent();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(is);
                httpclient.close();

                XPathFactory xPathFactory = XPathFactory.newInstance();
                XPath xpath = xPathFactory.newXPath();
                XPathExpression expression = xpath.compile("//str[@name='PID']/text()");
                nodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

                for (int i = 0; i < nodes.getLength(); i++) {
                    String uuid = nodes.item(i).getNodeValue();

                    HttpGet manifestGet = new HttpGet(
                            new StringBuilder("http://kramerius.mzk.cz/search/api/v5.0/iiif/").append(uuid)
                            .append("/manifest").toString());

                    CloseableHttpClient client = HttpClients.createDefault();

                    int status = client.execute(manifestGet).getStatusLine().getStatusCode();
                    client.close();
                    LOGGER.info(uuid + ": " + status);
                    if (status != 200) {
                        LOGGER.error("manifest error: " + uuid);
                    }
                }
            }
            while (nodes.getLength() > 0) ;
        } catch (Exception e) {
        }
    }

    @Override
    public String getUsage() {
        return null;
    }
}
