package cz.mzk.k4.tools.scripts;

import com.google.common.base.CharMatcher;
import cz.mzk.k4.tools.domain.Issue;
import cz.mzk.k4.tools.domain.Page;
import cz.mzk.k4.tools.domain.Volume;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.FormatConvertor;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.common.K5ApiException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
import java.util.List;

/**
 * Created by holmanj on 14.12.15.
 */
public class DjvuKonverze implements Script {
    public static final Logger LOGGER = Logger.getLogger(DjvuKonverze.class);
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);

    // TODO: měnit podle potřeby (dohledat sysno)
    private static final String IMAGESERVER_PATH = "mzk01/000/163/400/";

    public DjvuKonverze() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        String rootUuid = args.get(0);
        String title = null;
        try {
            title = k5Api.getItem(rootUuid).getRoot_title();
        } catch (K5ApiException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("Spuštěna konverze z djvu (nebo jpg) do jp2 na periodiku " + rootUuid + " " + title);
        // odkomentovat kód podle fáze (1. fáze odkomentovaná, pak buď 2. nebo 3. - radši postupně)

        // fáze 1: načíst a serializovat data z K5 (děje se vždycky)
        List<Volume> periodikum = null;
        String serializedDataName = "IO/" + rootUuid + ".ser";
        if (new File(serializedDataName).exists()) {
            try {
                periodikum = deserialize(serializedDataName);
            } catch (FileNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            }
        } else {
            LOGGER.warn("Serialized file not found. Loading data from  K5.");
            periodikum = new ArrayList<>();
            try {
                periodikum = getDataFromK5(periodikum, rootUuid);
            } catch (K5ApiException e) {
                e.printStackTrace();
            }
            serialize(periodikum, serializedDataName);
        }
        System.out.println("Data načtena");

        // fáze 2: přesun do imageserveru (případně konverze do jp2)
        // nově: stahovat obrázky z fedory, konvertovat, ukládat do imageserveru
//        try {
//            copyImages(periodikum, serializedDataName);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // fáze 3: doplnění vazeb do fedory
//        try {
//            addDatastreams(periodikum);
//        } catch (CreateObjectException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (TransformerException e) {
//            e.printStackTrace();
//        }

        // fáze 4: čištění starých djvu - není potřeba, fedora maže nepotřebné datastreamy sama
    }

    private void addDatastreams(List<Volume> periodikum) throws CreateObjectException, TransformerException, IOException {
        List<String> toSkip = skipVolumes();
        for (Volume volume : periodikum) {
            if (toSkip.contains(volume.getPid())) { // # TODO change
                LOGGER.debug("Skipping volume " + volume.getYear() + " " + volume.getPid());
                continue;
            }
            for (Issue issue : volume.getIssues()) {
                for (Page page : issue.getPages()) {
                    page.setImageserverImgLocation(page.getImageserverImgLocation().replaceAll(" ", ""));
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

    private void copyImages(List<Volume> periodikum, String serializedDataName) throws IOException {
        // sshfs root@editor.staff.mzk.cz:/mnt/imageserver/ /mnt/imageserver -o follow_symlinks
//        List<String> toSkip = skipVolumes();
        for (Volume volume : periodikum) {
//            if (!toSkip.contains(volume.getPid())) { // # TODO change
//                LOGGER.debug("Skipping volume " + volume.getYear() + " " + volume.getPid());
//                continue;
//            }
            LOGGER.info("Converting " + getTotalPageNumber(volume) + " pages of volume " + volume.getYear());
            for (Issue issue : volume.getIssues()) {
                for (Page page : issue.getPages()) {
                    String djvuName = page.getDjvuImgName();
                    if ("".equals(djvuName) || djvuName == null) {
                        djvuName = fedoraUtils.getDjVuImgName(page.getPid());
                        page.setDjvuImgName(djvuName); // name
                    }
                    String imageserverPath = "/mnt/imageserver/" + IMAGESERVER_PATH + volume.getYear() + "/" + FilenameUtils.removeExtension(page.getDjvuImgName()) + ".jp2";
                    page.setImageserverImgLocation("http://imageserver.mzk.cz/" + IMAGESERVER_PATH + volume.getYear() + "/" + FilenameUtils.removeExtension(page.getDjvuImgName()));
                    File tempDjvuImage = new File(page.getDjvuImgName());
                    try {
                        FileUtils.copyInputStreamToFile(fedoraUtils.getImgFull(page.getPid(), "image/djvu"), tempDjvuImage);
                    } catch (FileNotFoundException e) {
                        LOGGER.error(e.getMessage());
                    }

                    File imageserverFile = new File(imageserverPath);

                    // copy file to imageserver
                    if (imageserverFile.exists() && imageserverFile.length() != 0) {
                        LOGGER.warn("Obrázek " + page.getPid() + " " + page.getDjvuImgName() + " už je na imageserveru, nepokopírovat");
                        tempDjvuImage.delete();
                        continue;
                    }

                    try {
                        InputStream jp2Stream = null;
                        if (tempDjvuImage.getName().contains("jpg")) {
                            LOGGER.debug("Converting page " + page.getPid() + ". Input format: JPG (" + tempDjvuImage.getName() + ")");
                            jp2Stream = FormatConvertor.convertJpgToJp2(tempDjvuImage);
                        } else {
                            LOGGER.debug("Converting page " + page.getPid() + ". Input format: DjVu (" + tempDjvuImage.getName() + ")");
                            jp2Stream = FormatConvertor.convertDjvuToJp2(tempDjvuImage);
                        }
                        FileUtils.copyInputStreamToFile(jp2Stream, imageserverFile);
                        tempDjvuImage.delete();
                    } catch (IOException e) {
                        LOGGER.error("Kopírování obrázku z " + djvuName + " do " + imageserverPath + " selhalo.");
                        continue;
                    }
                }
            }
            LOGGER.info("Obrázky z ročníku " + volume.getYear() + " jsou zkonvertovány.");
            serialize(periodikum, "IO/temp-periodikum-converted.ser");
        }
        serialize(periodikum, serializedDataName);
        LOGGER.info("Obrázky jsou přesunuty");
    }

    private List<String> skipVolumes() {
        List<String> toSkip = new ArrayList<>();
        toSkip.add("uuid:afe0fd7d-435d-11dd-b505-00145e5790ea");
        toSkip.add("uuid:afe172ae-435d-11dd-b505-00145e5790ea");
        toSkip.add("uuid:b0746363-435d-11dd-b505-00145e5790ea");
        toSkip.add("uuid:b074b184-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b0754dc5-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b0754dc6-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:afbc0ff9-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:afbdbdaa-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:afbe0bcb-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b013a5d0-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b0149031-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b0152c72-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b0152c74-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b0163de5-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:aff76be6-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:aff8cb77-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:aff940a8-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:aff9b5d9-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:aff9dcea-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:affa03fb-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b068ca8b-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b06966cc-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b069dbfd-435d-11dd-b505-00145e5790ea");
//        toSkip.add("uuid:b069dbfe-435d-11dd-b505-00145e5790ea");
        return toSkip;
    }

    private int getTotalPageNumber(Volume volume) {
        int pageNumber = 0;
        for (Issue issue : volume.getIssues()) {
            pageNumber += issue.getPages().size();
        }
        return pageNumber;
    }

    private List<Volume> getDataFromK5(List<Volume> periodikum, String rootUuid) throws K5ApiException {
        List<Item> volumes = k5Api.getChildren(rootUuid);
        for (Item volume : volumes) {
            periodikum.add(new Volume(volume.getDetails().getYear(), volume.getPid()));
        }
        // buď tak, nebo ručně povkládat seznam ročník - uuid (v případě velkého periodika, kde je jen pár djvu ročníků):
//        periodikum.add(new Volume("1933", "uuid:5c39cf90-dfa6-11dc-9c02-000d606f5dc6"));

        for (Volume volume : periodikum) {
            List<Item> issueItems = k5Api.getChildren(volume.getPid()); // k5api kvůli pořadí
            for (int i = 0; i < issueItems.size(); i++) {
                Issue issue = new Issue();
                Item issueItem = issueItems.get(i);
                issue.setTitle(issueItem.getDetails().getPartNumber());
                issue.setPid(issueItem.getPid());
                List<Item> issueChildren = k5Api.getChildren(issueItem.getPid());
                List<Item> pageItems = new ArrayList<>();
                // uuid jen stran
                for (Item item : issueChildren) {
                    if (item.getModel().equals("page")) {
                        pageItems.add(item);
                    }
                }

                for (int j = 0; j < pageItems.size(); j++) {
                    Page page = new Page();
                    Item pageItem = pageItems.get(j);
                    String itemTitle = pageItem.getDetails().getPagenumber();
                    itemTitle = CharMatcher.WHITESPACE.trimFrom(itemTitle); // trim() nezvládá non-breaking space
                    page.setTitle(itemTitle);
                    page.setPid(pageItem.getPid());
                    issue.getPages().add(page);
                }
                volume.getIssues().add(issue);
                if (issue.getPid() == null) {
                    // nemělo by nastat nikdy - hodit výjimku?
                    LOGGER.warn("Title čísla " + issue.getTitle() + " nenalezen, ročník " + volume.getYear());
                }
            }
            String almostFileName = "periodikum-almost.ser";
            serialize(periodikum, almostFileName);
            LOGGER.info("Ročník " + volume.getYear() + " je načtený.");
        }

        return periodikum;
    }

    private void serialize(List<Volume> periodikum, String filename) {
        FileOutputStream fileOut = null;
        ObjectOutputStream out = null;
        try {
            fileOut = new FileOutputStream(filename);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(periodikum);
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

    private List<Volume> deserialize(String filename) throws FileNotFoundException {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        List<Volume> periodikum = null;
        try {
            fileIn = new FileInputStream(filename);
            in = new ObjectInputStream(fileIn);
            periodikum = (List<Volume>) in.readObject();
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
        return periodikum;
    }

    @Override
    public String getUsage() {
        return "Konverze stran periodik z djvu do jp2 a přesun na imageserver";
    }
}
