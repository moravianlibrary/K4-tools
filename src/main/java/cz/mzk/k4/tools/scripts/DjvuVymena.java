package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.exceptions.ControlCheckException;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.workers.ImageUrlWorker;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jan on 25.3.16.
 */
public class DjvuVymena implements Script {
    private static final Logger LOGGER = Logger.getLogger(ChangeImgs.class);
    private static final AccessProvider accessProvider = AccessProvider.getInstance();
    private static FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);
    private static final ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    private static final ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    private ImageUrlWorker prepisovakUrl;

    boolean isMonograph = true;
    String imageserverFolderPath = "mzk01/000/936/117";
    String topUuid = "uuid:6d962368-9ce3-11e0-9ad4-0050569d679d";

    @Override
    public void run(List<String> args) {
        // sshfs holmanj@editor.staff.mzk.cz:/mnt/imageserver/ /mnt/imageserver -o follow_symlinks
        // nejdřív pro monografii
        List<Item> pages = null;
        List<File> files = null;
        Path pairsFile = Paths.get("IO/page-image-list"); // TODO: připojit root uuid, vytvořit nový soubor
        try {
            pages = clientApi.getChildren(topUuid); // get list of pages
            files = getListOfImages(imageserverFolderPath); // get list of files
            if (pages.size() != files.size()) {
                // compare sizes
                throw new ControlCheckException("Počty stran v dokumentu " + topUuid + " a obrázků v " + imageserverFolderPath + " nesedí.");
            }

            if (!args.contains("writeEnabled")) {
                for (int i = 0; i < pages.size(); i++) {
                    String pair = pages.get(i).getPid() + " - " + files.get(i) + "\n";
                    Files.write(pairsFile, pair.getBytes(), StandardOpenOption.APPEND);
                }
            } else {
                for (int i = 0; i < pages.size(); i++) {
                    LOGGER.info("Výměna obrázků u strany " + i + " z " + pages.size());
                    String fileLocation = "http://imageserver.mzk.cz/" + imageserverFolderPath + "/" + files.get(i).getName().replace(".jp2", "");
                    Item page = pages.get(i);
                    fedoraUtils.setImgFullFromExternal(page.getPid(), fileLocation);

                    // datastreamy
                    fedoraUtils.setImgFullFromExternal(page.getPid(), fileLocation + "/big.jpg");
                    fedoraUtils.setImgPreviewFromExternal(page.getPid(), fileLocation + "/preview.jpg");
                    fedoraUtils.setImgThumbnailFromExternal(page.getPid(), fileLocation + "/thumb.jpg");

                    // vazba na dlaždice
                    changeRelsExt(page.getPid(), fileLocation);
                    fedoraUtils.repairImageserverTilesRelation(page.getPid());
                }
                LOGGER.info("Odkaz na obrázky dokumentu " + topUuid + " byly doplněny do fedory.");
            }
        } catch (K5ApiException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        } catch (ControlCheckException e) {
            LOGGER.error(e.getMessage());
        } catch (TransformerException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        } catch (CreateObjectException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
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

    private List<File> getListOfImages(String imageserverFolderPath) {
        File imageserverFolder = new File("/mnt/imageserver/" + imageserverFolderPath);
        File[] fileList = imageserverFolder.listFiles();
        Arrays.sort(fileList);
        return new ArrayList(Arrays.asList(fileList));
    }

    @Override
    public String getUsage() {
        return "Náhrada djvu za odkazy na existující JPEG2000 na imageserveru";
    }
}
