package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.fedoraApi.FedoraFactoryService;
import cz.mzk.k4.tools.fedoraApi.RelationshipTuple;
import cz.mzk.k4.tools.fedoraApi.RisearchService;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 12/5/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class VCGhosts implements Script {

    private static final Logger LOGGER = Logger.getLogger(VCGhosts.class);
    private FedoraUtils fedoraUtils = new FedoraUtils(AccessProvider.getInstance());
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    RisearchService risearch = FedoraFactoryService.getService(accessProvider.getFedoraHost());

    private static String OFFENDING_VC = "vc:ef1769f6-c97c-4c24-a547-b4b2055148e4";


    public VCGhosts() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        // get list of objects from risearch
        String query = "* <http://www.w3.org/1999/02/22-rdf-syntax-ns#isMemberOfCollection> <info:fedora/" + OFFENDING_VC + ">";
        List<RelationshipTuple> result = risearch.query(query, 100000);
        boolean writeEnabled = args.contains("writeEnabled");

        // remove offending VC
        for (RelationshipTuple triplet : result) {
            String uuid = triplet.getSubject().replace("info:fedora/", "");
            try {
                removeVCLink(uuid, writeEnabled);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            } catch (CreateObjectException e) {
                e.printStackTrace();
            }
        }


        // get parent pids, reindex - next step through SOLR (1 dotaz místo pro každý objekt dotaz)
        // dotaz na root_pid všeho s vazbou na danou kolekci, reindexace unikátních root_pid
        // Případně by mělo jít přímo indexovat kolekci, remote api na to má metodu. To ale možná nejde u VC co nejsou ve fedoře
        // dál viz https://github.com/moravianlibrary/kramerius/issues/126

    }


    public void removeVCLink(String uuid, boolean writeEnabled) throws IOException, CreateObjectException, TransformerException {
        File tempDom = null;
        String rdfNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        Path backupFile = Paths.get("IO/" + OFFENDING_VC);
        if (!Files.exists(backupFile)) {
            Files.createFile(backupFile);
        }
        // <rdf:isMemberOfCollection rdf:resource="info:fedora/vc:d3f011e3-f1fd-4025-a907-68a860460841"></rdf:isMemberOfCollection>

        try {
            Files.write(backupFile, (uuid + "\n").getBytes(), StandardOpenOption.APPEND);
            Document dom = fedoraUtils.getRelsExt(uuid);

            // smazat elementy <file> (např. vazba na DjVu obrázek)
            NodeList vcNodeList = dom.getElementsByTagNameNS(rdfNS, "isMemberOfCollection");
            int vcNodeListLength = vcNodeList.getLength(); // délka seznamu se během cyklu mění
            for (int i = 0; i < vcNodeListLength; i++) {
                Node vcLink = vcNodeList.item(i);
                if (vcLink.getAttributes().getNamedItem("rdf:resource").getTextContent().equals("info:fedora/" + OFFENDING_VC)) {
                    vcLink.getParentNode().removeChild(vcLink);
                    i--; // seznam se odmazáváním zmenšuje, je potřeba změnšit index
                    vcNodeListLength--;
                }
            }

            //save XML file temporary
            tempDom = File.createTempFile("relsExt", ".rdf");
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(dom), new StreamResult(tempDom));
            //Copy temporary file to document
            if (writeEnabled) {
                fedoraUtils.setRelsExt(uuid, tempDom.getAbsolutePath());
            }

        } catch (CreateObjectException e) {
            throw new CreateObjectException("Chyba při změně XML: " + e.getMessage());
        } catch (TransformerConfigurationException e) {
            throw new TransformerConfigurationException("Chyba při změně XML: " + e.getMessage());
        } catch (TransformerException e) {
            throw new TransformerException("Chyba při změně XML: " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("Chyba při změně XML: " + e.getMessage());
        } finally {
            if (tempDom != null) {
                tempDom.delete();
            }
        }
    }

    @Override
    public String getUsage() {
        return null;
    }

}
