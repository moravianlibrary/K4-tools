package cz.mzk.k4.tools.utils;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 11/21/13
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class KrameriusUtils {

    private static final Logger LOGGER = Logger.getLogger(KrameriusUtils.class);
    private AccessProvider accessProvider;

    public KrameriusUtils(AccessProvider accessProvider) {
        this.accessProvider = accessProvider;
    }

    /**
     * Naplánuje proces mazání dokumentu (mazání provede Kramerius, vč. rekurze)
     * cz.incad.kramerius.service.impl.DeleteServiceImpl
     *
     * @param pid_path
     */
    public void exterminate(String pid_path) {
        pid_path = checkPid(pid_path);

        // {"parameters":["uuid:...","uuid:..."]}
        String json = "{\"parameters\":[\"" + pid_path + "\",\"" + pid_path + "\"]}";
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("def", "delete");
        WebResource resource = accessProvider.getKrameriusRESTWebResource("");
        ClientResponse response = resource.queryParams(queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(json, MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);


        if (response.getStatus() == 201) {
            LOGGER.info("Deleting object " + pid_path);
        } else {
            LOGGER.error("An error occured while planning deletion of document " + pid_path + " (code " + response.getStatus() + ")");
        }
    }

    public void setPrivate(String pid_path) {
        pid_path = checkPid(pid_path);
        // {"parameters":["uuid:...","uuid:..."]}
        String json = "{\"parameters\":[\"" + pid_path + "\",\"" + pid_path + "\"]}";
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("def", "setprivate");
        WebResource resource = accessProvider.getKrameriusRESTWebResource("");
        ClientResponse response = resource.queryParams(queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(json, MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);

        if (response.getStatus() == 201) {
            LOGGER.info("Setting object " + pid_path + " policy to private");
        } else {
            LOGGER.error("Could not plan process setprivate for object " + pid_path + " (code " + response.getStatus() + ")");
        }
    }

    public void setPublic(String pid_path) {
        pid_path = checkPid(pid_path);
        // {"parameters":["uuid:...","uuid:..."]}
        String json = "{\"parameters\":[\"" + pid_path + "\",\"" + pid_path + "\"]}";
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("def", "setpublic");
        WebResource resource = accessProvider.getKrameriusRESTWebResource("");
        ClientResponse response = resource.queryParams(queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(json, MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);

        if (response.getStatus() == 201) {
            LOGGER.info("Setting object " + pid_path + " policy to public");
        } else {
            LOGGER.error("Could not plan process setpublic for object " + pid_path + " (code " + response.getStatus() + ")");
        }
    }

    private String checkPid(String pid) {
        if (!pid.contains("uuid")) {
            pid = "uuid:" + pid;
        }
        return pid;
    }

    public List<String> getUuidsByModelSolr(String model) {
        List<String> uuidList = new ArrayList<String>();

        int offset = 0;
        int numFound = 1;

        LOGGER.debug("Hledání uuid modelu " + model);
        while (offset < numFound) {
            WebResource resource = accessProvider.getKrameriusWebResource("/solr/select?indent=on&version=2.2&q=document_type%3A" + model + "&fq=&start=" + offset + "&rows=1000&fl=PID");
            Document xml = resource.accept(MediaType.APPLICATION_XML).get(Document.class);

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodes = null;
            try {
                nodes = (NodeList) xPath.evaluate("/response/result/doc/str",
                        xml.getDocumentElement(), XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < nodes.getLength(); ++i) {
                Element element = (Element) nodes.item(i);
                uuidList.add(element.getTextContent());
            }

            offset += 1000;
            try {
                Node result = (Node) xPath.evaluate("/response/result",
                        xml.getDocumentElement(), XPathConstants.NODE);
                String numString = result.getAttributes().getNamedItem("numFound").getTextContent();
                numFound = Integer.parseInt(numString);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
            LOGGER.debug("Offset = " + offset + " z " + numFound);

        }
        LOGGER.info("Nalezeno celkem " + uuidList.size() + " objektů typu " + model + ".");
        return uuidList;
    }

    public void export(String pid) {
        String json = "{ \"parameters\":[ \"" + pid + "\" ]}";
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("def","export");
        WebResource resource = accessProvider.getKrameriusRESTWebResource("");
        ClientResponse response = resource.queryParams(queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(json,MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);
        if(response.getStatus() == 201) {
            LOGGER.info("Exportováno: " + pid);
        } else {
            LOGGER.error("Export " + pid + "se nepodaril. CHYBA: " + response.getStatus());
        }
    }

}
