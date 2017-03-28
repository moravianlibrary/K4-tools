/*
package cz.mzk.k4.tools.scripts.lidovky;

import com.google.common.base.CharMatcher;
import cz.mzk.k4.tools.domain.Issue;
import cz.mzk.k4.tools.domain.Page;
import cz.mzk.k4.tools.domain.Volume;
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
import org.apache.logging.log4j.Logger;
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

*/
/**
 * Created by holmanj on 20.3.15.
 *//*

public class LnKonverze implements Script {
    public static final Logger LOGGER = Logger.getLogger(LnKonverze.class);
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);

    @Override
    public void run(List<String> args) {

        // odkomentovat kód podle fáze (1. fáze odkomentovaná, pak buď 2. nebo 3. - radši postupně)

        // fáze 1: načíst a serializovat data z K5 (děje se vždycky)

        List<Volume> lidovky = null;
        String serializedDataName = "lidovky-converted.ser";
        if (new File(serializedDataName).exists()) {
            try {
                lidovky = deserializeLN(serializedDataName);
            } catch (FileNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            }
        } else {
            LOGGER.warn("Serialized file not found. Loading data from  K5.");
            lidovky = fillSecondStage();
            try {
                lidovky = getDataFromK5(lidovky);
            } catch (K5ApiException e) {
                e.printStackTrace();
            }
            serializeLN(lidovky, serializedDataName);
        }
        System.out.println("Data načtena");

        // fáze 2: přesun do imageserveru (případně konverze do jp2)
        // nově: stahovat obrázky z fedory, konvertovat, ukládat do imageserveru
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

        // fáze 4: čištění starých djvu - není potřeba, fedora maže nepotřebné datastreamy sama

    }

    private void addDatastreams(List<Volume> lidovky) throws CreateObjectException, TransformerException, IOException {
        for (Volume volume : lidovky) {
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
        // sshfs root@editor.staff.mzk.cz:/mnt/imageserver/ /mnt/imageserver -o follow_symlinks
        for (Volume volume : lidovky) {
            LOGGER.info("Converting " + getPageNumber(volume) + " pages of volume " + volume.getYear());
            for (Issue issue : volume.getIssues()) {
                for (Page page : issue.getPages()) {
                    String djvuName = page.getDjvuImgName();
                    if ("".equals(djvuName) || djvuName == null) {
                        djvuName = fedoraUtils.getDjVuImgName(page.getPid());
                        page.setDjvuImgName(djvuName); // name
                    }
                    String imageserverPath = "/mnt/imageserver/mzk01/000/244/261/" + volume.getYear() + "/" + FilenameUtils.removeExtension(page.getDjvuImgName()) + ".jp2";
                    page.setImageserverImgLocation("http://imageserver.mzk.cz/mzk01/000/244/261/" + volume.getYear() + "/" + FilenameUtils.removeExtension(page.getDjvuImgName()));
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
                        InputStream jp2Stream = FormatConvertor.convertDjvuToJp2(tempDjvuImage);
                        FileUtils.copyInputStreamToFile(jp2Stream, imageserverFile);
                        tempDjvuImage.delete();
                    } catch (IOException e) {
                        LOGGER.error("Kopírování obrázku z " + djvuName + " do " + imageserverPath + " selhalo.");
                        continue;
                    }
                }
            }
            LOGGER.info("Obrázky z ročníku " + volume.getYear() + " jsou zkonvertovány.");
            serializeLN(lidovky, "lidovky-converted.ser");
        }
        LOGGER.info("Obrázky jsou přesunuty");
    }

    private int getPageNumber(Volume volume) {
        int pageNumber = 0;
        for (Issue issue : volume.getIssues()) {
            pageNumber += issue.getPages().size();
        }
        return pageNumber;
    }

    private List<Volume> getDataFromK5(List<Volume> lidovky) throws K5ApiException {
//        for (Volume volume : lidovky) {
        for (int k = 0; k < lidovky.size(); k++) {
            Volume volume = lidovky.get(k);

            List<Item> issueItems = k5Api.getChildren(volume.getPid()); // potřeba dělat podle pořadí, title jen na kontrolu
            for (int i = 0; i < issueItems.size(); i++) {
                Issue issue = new Issue();
                Item issueItem = issueItems.get(i);
                issue.setTitle(issueItem.getDetails().getPartNumber());
                issue.setPid(issueItem.getPid());
                List<Item> issueChildren = k5Api.getChildren(issueItem.getPid());
                List<Item> pageItems = new ArrayList<>();
                // uuid jen stran, v pořadí (možná tady už pořadí není potřeba a z fedory by to šlo rychlejc
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
            String almostFileName = "lidovky-almost.ser";
            serializeLN(lidovky, almostFileName);
            LOGGER.info("Ročník " + volume.getYear() + " je načtený.");
        }

        return lidovky;
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

    private List<Volume> fillSecondStage() {
        List<Volume> lidovky = new ArrayList<>();
        // u LN se konvertovaly jen ročníky 1933-1945, ne celé periodikum -> takhle se načítá jen potřebná část
//        lidovky.add(new Volume("1933", "uuid:5c39cf90-dfa6-11dc-9c02-000d606f5dc6"));
//        lidovky.add(new Volume("1934", "uuid:e0dfa5f0-0fba-11dd-9f3d-000d606f5dc6"));
//        lidovky.add(new Volume("1935", "uuid:3024eee0-1baa-11dd-895a-000d606f5dc6"));
//        lidovky.add(new Volume("1936", "uuid:8ae68730-25cd-11dd-8afc-000d606f5dc6"));
//        lidovky.add(new Volume("1937", "uuid:2e856c90-2bf9-11dd-aaa1-000d606f5dc6"));
//        lidovky.add(new Volume("1938", "uuid:926698b0-37f7-11dd-8e35-000d606f5dc6"));
//        lidovky.add(new Volume("1939", "uuid:23e87270-4ebb-11dd-a91e-000d606f5dc6"));
//        lidovky.add(new Volume("1940", "uuid:e62aac60-5323-11dd-9471-000d606f5dc6"));
//        lidovky.add(new Volume("1941", "uuid:3ad7f270-484f-11dd-b012-000d606f5dc6"));
//        lidovky.add(new Volume("1942", "uuid:0226fbd0-6eb8-11dd-9de7-000d606f5dc6"));
//        lidovky.add(new Volume("1943", "uuid:047b0c30-6b0e-11dd-b545-000d606f5dc6"));
//        lidovky.add(new Volume("1944", "uuid:2ec8bbf0-63c4-11dd-afa8-000d606f5dc6"));
//        lidovky.add(new Volume("1945", "uuid:04281a60-6330-11dd-ab5f-000d606f5dc6"));

        lidovky.add(new Volume("1948", "uuid:182d2130-783a-11dd-88df-000d606f5dc6"));
        lidovky.add(new Volume("1949", "uuid:f9dedcb0-7d79-11dd-9bfe-000d606f5dc6"));
        lidovky.add(new Volume("1950", "uuid:e512bd10-7eaf-11dd-8fcc-000d606f5dc6"));
        lidovky.add(new Volume("1951", "uuid:64f672b0-7e4c-11dd-b89d-000d606f5dc6"));
        lidovky.add(new Volume("1952", "uuid:30b93080-7f7f-11dd-b82a-000d606f5dc6"));

        return lidovky;
    }

    @Override
    public String getUsage() {
        return "přesun jpg Lidovek z Hada na imageserver \n" +
                "(jednorázový skript)";
    }
}
*/
