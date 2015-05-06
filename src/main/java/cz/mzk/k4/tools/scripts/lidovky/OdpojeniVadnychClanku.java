package cz.mzk.k4.tools.scripts.lidovky;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Created by holmanj on 12.2.15.
 */
public class OdpojeniVadnychClanku implements Script {
    private static final Logger LOGGER = Logger.getLogger(OdpojeniVadnychClanku.class);
    private final FedoraUtils fedora = new FedoraUtils(new AccessProvider());
    private final AccessProvider accessProvider = new AccessProvider();
//    private ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());

    @Override
    public void run(List<String> args) {
        LOGGER.info("Running " + this.getClass() + " on " + accessProvider.getLibraryPrefix());

        if (args.size() != 1) {
            System.out.println("Součástí příkazu musí být i název souboru. (A nic dalšího)");
            return;
        }

        LOGGER.info("Otvírání seznamu uuid.");
        String parametr = args.get(0);
        if (parametr.startsWith("uuid:")) {
            try {
//                repairArticle(parametr);
                repairArticleDetachOnly(parametr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File inputFile = new File(parametr);
            BufferedReader reader = null;
            try {
                //Open file and load content
                reader = new BufferedReader(new FileReader(inputFile));
                String uuid = "";

                //Parse file by line
                while ((uuid = reader.readLine()) != null) {
                    uuid = parseUuid(uuid);
//                    repairArticle(uuid);
                    repairArticleDetachOnly(uuid);
                }

            } catch (FileNotFoundException e) {
                LOGGER.error("Chyba při otvírání souboru: " + args.get(0) + ". ");
                e.printStackTrace();
            } catch (IOException e) {
                LOGGER.error("Chyba při čtení souboru: ");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOGGER.error("Chyba při zavírání souboru: " + e.getStackTrace());
                    }
                }
            }
        }
    }

    private void repairArticleDetachOnly(String articleUuid) {
        // get parent (ritriples)
        List<String> parentUuids = fedora.getParentUuids(articleUuid);

        try {
            clearArticle(articleUuid);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        for (String issueUuid : parentUuids) {
//            try {
//                detach(articleUuid, issueUuid);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (TransformerException e) {
//                e.printStackTrace();
//            } catch (CreateObjectException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void clearArticle(String articleUuid) throws IOException {
        // odstranit všechny starý isOnPage null
        Document relsExt = fedora.getRelsExt(articleUuid);

        NodeList oldIsOnPageList = relsExt.getElementsByTagName("isOnPage");
        for (int i = 0; i < oldIsOnPageList.getLength(); i++) {
            Node isOnPageNode = oldIsOnPageList.item(i);
            if (isOnPageNode.getAttributes().getNamedItem("rdf:resource").getNodeValue().contains("null")) {
                isOnPageNode.getParentNode().removeChild(isOnPageNode);
            }
        }

        NodeList oldIsOnPageListNS = relsExt.getElementsByTagName("kramerius:isOnPage");
        for (int i = 0; i < oldIsOnPageListNS.getLength(); i++) {
            Node isOnPageNode = oldIsOnPageListNS.item(i);
            if (isOnPageNode.getAttributes().getNamedItem("rdf:resource").getNodeValue().contains("null")) {
                isOnPageNode.getParentNode().removeChild(isOnPageNode);
            }
        }

        try {
            PrintWriter relsFile = new PrintWriter("rels-zalohy/" + articleUuid + "-article");
//            PrintWriter relsFile = new PrintWriter("rels-zalohy/" + articleUuid + "-internalpart");
            relsFile.println(prettyPrint(relsExt));
            relsFile.close();
            // TODO: save RESL-EXT
            fedora.setRelsExt(articleUuid, "rels-zalohy/" + articleUuid + "-article");
//            fedora.setRelsExt(articleUuid, "rels-zalohy/" + articleUuid + "-internalpart");
            System.out.println("Nové RELS článku " + articleUuid + ":\n" + prettyPrint(relsExt));
        } catch (Exception e) {
            e.printStackTrace();
        }

        LOGGER.info("Článek " + articleUuid + " byl vyčištěn od nullových vazeb");
//        LOGGER.info("Příloha " + articleUuid + " byla vyčištěna od nullových vazeb");
    }

    private void detach(String articleUuid, String issueUuid) throws IOException, TransformerException, CreateObjectException {
        // odstranit vazbu z ročníku
        Document relsExtIssue = fedora.getRelsExt(issueUuid);
        Element relsElement = relsExtIssue.getDocumentElement();
        NodeList articles = relsElement.getElementsByTagName("hasIntCompPart");
        for (int i = 0; i < articles.getLength(); i++) {
            Node article = articles.item(i);
            if (article.getAttributes().getNamedItem("rdf:resource").getNodeValue().contains(articleUuid)) {
                article.getParentNode().removeChild(article);
                LOGGER.info("Byl smazán článek " + articleUuid + " z čísla " + issueUuid);

                PrintWriter relsFileIssue = new PrintWriter("rels-zalohy/" + issueUuid + "-issue");
                relsFileIssue.println(prettyPrint(relsExtIssue));
                relsFileIssue.close();
                // TODO: save RESL-EXT
                fedora.setRelsExt(issueUuid, "rels-zalohy/" + issueUuid + "-issue");
                System.out.println("Nové RELS čísla " + issueUuid + ":\n" + prettyPrint(relsExtIssue));
            }
        }

        // znova vytažení z fedory (něco se mohlo změnit, i když asi to bude vždycky jen buď všechno s NS a nebo bez NS)
        relsExtIssue = fedora.getRelsExt(issueUuid);
        relsElement = relsExtIssue.getDocumentElement();
        NodeList articlesNS = relsElement.getElementsByTagName("kramerius:hasIntCompPart");
        for (int i = 0; i < articlesNS.getLength(); i++) {
            Node article = articlesNS.item(i);
            if (article.getAttributes().getNamedItem("rdf:resource").getNodeValue().contains(articleUuid)) {
                article.getParentNode().removeChild(article);
                LOGGER.info("Byl smazán článek " + articleUuid + " z čísla " + issueUuid);

                PrintWriter relsFileIssue = new PrintWriter("rels-zalohy/" + issueUuid + "-issue");
                relsFileIssue.println(prettyPrint(relsExtIssue));
                relsFileIssue.close();
                // TODO: save RESL-EXT
                fedora.setRelsExt(issueUuid, "rels-zalohy/" + issueUuid + "-issue");
                System.out.println("Nové RELS čísla " + issueUuid + ":\n" + prettyPrint(relsExtIssue));
            }
        }
    }


//    // Složitá oprava, ne jen odpojení (+ používá K5 API a to nefungovalo)

    //    private void repairArticle(String articleUuid) throws Exception {
//        // odstranit všechny starý isOnPage
//        Document relsExt = fedora.getRelsExt(articleUuid);
//        Element relsElement = relsExt.getDocumentElement();
////        NodeList oldIsOnPageList = relsExt.getElementsByTagName("kramerius:isOnPage");
//        NodeList oldIsOnPageList = relsExt.getElementsByTagName("isOnPage");
//        for (int i = 0; i < oldIsOnPageList.getLength(); i++) {
//            Node oldIsOnPage = oldIsOnPageList.item(i);
//            oldIsOnPage.getParentNode().removeChild(oldIsOnPage);
//        }
//        try {
//            System.out.println(prettyPrint(relsExt));
//            PrintWriter relsFile = new PrintWriter("rels-zalohy/" + articleUuid + "-article");
//            relsFile.println(prettyPrint(relsExt));
//            relsFile.close();
////            fedora.setRelsExt(articleUuid, "rels-zalohy/" + articleUuid + "-article");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        LOGGER.info("Cleaned article " + articleUuid);
//
//        // get item description
//        Item article = k5Api.getItem(articleUuid);
//        Context[][] contexts = article.getContext();
//
//        // get MODS stream
//        Document mods = null;
//        try {
//            mods = fedora.getMODSStream(articleUuid);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mods.getDocumentElement().normalize();
//
//        // get article parts <mods:part type="article">
//        NodeList parts = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "part");
//
//        // get page,item and volume numbers
//        // for each part: get uuids, add missing links to RDF
//        for (int i = 0; i < parts.getLength(); i++) {
//            Node part = parts.item(i);
//            int[] pageNumbers = getInterval(part);
//            String issueNumber = getItemTitle(part);
//            String volumeNumber = getVolumeTitle(part);
//            for (int j = 0; j < pageNumbers.length; j++) {
//                System.out.println(pageNumbers[j]);
//            }
//            System.out.println(issueNumber);
//            System.out.println(volumeNumber);
//
//            // get page,item and volume uuids (pro každý part se projdou všechny kontexty a najde se ten správný)
//            String issueUuid = "";
//            String volumeUuid = "";
//            // procházení celých kontextů
//            for (int j = 0; j < contexts.length; j++) {
//                issueUuid = "";
//                volumeUuid = "";
//                // procházení částí kontextu
//                for (int k = 0; k < contexts[j].length; k++) {
//                    Context context = contexts[j][k];
//                    if (context.getModel().equals("periodicalitem")) {
//                        issueUuid = context.getPid();
//                    }
//                    if (context.getModel().equals("periodicalvolume")) {
//                        volumeUuid = context.getPid();
//                    }
//                }
//                if (!issueUuid.equals("") && !volumeNumber.equals("")) {
//                    Item issue = k5Api.getItem(issueUuid);
//                    Item volume = k5Api.getItem(volumeUuid);
//
//                    // kontrola správnosti kontextu
//                    if (issue.getDetails().getPartNumber().equals(issueNumber) && volume.getDetails().getVolumeNumber().equals(volumeNumber)) {
//                        LOGGER.debug("Byl nalezen správný kontext (uuid čísla: " + issueUuid + " a ročníku: " + volumeUuid + ")");
//                        break;
//                    }
//                } else {
//                    LOGGER.error("Problém v kódu procházení kontextů");
//                }
//
//            }
//            if (issueUuid.equals("") || volumeNumber.equals("")) {
//                LOGGER.error("nenalezano uuid");
//            }
//
//            // interval čísel stran na seznam uuid
//            List<String> pageUuids = new ArrayList<>();
//            List<Item> issueChildren = k5Api.getChildren(issueUuid);
//            for (Item item : issueChildren) {
//                if (item.getModel().equals("page")) {
//                    // rozbitý API - title je třeba získat jinak
//                    String pageTitle = getPageTitle(item.getPid());
//
//                    for (int j = 0; j < pageNumbers.length; j++) {
//                        if (pageTitle.equals(String.valueOf(pageNumbers[j]))) {
//                            pageUuids.add(item.getPid());
//                        }
//                    }
//                }
//            }
//
//            if (pageUuids.size() == 0 || pageNumbers.length != pageUuids.size()) {
//                LOGGER.info("Nenalezeny uuid některých stran - počet čísel stran: " + pageNumbers.length + ", počet uuid stran: " + pageUuids.size());
//                detach(articleUuid, issueUuid);
//                LOGGER.info("Odpojení článku article uuid: " + articleUuid + " issue uuid: " + issueUuid);
//            } else {
//                System.out.println("Počet stran: " + pageUuids.size());
//                for (String pageUudid : pageUuids) {
//                    System.out.println(pageUudid);
//                }
//                repair(articleUuid, pageUuids);
//                LOGGER.info("Úspěšná oprava článku: " + articleUuid);
//            }
//        }
//    }
//
//    private void repair(String articleUuid, List<String> pageUuids) throws IOException {
//        // získat rdf článku
//        Document relsExt = fedora.getRelsExt(articleUuid);
//        Element relsElement = relsExt.getDocumentElement();
//
//        // přidat vazby - potomek uzlu <rdf:Description>, uzel <kramerius:isOnPage>
//        /*
//        rdf:RDF>
//            <rdf:Description rdf:about="info:fedora/uuid:1d2554ee-640d-4c08-a910-5698ff75c588">
//                <fedora-model:hasModel rdf:resource="info:fedora/model:article"/>
//                <oai:itemID>uuid:1d2554ee-640d-4c08-a910-5698ff75c588</oai:itemID>
//                <kramerius:isOnPage rdf:resource="info:fedora/uuid:48a28620-5bf8-11dc-91eb-0013e6840575"/>
//                <kramerius:isOnPage rdf:resource="info:fedora/uuid:489b5a30-5bf8-11dc-9791-0013e6840575"/>
//                <kramerius:isOnPage rdf:resource="info:fedora/uuid:48b54ad0-5bf8-11dc-902b-0013e6840575"/>
//                <kramerius:policy>policy:public</kramerius:policy>
//            </rdf:Description>
//        </rdf:RDF>
//         */
//
//        // dát nové isOnPage
//        for (String pageUuid : pageUuids) {
//            relsElement = relsExt.getDocumentElement();
////            Element isOnPage = relsExt.createElement("kramerius:isOnPage");
//            Element isOnPage = relsExt.createElement("isOnPage");
//            String resource = "info:fedora/" + pageUuid;
//            isOnPage.setAttribute("rdf:resource", resource);
//            isOnPage.setAttribute("xmlns:kramerius", "http://www.nsdl.org/ontologies/relationships#");
//            Node description = relsElement.getElementsByTagName("rdf:Description").item(0);
//            description.appendChild(isOnPage);
//            relsExt.normalizeDocument();
//            try {
//
//                System.out.println(prettyPrint(relsExt));
//                PrintWriter relsFile = new PrintWriter("rels-zalohy/" + articleUuid + "-article");
//                relsFile.println(prettyPrint(relsExt));
//                relsFile.close();
//
////                fedora.setRelsExt(articleUuid, "rels-zalohy/" + articleUuid + "-article");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private String getPageTitle(String pageUuid) {
//        Document dc = null;
//        try {
//            dc = fedora.getDCStream(pageUuid);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String pageTitle = "";
//        String startXPath = "//dc:title/text()";
//        XPath xPath = XPathFactory.newInstance().newXPath();
//        xPath.setNamespaceContext(new NamespaceContext() {
//            public String getNamespaceURI(String prefix) {
//                if (prefix == null) throw new NullPointerException("Null prefix");
//                else if ("dc".equals(prefix)) return "http://purl.org/dc/elements/1.1/";
//                return XMLConstants.NULL_NS_URI;
//            }
//
//            // This method isn't necessary for XPath processing.
//            public String getPrefix(String uri) {
//                throw new UnsupportedOperationException();
//            }
//
//            // This method isn't necessary for XPath processing either.
//            public Iterator getPrefixes(String uri) {
//                throw new UnsupportedOperationException();
//            }
//        });
//        try {
//            pageTitle = xPath.evaluate(startXPath, dc);
//        } catch (XPathExpressionException e) {
//            LOGGER.error("XPath error " + e);
//
//        }
//        if (pageTitle.equals("")) {
//            LOGGER.error("Error while getting page title from DC stream");
//        }
//        if (pageTitle.startsWith("[")) {
//            pageTitle = pageTitle.replace("[", "");
//            pageTitle = pageTitle.replace("]", "");
//        }
//        return pageTitle;
//    }
//
//    private int[] getInterval(Node part) {
//        // get <mods:start>10</mods:start>: //mods:extent/mods:start/text()
//        String startXPath = ".//mods:extent/mods:start/text()";
//        String endXPath = ".//mods:extent/mods:end/text()";
//        String totalXPath = ".//mods:extent/mods:total/text()";
//        XPath xPath = XPathFactory.newInstance().newXPath();
//        xPath.setNamespaceContext(new NamespaceContext() {
//            public String getNamespaceURI(String prefix) {
//                if (prefix == null) throw new NullPointerException("Null prefix");
//                else if ("mods".equals(prefix)) return "http://www.loc.gov/mods/v3";
//                return XMLConstants.NULL_NS_URI;
//            }
//
//            // This method isn't necessary for XPath processing.
//            public String getPrefix(String uri) {
//                throw new UnsupportedOperationException();
//            }
//
//            // This method isn't necessary for XPath processing either.
//            public Iterator getPrefixes(String uri) {
//                throw new UnsupportedOperationException();
//            }
//        });
//        int start = 0;
//        int end = 0;
//        int total = 0;
//        try {
//            String startString = xPath.evaluate(startXPath, part);
//            start = Integer.valueOf(startString);
//            String endString = xPath.evaluate(endXPath, part);
//            end = Integer.valueOf(endString);
//            String totalString = xPath.evaluate(totalXPath, part);
//            total = Integer.valueOf(totalString);
//        } catch (XPathExpressionException e) {
//            LOGGER.error("XPath error " + e);
//
//        }
//        int[] interval = new int[total];
//        for (int i = 0; i < total; i++) {
//            interval[i] = start;
//            start++;
//            if (start > end + 1) System.out.println("Problém..");
//        }
//        return interval;
//    }
//
//    /*
//    <mods:part type="article">
//        <mods:detail type="issue">
//            <mods:number>452</mods:number>
//            <mods:caption>číslo</mods:caption>
//        </mods:detail>
//        ...
//     */
//    private String getItemTitle(Node part) {
//        String itemTitle = "";
//        String startXPath = ".//mods:detail[@type='issue']/mods:number/text()";
//        XPath xPath = XPathFactory.newInstance().newXPath();
//        xPath.setNamespaceContext(new NamespaceContext() {
//            public String getNamespaceURI(String prefix) {
//                if (prefix == null) throw new NullPointerException("Null prefix");
//                else if ("mods".equals(prefix)) return "http://www.loc.gov/mods/v3";
//                return XMLConstants.NULL_NS_URI;
//            }
//
//            // This method isn't necessary for XPath processing.
//            public String getPrefix(String uri) {
//                throw new UnsupportedOperationException();
//            }
//
//            // This method isn't necessary for XPath processing either.
//            public Iterator getPrefixes(String uri) {
//                throw new UnsupportedOperationException();
//            }
//        });
//        try {
//            itemTitle = xPath.evaluate(startXPath, part);
//        } catch (XPathExpressionException e) {
//            LOGGER.error("XPath error " + e);
//
//        }
//        return itemTitle;
//    }
//
//    /*
//    <mods:part type="article">
//        <mods:detail type="volume">
//            <mods:number>28</mods:number>
//            <mods:caption>ročník</mods:caption>
//        </mods:detail>
//        ...
//     */
//    private String getVolumeTitle(Node part) {
//        String volumeTitle = "";
//        String startXPath = ".//mods:detail[@type='volume']/mods:number/text()";
//        XPath xPath = XPathFactory.newInstance().newXPath();
//        xPath.setNamespaceContext(new NamespaceContext() {
//            public String getNamespaceURI(String prefix) {
//                if (prefix == null) throw new NullPointerException("Null prefix");
//                else if ("mods".equals(prefix)) return "http://www.loc.gov/mods/v3";
//                return XMLConstants.NULL_NS_URI;
//            }
//
//            // This method isn't necessary for XPath processing.
//            public String getPrefix(String uri) {
//                throw new UnsupportedOperationException();
//            }
//
//            // This method isn't necessary for XPath processing either.
//            public Iterator getPrefixes(String uri) {
//                throw new UnsupportedOperationException();
//            }
//        });
//        try {
//            volumeTitle = xPath.evaluate(startXPath, part);
//        } catch (XPathExpressionException e) {
//            LOGGER.error("XPath error " + e);
//
//        }
//        return volumeTitle;
//    }
//
    private String parseUuid(String uuid) {
        return uuid.substring(uuid.indexOf("uuid:"));
    }

    @Override
    public String getUsage() {
        return "Oprava článků Lidových novin (chybějící vazby isOnPage ve směru článek - strana).\n" +
                "Parametr: uuid článku nebo cesta k txt souboru se seznamem uuid";
    }

    private static final String prettyPrint(Document xml) throws TransformerException {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
        return out.toString();
    }

}
