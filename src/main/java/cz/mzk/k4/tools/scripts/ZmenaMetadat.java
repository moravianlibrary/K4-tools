package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by holmanj on 29.4.16.
 */
public class ZmenaMetadat implements Script {

    private static final Logger LOGGER = Logger.getLogger(TestScript.class);
    private FedoraUtils fedoraUtils;
    AccessProvider accessProvider;
    ClientRemoteApi clientApi;
    ProcessRemoteApi remoteApi;

    public ZmenaMetadat() throws FileNotFoundException {
        fedoraUtils = new FedoraUtils(AccessProvider.getInstance());
        accessProvider = AccessProvider.getInstance();
        clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
        remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    }

    @Override
    public void run(List<String> args) throws FileNotFoundException {
        // TODO: zvlášť metody na stažení - změnu - uložení do fedory
        // seznam uuid objektů na změnu metadat
        List<String> uuids = GeneralUtils.loadUuidsFromFile("IO/mezi-roots");
        // xpath na vyhledání elementu v xml
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        String xpathExpression = "//*[local-name()='dateIssued' and not(@*)]"; // změnit podle potřeby
        Element element = null;
        // specifické pro změnu z "mezi rok a rok]" na "rok-rok"
        Pattern p = Pattern.compile("\\d{4}"); // pro vyhledání hodnot(y) v textu - tady hledání roku
        String from = "";
        String to = "";
        String newDateIssued = "";

        for (String uuid : uuids) {
            Document mods = null; // příp. DC
            String dateIssued = "";
            try {
                mods = fedoraUtils.getMODSStream(uuid);
            } catch (IOException e) {
                LOGGER.error("Could not get MODS stream of " + uuid);
                continue;
            }
            try {
                // nalezení elementu pro změnu
                element = (Element) xpath.evaluate(xpathExpression, mods, XPathConstants.NODE);
                dateIssued = element.getTextContent();
            } catch (XPathExpressionException e) {
                LOGGER.error("Could not get MODS element " + xpathExpression + " of " + uuid);
                continue;
            }
            // pattern matching - hledání hodnoty v textu
            Matcher m = p.matcher(dateIssued);
            m.find(); // najde 1. výskyt
            from = m.group();
            m.find(); // najde 2. výskyt
            to = m.group();
            newDateIssued = from + "-" + to;

            element.setTextContent(newDateIssued); // změna obsahu elementu

            // uložení změněného xml do souboru
            String outputPath = "IO/output.xml";
            Transformer transformer = null;
            try {
                transformer = TransformerFactory.newInstance().newTransformer();
                Result output = new StreamResult(new File(outputPath));
                Source input = new DOMSource(mods);
                transformer.transform(input, output);
            } catch (TransformerConfigurationException e) {
                LOGGER.error("Nepodařilo se uložit změněný MODS dokumentu " + uuid + " do souboru");
                continue;
            } catch (TransformerException e) {
                LOGGER.error("Nepodařilo se uložit změněný MODS dokumentu " + uuid + " do souboru");
                continue;
            }

            try {
                fedoraUtils.setModsStream(uuid, outputPath);
            } catch (CreateObjectException e) {
                LOGGER.error("Nepodařilo se uložit změněný MODS dokumentu " + uuid + " do fedory");
                continue;
            }

            try {
                remoteApi.reindex(uuid);
            } catch (K5ApiException e) {
                LOGGER.error("Nepodařilo se naplánovat reindexaci dokumentu " + uuid);
                continue;
            }


            LOGGER.info(uuid + " dateIssued " + dateIssued + " vyměněno za " + newDateIssued);
        }
    }

    @Override
    public String getUsage() {
        return "Vyhledání a změna elementu v metadatech (MODS nebo DC). Víc viz komentáře v kódu.";
    }
}
