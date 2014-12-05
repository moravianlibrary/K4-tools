package cz.mzk.k4.tools.utils;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
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
//        MultivaluedMap queryParams = new MultivaluedMapImpl();
//        queryParams.add("def", "delete");
        WebTarget resource = accessProvider.getKrameriusRESTWebResource("");
        Response response = resource.queryParam("def", "delete")
//                .type(MediaType.APPLICATION_JSON)
                .request(MediaType.APPLICATION_JSON)
//                .entity(json, MediaType.APPLICATION_JSON)
                .post(Entity.json(json));


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
//        MultivaluedMap queryParams = new MultivaluedMapImpl();
//        queryParams.add("def", "setprivate");
        WebTarget resource = accessProvider.getKrameriusRESTWebResource("");
        Response response = resource.queryParam("def", "setprivate")
                .request(MediaType.APPLICATION_JSON)
//                .type(MediaType.APPLICATION_JSON)
//                .entity(json, MediaType.APPLICATION_JSON)
                .post(Entity.json(json));

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
//        MultivaluedMap queryParams = new MultivaluedMapImpl();
//        queryParams.add("def", "setpublic");
        WebTarget resource = accessProvider.getKrameriusRESTWebResource("");
        Response response = resource.queryParam("def", "setpublic")
                .request(MediaType.APPLICATION_JSON)
//                .type(MediaType.APPLICATION_JSON)
//                .entity(json, MediaType.APPLICATION_JSON)
                .post(Entity.json(json));

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
            WebTarget resource = accessProvider.getKrameriusWebResource("/solr/select?indent=on&version=2.2&q=document_type%3A" + model + "&fq=&start=" + offset + "&rows=1000&fl=PID");
            Document xml = resource.request(MediaType.APPLICATION_XML).get(Document.class);

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
//        MultivaluedMap queryParams = new MultivaluedMapImpl();
//        queryParams.add("def","export");
        WebTarget resource = accessProvider.getKrameriusRESTWebResource("");
        Response response = resource.queryParam("def","export")
                .request(MediaType.APPLICATION_JSON)
//                .type(MediaType.APPLICATION_JSON)
//                .entity(json,MediaType.APPLICATION_JSON)
                .post(Entity.json(json));
        if(response.getStatus() == 201) {
            LOGGER.info("Exportováno: " + pid);
        } else {
            LOGGER.error("Export " + pid + "se nepodaril. CHYBA: " + response.getStatus());
        }
    }

    public void reindex(String pid) {
//        MultivaluedMap queryParams = new MultivaluedMapImpl();
//        queryParams.add("action","start");
//        queryParams.add("def", "reindex");
//        queryParams.add("out", "text");
//        queryParams.add("params", "reindexDoc," + pid);
        WebTarget resource = accessProvider.getKrameriusWebResource("/search//lr");
        Response response = resource
                .queryParam("action","start")
                .queryParam("def", "reindex")
                .queryParam("out", "text")
                .queryParam("params", "reindexDoc," + pid)
                .request()
                .get();
        if(response.getStatus() == 200){
            LOGGER.info("Reindexováno: " +pid);
        } else {
            LOGGER.error("Nepodařila se reindexace souboru " + pid);
        }

    }

    public void addToCollection(String pid, String collectionPid) {
//        MultivaluedMap queryParams = new MultivaluedMapImpl();
//        queryParams.add("action","start");
//        queryParams.add("def", "aggregate");
//        queryParams.add("out", "text");
        String nparams = "{virtualcollections;{add;uuid\\:" + pid + ";vc\\:" + collectionPid + "}}}";
//        queryParams.add("nparams", nparams);
        WebTarget resource = accessProvider.getKrameriusWebResource("/search/lr");
        Response response = resource
                .queryParam("action","start")
                .queryParam("def", "aggregate")
                .queryParam("out", "text")
//                .type(MediaType.TEXT_PLAIN)
                .request(MediaType.TEXT_PLAIN).get();
        if(response.getStatus() == 200){
            LOGGER.info("Přidáno: " +pid);
        } else {
            LOGGER.error("Nepodařilo se přidat soubor " + pid);
        }

    }

    public void stopProcessess(String pid) {
//        MultivaluedMap queryParams = new MultivaluedMapImpl();
        WebTarget resource = accessProvider.getKrameriusRESTWebResource("/" + pid + "?stop");
        Response response = resource
                .request(MediaType.APPLICATION_JSON)
//                .type(MediaType.APPLICATION_JSON)
                .put(Entity.json(null));
        if(response.getStatus() == 200) {
            LOGGER.info("Zabito: " + pid);
        } else {
            LOGGER.error("Zabiti " + pid + "se nepodarilo. CHYBA: " + response.getStatus());
        }
    }

}
