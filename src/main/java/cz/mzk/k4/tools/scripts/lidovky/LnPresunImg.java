package cz.mzk.k4.tools.scripts.lidovky;

import com.google.common.base.CharMatcher;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.FormatConvertor;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.domain.Item;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by holmanj on 20.3.15.
 */
public class LnPresunImg implements Script {
    public static final Logger LOGGER = Logger.getLogger(LnPresunImg.class);
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);

    @Override
    public void run(List<String> args) {

        // odkomentovat kód podle fáze

        // fáze 1: načtení ročníků LN + názvů jpg obrázků (vč. kontroly)
        List<Volume> lidovky = null;
        try {
            lidovky = loadData("lidovky.ser");
        } catch (K5ApiException e) {
            e.printStackTrace();
        }

        // fáze 2: přesun do imageserveru (případně konverze do jp2)
//        try {
//            copyImages(lidovky);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // fáze 3: doplnění vazeb do fedory
//        try {
//            addDatastreams(lidovky);
//        } catch (CreateObjectException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (TransformerException e) {
//            e.printStackTrace();
//        }

        // fáze 4: (možná) čištění starých djvu - zdá se, že to nebude potřeba

    }

    private void addDatastreams(List<Volume> lidovky) throws CreateObjectException, TransformerException, IOException {
        for (Volume volume : lidovky) {
//            for (int i = 35; i < lidovky.size(); i++) {
//            Volume volume = lidovky.get(i);

            for (Issue issue : volume.getIssues()) {
                for (Page page : issue.getPages()) {
                    // datastreamy
                    fedoraUtils.setImgFullFromExternal(page.getPid(), page.getImageserverImgLocation() + "/big.jpg");
                    fedoraUtils.setImgPreviewFromExternal(page.getPid(), page.getImageserverImgLocation() + "/preview.jpg");
                    fedoraUtils.setImgThumbnailFromExternal(page.getPid(), page.getImageserverImgLocation() + "/thumb.jpg");

                    // vazba na dlaždice
                    changeRelsExt(page.getPid(), page.getImageserverImgLocation());
                }
            }
            LOGGER.info("Odkaz na obrázky z ročníku " + volume.getYear() + " byly doplněny do fedory.");
        }
    }

    private void changeRelsExt(String uuid, String imagePath) throws CreateObjectException, TransformerException, IOException {
        File tempDom = null;
        try {
            Document dom = fedoraUtils.getRelsExt(uuid);
            Element rdf = (Element) dom.getElementsByTagName("rdf:RDF").item(0);
            if (!rdf.hasAttribute("xmlns:kramerius")) {
                rdf.setAttribute("xmlns:kramerius", "http://www.nsdl.org/ontologies/relationships#");
            }
            Element djvu;
            if ((djvu = (Element) dom.getElementsByTagName("kramerius:file").item(0)) != null) {
                if (djvu.getTextContent().contains("djvu"))
                    djvu.getParentNode().removeChild(djvu);
            }
            if (dom.getChildNodes().getLength() == 0) {
                dom.appendChild(dom.createElement("rdf:Description"));
            }
            Element currentElement = (Element) dom.getElementsByTagName("rdf:Description").item(0);
            //Check if kramerius:tiles-url element exist
            if (currentElement.getElementsByTagName("kramerius:tiles-url").getLength() == 0) {

                //Add element kramerius:tiles-url
                Element tiles = dom.createElement("kramerius:tiles-url");
                tiles.setTextContent(imagePath);
                currentElement.appendChild(tiles);

                //save XML file temporary
                tempDom = File.createTempFile("relsExt", ".rdf");
                TransformerFactory.newInstance().newTransformer().transform(new DOMSource(dom), new StreamResult(tempDom));
                //Copy temporary file to document
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

    private void copyImages(List<Volume> lidovky) throws IOException {
        // sshfs holmanj@hades.mzk.cz:/data/ARCHIV /mnt/hades/
        // sshfs imageserver@imageserver.mzk.cz:/data/georef/ /mnt/imageserver -o follow_symlinks

        for (Volume volume : lidovky) {
            for (Issue issue : volume.getIssues()) {
                for (Page page : issue.getPages()) {
                    // get jpg file from Hades
                    String hadesPath = "/mnt/hades/mzk01/000/244/261/000244261/convert";
                    String restOfHPath = "/" + page.getJpgImgName().substring(0, 5) + "/jp2/" + FilenameUtils.removeExtension(page.getJpgImgName()) + ".jp2";
                    int year = Integer.parseInt(volume.getYear());
                    String convertNum = "";
                    if (year <= 1914 || year == 1919 || year == 1921 || year == 1927) {
                        convertNum = "1";
                    } else {
                        // 1915-1918, 1920, 1922-1926, 1928-1932
                        convertNum = "2";
                    }
                    hadesPath = hadesPath + convertNum + restOfHPath;
                    String imageserverPath = "/mnt/imageserver/mzk01/000/244/261/" + volume.getYear() + "/" + FilenameUtils.removeExtension(page.getJpgImgName()) + ".jp2";
                    page.setImageserverImgLocation("http://imageserver.mzk.cz/mzk01/000/244/261/" + volume.getYear() + "/" + FilenameUtils.removeExtension(page.getJpgImgName()));
                    File hadesImage = new File(hadesPath);
                    File imageserverFile = new File(imageserverPath);

                    // copy file to imageserver
                    if (imageserverFile.exists() && imageserverFile.length() != 0) {
                        // obrázek už je na imageserveru, nepokopírovat
                        continue;
                    }

                    if (!hadesImage.exists() || hadesImage.length() == 0) {
                        // na hadovi není obrázek v jpeg2000, nebo je vadný (má velikost 0) -> konvertovat nový z jpeg
                        File hadesJpgFile = new File(hadesPath.replace("jp2", "jpg")); // změna složky a suffixu
                        InputStream jp2Stream;
                        if (!hadesJpgFile.exists() || hadesJpgFile.length() == 0) {
                            // jpg na hadovi chybí - konvertovat z djvu
                            File hadesDjvuFile = new File(hadesPath.replace("jp2", "djvu")); // změna složky a suffixu
                            jp2Stream = FormatConvertor.convertDjvuToJp2(hadesDjvuFile);
                            LOGGER.warn("Obrázek " + page.getJpgImgName().replace("jpg", "jp2") + " strany " + page.getPid() + " byl zkonvertován z DjVu");
                        } else {
                            jp2Stream = FormatConvertor.convertJpgToJp2(hadesJpgFile);
                        }
                        FileUtils.copyInputStreamToFile(jp2Stream, imageserverFile);
                    } else {
                        // jpeg2000 na hadovi je ok -> jen kopírovat na imageserver
                        try {
                            FileUtils.copyFile(hadesImage, imageserverFile);
                        } catch (IOException e) {
                            LOGGER.error("Kopírování obrázku z " + hadesPath + " do " + imageserverPath + " selhalo.");
                            throw new IOException(e);
                        }
                    }
                }
            }
            LOGGER.info("Obrázky z ročníku " + volume.getYear() + " jsou zkopírovány.");
            serializeLN(lidovky, "lidovky-moved.ser");
        }
        LOGGER.info("Obrázky jsou přesunuty");
    }

    private List<Volume> loadData(String filename) throws K5ApiException {
        String serializedLN = filename;
        List<Volume> lidovky = null;
        LOGGER.info("Serializovaná data načtena");
        try {
            lidovky = deserializeLN(serializedLN);
        } catch (FileNotFoundException ex) {
            LOGGER.warn("Serialized file not found. Loading data from xml + K5.");

            lidovky = getDataFromXml();
            String xmlFileName = "lidovky-almost.ser";
            serializeLN(lidovky, xmlFileName);
            LOGGER.info("Data z XML načtena");

            lidovky = getDataFromK5(lidovky); // zároveň kontroluje (podle počtů stran, title a názvů obrázků)
            String allFileName = "lidovky-all.ser";
            serializeLN(lidovky, allFileName);
            LOGGER.info("Data z K5 načtena");
        }
        return lidovky;
    }

    private List<Volume> getDataFromXml() {
        int podvod = 0; // jsou 2 čísla bez title, je potřeba jim dát uuid
        List<Volume> lidovky = new ArrayList<>();

        // načíst ročníky + počty stran z K5 (kvůli zrychlení textově)
        Map<String, String> volumeMap = fillMap();

        // pro všechny xml:
        File sourceFolder = new File("/home/holmanj/Dropbox/MZK/lidovky/hades");
        File[] xmlFileList = sourceFolder.listFiles();
        List<File> xmlFileArray = Arrays.asList(xmlFileList);
        Collections.sort(xmlFileArray);
        for (File xmlFile : xmlFileArray) {
            // načíst data
            Volume volume = new Volume();
            volume.setXmlFileName(xmlFile.getName());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            Document xmlDocument = null;
            try {
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                xmlDocument = dBuilder.parse(xmlFile);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            NodeList volumeDateList = xmlDocument.getElementsByTagName("PeriodicalVolumeDate"); // je jen 1 ročník na xml soubor
            for (int i = 0; i < volumeDateList.getLength(); i++) {
                Node volumeDateNode = volumeDateList.item(i);
                Element volumeDateElement = (Element) volumeDateNode;
                volume.setYear(volumeDateElement.getTextContent());
                volume.setPid(volumeMap.get(volume.getYear()));

                NodeList issueList = xmlDocument.getElementsByTagName("PeriodicalItem");
                for (int j = 0; j < issueList.getLength(); j++) {
                    Node issueNode = issueList.item(j);
                    Element issueElement = (Element) issueNode;
                    Element periodicalItemIdentificationElement = (Element) issueElement.getElementsByTagName("PeriodicalItemIdentification").item(0);
                    Element periodicalItemNumberElement = (Element) periodicalItemIdentificationElement.getElementsByTagName("PeriodicalItemNumber").item(0);
                    Issue issue = new Issue();
                    if (periodicalItemNumberElement != null) {
                        String issueNumber = periodicalItemNumberElement.getTextContent();
                        issue.setTitle(issueNumber);
                        issue.setPid(volumeMap.get(issue.getTitle()));
                    } else {
                        Element periodicalItemDateElement = (Element) periodicalItemIdentificationElement.getElementsByTagName("PeriodicalItemDate").item(0);
                        LOGGER.warn("Číslu ze dne " + periodicalItemDateElement.getTextContent() + " chybí title");
                        switch (podvod) {
                            case 0:
                                issue.setPid(volumeMap.get("uuid:b77cc5c7-435d-11dd-b505-00145e5790ea"));
                                break;
                            case 1:
                                issue.setPid(volumeMap.get("uuid:b77d3b04-435d-11dd-b505-00145e5790ea"));
                                break;
                        }
                        i++;
                    }

                    NodeList pageList = issueNode.getChildNodes();
                    for (int k = 0; k < pageList.getLength(); k++) {
                        Node pageNode = pageList.item(k);
                        if (pageNode.getNodeName().equals("PeriodicalPage")) {
                            Page page = new Page();
                            Element pageElement = (Element) pageNode;
                            Element pageNumberElement = (Element) pageElement.getElementsByTagName("PageNumber").item(0);
                            if (pageNumberElement != null) {
                                page.setTitle(pageNumberElement.getTextContent());
                            } else {
                                Element periodicalItemDateElement = (Element) periodicalItemIdentificationElement.getElementsByTagName("PeriodicalItemDate").item(0);
                                LOGGER.warn("Straně v čísle ze dne " + periodicalItemDateElement.getTextContent() + " chybí title");
                            }
                            Element pageRepresentationElement = (Element) pageElement.getElementsByTagName("PageRepresentation").item(0);
                            Element pageImageElement = (Element) pageRepresentationElement.getElementsByTagName("PageImage").item(0);
                            String imageName = pageImageElement.getAttribute("href");
                            page.setJpgImgName(imageName);
                            issue.getPages().add(page);
                        }
                    }
                    volume.getIssues().add(issue);
                }
            }
            lidovky.add(volume);
        }
        return lidovky;
    }

    private List<Volume> getDataFromK5(List<Volume> lidovky) throws K5ApiException {
//        for (Volume volume : lidovky) {
        for (int k = 23; k < lidovky.size(); k++) {
            Volume volume = lidovky.get(k);

            List<Item> issueItems = k5Api.getChildren(volume.getPid()); // potřeba dělat podle pořadí, title jen na kontrolu
            for (int i = 0; i < issueItems.size(); i++) {
                Issue issue = volume.getIssues().get(i);
                Item issueItem = issueItems.get(i);
                if (issue.getTitle() == null) {
                    // mělo by nastat ve 2 případech v r. 1905
                    LOGGER.warn("Ročník " + issueItem.getPid() + " nemá title");
                } else {
                    if (!issueItem.getDetails().getPartNumber().equals(issue.getTitle())) {
                        // nemělo by nastat nikdy - hodit výjimku?
                        LOGGER.error("Názvy čísel nesedí");
                    }
                }
                issue.setPid(issueItem.getPid());
                List<Item> issueChildren = k5Api.getChildren(issueItem.getPid());
                List<Item> pageItems = new ArrayList<>();
                for (Item item : issueChildren) {
                    if (item.getModel().equals("page")) {
                        pageItems.add(item);
                    }
                }
                // uuid jen stran
//                List<String> pageUuids = fedoraUtils.getChildrenUuids(issue.getPid(), DigitalObjectModel.PAGE);
                if (issue.getPages().size() != pageItems.size()) {
                    // nemělo by nastat nikdy - hodit výjimku?
                    LOGGER.error("Počty stran nesedí");
                }
                for (int j = 0; j < pageItems.size(); j++) {
                    Page page = issue.getPages().get(j);
                    Item pageItem = pageItems.get(j);
                    String itemTitle = pageItem.getDetails().getPagenumber();
                    itemTitle = CharMatcher.WHITESPACE.trimFrom(itemTitle); // trim() nezvládá non-breaking space
                    String pageTitle = page.getTitle();

                    // kontrola podle názvu obrázku (jpg podle xml i djvu z fedory mají mít stejný název)
                    String djvuName = "";
                    try {
                        djvuName = fedoraUtils.getDjVuImgName(pageItem.getPid());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    djvuName = djvuName.split("_")[1]; // odstranění prefixu
                    djvuName = FilenameUtils.removeExtension(djvuName); // odstranění .djvu
                    String jpgName = page.getJpgImgName();
                    jpgName = FilenameUtils.removeExtension(jpgName); // odstranění .jpg
                    if (!jpgName.equals(djvuName)) {
                        // nemělo by nastat nikdy - hodit výjimku?
                        LOGGER.error("Názvy obrázků nesedí!");
                    }

                    // kontrola podle title (v 1 případě chybí)
                    if (pageTitle == null) {
                        // mělo by nastat v 1 případě v r. 1916
                        LOGGER.warn("Strana " + pageItem.getPid() + " nemá title");
                        page.setPid(pageItem.getPid());
                    } else {
                        if (!pageTitle.equals(itemTitle)) {
                            // nemělo by nastat nikdy - hodit výjimku?
                            LOGGER.error("Názvy stran nesedí");
                        } else {
                            page.setPid(pageItem.getPid());
                        }
                    }
                }

                if (issue.getPid() == null) {
                    // nemělo by nastat nikdy - hodit výjimku?
                    LOGGER.warn("Title čísla " + issue.getTitle() + " nenalezen, ročník " + volume.getYear());
                }
            }
            String almostFileName = "lidovky-almost.ser";
            serializeLN(lidovky, almostFileName);
            LOGGER.info("Ročník " + volume.getYear() + " je načtený.");
        }

        // r. 1916 (index 23) issue 185 (index 453), strana 3 chybí; strana 4 proto nemá naštené uuid (neseděl počet)
        lidovky = oprava(lidovky);

        return lidovky;
    }

    private List<Volume> oprava(List<Volume> lidovky) {
        lidovky.get(23).getIssues().get(453).getPages().get(3).setPid("uuid:26ead9d0-61e6-11dc-8e7a-000d606f5dc6");
        lidovky.get(23).getIssues().get(453).getPages().remove(2);
        return lidovky;
    }

    public Map<String, String> fillMap() {
        Map<String, String> volumeMap = new HashMap<>();
        volumeMap.put("1893", "uuid:b231a40c-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1894", "uuid:b235e9d1-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1895", "uuid:b23610e2-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1896", "uuid:b231f22d-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1897", "uuid:b23610e3-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1898", "uuid:b2368614-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1899", "uuid:b236d435-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1900", "uuid:b2379786-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1901", "uuid:b237be97-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1902", "uuid:b237be98-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1903", "uuid:b23881e9-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1904", "uuid:b232dc8e-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1905", "uuid:b235267f-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1906", "uuid:b2354d90-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1907", "uuid:b23881ea-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1908", "uuid:b239ba6b-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1909", "uuid:b23a56ac-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1910", "uuid:b23a56ad-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1911", "uuid:b23b410e-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1912", "uuid:b23c798f-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1913", "uuid:b23c7990-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1914", "uuid:b23d3ce1-435d-11dd-b505-00145e5790ea");
        volumeMap.put("1915", "uuid:25746670-61e6-11dc-9400-000d606f5dc6");
        volumeMap.put("1916", "uuid:2607f3e0-61e6-11dc-9c6f-000d606f5dc6");
        volumeMap.put("1917", "uuid:de680b60-5a01-11dc-8b19-0013e6840575");
        volumeMap.put("1918", "uuid:25231220-61e6-11dc-90a3-000d606f5dc6");
        volumeMap.put("1919", "uuid:25473bf0-61e6-11dc-a2fd-000d606f5dc6");
        volumeMap.put("1920", "uuid:25a64be0-61e6-11dc-9a0d-000d606f5dc6");
        volumeMap.put("1921", "uuid:269bf680-61e6-11dc-b537-000d606f5dc6");
        volumeMap.put("1922", "uuid:5116bfb0-7e49-11dc-8610-000d606f5dc6");
        volumeMap.put("1923", "uuid:2b59fd30-8337-11dc-85bf-000d606f5dc6");
        volumeMap.put("1924", "uuid:b5c32f80-824e-11dc-b798-000d606f5dc6");
        volumeMap.put("1925", "uuid:dde30480-82e0-11dc-ae84-000d606f5dc6");
        volumeMap.put("1926", "uuid:4a1e1890-86f8-11dc-ba73-000d606f5dc6");
        volumeMap.put("1927", "uuid:0acf0f10-86ca-11dc-a462-000d606f5dc6");
        volumeMap.put("1928", "uuid:64f2e670-884a-11dc-ba7c-000d606f5dc6");
        volumeMap.put("1929", "uuid:07f80b90-871d-11dc-8cb1-000d606f5dc6");
        volumeMap.put("1930", "uuid:9a205bd0-8b87-11dc-91b8-000d606f5dc6");
        volumeMap.put("1931", "uuid:a3773110-acc3-11dc-8070-000d606f5dc6");
        volumeMap.put("1932", "uuid:332fe8d0-a2f4-11dc-bb00-000d606f5dc6");
        return volumeMap;
    }

    private void serializeLN(List<Volume> lidovky, String filename) {
        FileOutputStream fileOut = null;
        ObjectOutputStream out = null;
        try {
            fileOut = new FileOutputStream(filename);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(lidovky);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Volume> deserializeLN(String filename) throws FileNotFoundException {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        List<Volume> lidovky = null;
        try {
            fileIn = new FileInputStream(filename);
            in = new ObjectInputStream(fileIn);
            lidovky = (List<Volume>) in.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new FileNotFoundException();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                fileIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lidovky;
    }

    @Override
    public String getUsage() {
        return "přesun jpg Lidovek z Hada na imageserver \n" +
                "(jednorázový skript)";
    }
}
