package cz.mzk.k4.tools.scripts;

import com.sun.jersey.api.client.UniformInterfaceException;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.SolrUtils;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.domain.FedoraNamespaces;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.common.K5ApiException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jan on 12.6.16.
 */
public class IdentificatorSearch implements Script {
    private static final Logger LOGGER = LogManager.getLogger(IdentificatorSearch.class);
    private FedoraUtils fedoraUtils = new FedoraUtils(AccessProvider.getInstance());
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    SolrUtils solr = new SolrUtils(accessProvider);

    public IdentificatorSearch() throws FileNotFoundException {
    }


    @Override
    public void run(List<String> args) {
        String filename = args.get(0);
        List<String> uuids = GeneralUtils.loadUuidsFromFile(filename);
        Map<String, String> idMap = null;
        String returnField = "dc.identifier";
        String query;

        Path resultFile = Paths.get("IO/vsechno-identifikatory.csv");
        Path errorsFile = Paths.get("IO/solr-errors");
//        try {
//            Files.write(resultFile, "uuid;ccnb;barcode\n".getBytes(), StandardOpenOption.APPEND);
//          sysno),ččnb, issn, čárový kód, signaturu a půjde-li to, i název"
//            Files.write(resultFile, "uuid;imageserver;model;sigla;ccnb;sysno;barcode;issn;signatura;titul\n".getBytes(), StandardOpenOption.APPEND);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        for (String uuid : uuids) {
            query = "PID:\"" + uuid + "\"";
            try {
                idMap = fedoraQuery(uuid);
                if (idMap == null) {
                    // log
                    Files.write(errorsFile, (uuid + "\n").getBytes(), StandardOpenOption.APPEND);
                    continue;
                }
            } catch (MalformedURLException e) {
                LOGGER.error("Špatné url solr serveru");
                e.printStackTrace();
//            } catch (SolrServerException e) {
//                LOGGER.error("Chyba solr serveru");
//                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // TITLE, UUID
            String title = "";
            try {
                title = clientApi.getItem(uuid).getTitle().replace(";", ","); // kvuli CSV
            } catch (K5ApiException e) {
                LOGGER.warn("Could not get title of " + uuid);
            }
            idMap.put("title", title);

            try {
                DigitalObjectModel model = fedoraUtils.getModel(uuid);
                idMap.put("model", model.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                String sigla = fedoraUtils.getModsLocationElement(uuid);
                idMap.put("sigla", sigla);
            } catch (IOException e) {
                e.printStackTrace();
            }


            // titul;uuid;ccnb;sysno;barcode;issn;signatura
            String line = uuid + ";"
                    + "NDK" + ";"
                    + idMap.get("model") + ";"
                    + idMap.get("sigla") + ";"
                    + idMap.get("ccnb") + ";"
                    + idMap.get("sysno") + ";"
                    + idMap.get("barCode") + ";"
                    + idMap.get("issn") + ";"
                    + idMap.get("signature") + ";"
                    + idMap.get("title") + "\n";

            // vypis
            try {
                Files.write(resultFile, line.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


//    @Override
    public void run_solr(List<String> args) throws FileNotFoundException {
        SolrUtils solr = new SolrUtils(new AccessProvider());

        String filename = args.get(0);
        List<String> uuids = GeneralUtils.loadUuidsFromFile(filename);
        List<String> idList = null;
        String returnField = "dc.identifier";
        String query;

        Path resultFile = Paths.get("IO/vsechno-identifikatory.csv");
        Path errorsFile = Paths.get("IO/solr-errors");
        try {
            Files.write(resultFile, "titul;uuid;ccnb;sysno;barcode;issn;signatura\n".getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String uuid : uuids) {
            query = "PID:\"" + uuid + "\"";
            try {
                idList = solr.solrQuery(query, returnField);
                if (idList == null) {
                    // log
                    Files.write(errorsFile, (uuid + "\n").getBytes(), StandardOpenOption.APPEND);
                    continue;
                }
            } catch (MalformedURLException e) {
                LOGGER.error("Špatné url solr serveru");
                e.printStackTrace();
            } catch (SolrServerException e) {
                LOGGER.error("Chyba solr serveru");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // TITLE, UUID
            String title = "";
            try {
                title = clientApi.getItem(uuid).getTitle().replace(";", ","); // kvuli CSV
            } catch (K5ApiException e) {
                LOGGER.warn("Could not get title of " + uuid);
            }
            String line = title + ";" + uuid + ";";

            // CCNB
            for (String id : idList) {
                if (id.startsWith("ccnb:")) {
                    line += id.replace("ccnb:", "");
                }
            }

            // SYSNO
            line += ";";
            for (String id : idList) {
                if (id.startsWith("sysno:")) {
                    line += id.replace("sysno:", "");
                }
            }

            // BARCODE
            line += ";";
            for (String id : idList) {
                if (id.startsWith("barCode:")) {
                    line += id.replace("barCode:", "");
                }
            }

            // ISSN
            line += ";";
            for (String id : idList) {
                if (id.startsWith("issn:")) {
                    line += id.replace("issn:", "");
                }
            }

            // SIGNATURA
            line += ";";
            for (String id : idList) {
                if (id.startsWith("signature:")) {
                    line += id.replace("signature:", "");
                }
            }

            // vypis
            line += "\n";
            try {
                Files.write(resultFile, line.getBytes(), StandardOpenOption.APPEND);
//                System.out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private Map<String, String> fedoraQuery(String uuid) {
        Map<String, String> ids = new HashMap<>();
        // inicializace proti NullPointerException
        ids.put("title","");
        ids.put("model","");
        ids.put("sigla","");
        ids.put("uuid","");
        ids.put("ccnb","");
        ids.put("sysno","");
        ids.put("barCode","");
        ids.put("issn","");
        ids.put("signature","");
        try {
            Document mods = fedoraUtils.getMODSStream(uuid);
            NodeList idNodes = mods.getElementsByTagNameNS(FedoraNamespaces.BIBILO_MODS_URI, "identifier");
            if (idNodes.getLength() == 0) {
                idNodes = mods.getElementsByTagName("identifier");
            }
            for (int i = 0; i < idNodes.getLength(); i++) {
                Node idNode = idNodes.item(i);
                String type = idNode.getAttributes().getNamedItem("type").getTextContent();
                String value = idNode.getTextContent();
                ids.put(type, value);
            }
            NodeList shelfLocators = mods.getElementsByTagNameNS(FedoraNamespaces.BIBILO_MODS_URI, "shelfLocator");
            if (shelfLocators.getLength() == 0) {
                shelfLocators = mods.getElementsByTagName("shelfLocator");
//            } else if (shelfLocators.getLength() > 1) {
//                LOGGER.warn("Dokument " + uuid + " obsahuje víc polí shelfLocator");
            } else {
                String signature = shelfLocators.item(0).getTextContent();
                ids.put("signature", signature);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ex) {
            return null;
        } catch (UniformInterfaceException ex) {
            return null;
        }
        return ids;
    }

    @Override
    public String getUsage() {
        return "ids [cesta k souboru se seznamem kořenových uuid]\n" +
                "Pro seznam root uuid vytáhne ze solr další identifikátory\n" +
                "Časem snad i naopak.";
    }
}
