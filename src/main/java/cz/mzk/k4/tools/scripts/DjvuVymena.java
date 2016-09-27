package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.exceptions.ControlCheckException;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jan on 25.3.16.
 */
public class DjvuVymena implements Script {
    private static final Logger LOGGER = Logger.getLogger(ChangeImgs.class);
    private AccessProvider accessProvider = AccessProvider.getInstance();
    private FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);
    private ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    private ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    private ImageUrlWorker prepisovakUrl;

    boolean isMonograph = true;
    String imageserverFolderPath = "mzk01/000/206/586";
    String topUuid = "uuid:ae876087-435d-11dd-b505-00145e5790ea";
    String subFolderName = "";
    String year = "";

    public DjvuVymena() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        // sshfs holmanj@editor.staff.mzk.cz:/mnt/imageserver/ /mnt/imageserver -o follow_symlinks

        DigitalObjectModel topModel = null;
        boolean writeEnabled = args.contains("writeEnabled");
        try {
            topModel = fedoraUtils.getModel(topUuid);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        if (topModel.equals(DigitalObjectModel.MONOGRAPH)) {
            try {
                swapImagesMonograph(topUuid, writeEnabled);
            } catch (TransformerException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            } catch (CreateObjectException e) {
                LOGGER.error(e.getMessage());
            } catch (ControlCheckException e) {
                LOGGER.error(e.getMessage());
            }
        } else if (topModel.equals(DigitalObjectModel.PERIODICAL)) {
            try {
                List<Item> volumes = clientApi.getChildren(topUuid);
                Map<String, File> yearSubfolderMap = fillSubfolderMap();
                for (Item volume : volumes) {
                    year = volume.getDetails().getYear();
                    File subfolder = yearSubfolderMap.get(year);
                    subFolderName = subfolder.getName();
//                    if (year.equals("1898 - 1899")) {
                        swapImagesVolume(volume.getPid(), subfolder, writeEnabled);
//                    }
                }
            } catch (K5ApiException e) {
                LOGGER.error(e.getMessage());
            } catch (ParserConfigurationException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            } catch (SAXException e) {
                LOGGER.error(e.getMessage());
            } catch (ControlCheckException e) {
                LOGGER.error(e.getMessage());
            } catch (TransformerException e) {
                LOGGER.error(e.getMessage());
            } catch (CreateObjectException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private void swapImagesVolume(String volumeUuid, File volumeImgSubfolder, boolean writeEnabled) throws IOException, SAXException, ParserConfigurationException, ControlCheckException, CreateObjectException, TransformerException {
        List<Item> pages = new ArrayList<>(); // složka může obsahovat i OCR - ignorovat bez ALTO?
        List<File> files;
        List<String> filesFromXml;
        String indexFileName = volumeImgSubfolder.getName() + ".xml";
        Map<String, String> checkMap = fillTitleImgMap(volumeImgSubfolder.getAbsolutePath() + "/" + indexFileName);
        try {
            List<Item> children = clientApi.getChildren(volumeUuid); // get list of pages
            // nejdřív stránky přímo pod ročníkem
            pages.addAll(children.stream().filter(child -> child.getModel().equals("page")).collect(Collectors.toList()));
            for (Item child : children) {
                if (child.getModel().equals("periodicalitem")) {
                    // potom stránky v číslech
                    pages.addAll(clientApi.getChildren(child.getPid()));
                }
            }
        } catch (K5ApiException e) {
            LOGGER.error(e.getMessage());
        }
        files = getFolderContentList(imageserverFolderPath + "/" + volumeImgSubfolder.getName(), ".jp2"); // get list of files
        filesFromXml = loadFilesFromXml(volumeImgSubfolder.getAbsolutePath() + "/" + indexFileName);
//        swapImages(pages, files, writeEnabled, checkMap);
        swapImages(pages, files, writeEnabled, checkMap, filesFromXml);
    }

    private List<String> loadFilesFromXml(String indexFilePath) throws ParserConfigurationException, IOException, SAXException {
        List<String> fileList = new ArrayList<>();
        File fXmlFile = new File(indexFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        // varování: i ročník může mít strany (obsah, titulní strana ročníku,..)
        NodeList fileNames = doc.getElementsByTagName("PageImage");
        for (int i = 0; i < fileNames.getLength(); i++) {
            fileList.add(fileNames.item(i).getAttributes().getNamedItem("href").getTextContent());
        }
        return fileList;
    }

    private void swapImagesMonograph(String topUuid, boolean writeEnabled) throws TransformerException, IOException, CreateObjectException, ControlCheckException {
        List<Item> pages = null;
        List<File> files = null;
        try {
            pages = clientApi.getChildren(topUuid); // get list of pages
            files = getFolderContentList(imageserverFolderPath); // get list of files
        } catch (K5ApiException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getMessage());
        }
        swapImages(pages, files, writeEnabled);
    }

    private void swapImages(List<Item> pages, List<File> files, boolean writeEnabled) throws TransformerException, IOException, CreateObjectException, ControlCheckException {
        swapImages(pages, files, writeEnabled, null);
    }

    // pro obrázky z budějovic, načtení souborů ze složky může mít jiné pořadí než stránky v K5 / xml souboru
    private void swapImages(List<Item> pages, List<File> files, boolean writeEnabled, Map<String, String> checkMap, List<String> filesFromXml) throws IOException, CreateObjectException, TransformerException, ControlCheckException {
        Path pairsFile = Paths.get("IO/page-image-list");

        if (pages.size() != files.size()) {
            // compare sizes
            throw new ControlCheckException("Počty stran v ročníku " + year + " a obrázků v " + imageserverFolderPath + "/" + subFolderName + " nesedí.");
        }

        if (!writeEnabled) {
            for (int i = 0; i < pages.size(); i++) {
                if (checkMap != null) {
                    Item page = pages.get(i);
                    String k5Title = page.getDetails().getPagenumber();
                    k5Title = k5Title.replace('\u00A0', ' ').trim(); // trim including non-breaking space
                    String checkTitle = checkMap.get(filesFromXml.get(i));
                    if (!k5Title.equals(checkTitle)) {
                        LOGGER.warn("Page titles do not match (" + page.getPid() + ": " + k5Title + " vs. " + checkTitle + ") - year "
                                + year);
                    }
                }
                String pair = pages.get(i).getPid() + " - " + filesFromXml.get(i) + "\n";
                Files.write(pairsFile, pair.getBytes(), StandardOpenOption.APPEND);
            }
        } else {
            swapImages(pages, files, writeEnabled, checkMap);
        }
    }

    private void swapImages(List<Item> pages, List<File> files, boolean writeEnabled, Map<String, String> checkMap) throws IOException, CreateObjectException, TransformerException, ControlCheckException {
        Path pairsFile = Paths.get("IO/page-image-list"); // TODO: připojit root uuid, vytvořit nový soubor

        if (pages.size() != files.size()) {
            // compare sizes
            throw new ControlCheckException("Počty stran v ročníku " + year + " a obrázků v " + imageserverFolderPath + "/" + subFolderName + " nesedí.");
        }

        if (!writeEnabled) {
            for (int i = 0; i < pages.size(); i++) {
                if (checkMap != null) {
                    Item page = pages.get(i);
//                    String k5Title = page.getPageTitle();
                    String k5Title = page.getDetails().getPagenumber();
                    k5Title = k5Title.replace('\u00A0', ' ').trim(); // trim including non-breaking space
                    String checkTitle = checkMap.get(files.get(i).getName());
                    if (!k5Title.equals(checkTitle)) {
                        LOGGER.warn("Page titles do not match (" + page.getPid() + ": " + k5Title + " vs. " + checkTitle + ") - year "
                                + year);
                    }
                }
                String pair = pages.get(i).getPid() + " - " + files.get(i) + "\n";
                Files.write(pairsFile, pair.getBytes(), StandardOpenOption.APPEND);
            }
        } else {
            for (int i = 0; i < pages.size(); i++) {
                LOGGER.debug("Výměna obrázků u strany " + i + " z " + pages.size());
                String fileLocation = "http://imageserver.mzk.cz/" + imageserverFolderPath + "/" + subFolderName + "/" + files.get(i).getName().replace(".jp2", "");
                Item page = pages.get(i);
                fedoraUtils.setImgFullFromExternal(page.getPid(), fileLocation);

                // datastreamy
                fedoraUtils.setImgFullFromExternal(page.getPid(), fileLocation + "/big.jpg");
                fedoraUtils.setImgPreviewFromExternal(page.getPid(), fileLocation + "/preview.jpg");
                fedoraUtils.setImgThumbnailFromExternal(page.getPid(), fileLocation + "/thumb.jpg");

                // vazba na dlaždice
//                changeRelsExt(page.getPid(), fileLocation);
                fedoraUtils.repairImageserverTilesRelation(page.getPid());
            }
            LOGGER.info("Odkazy na obrázky ročníku " + year + " byly doplněny do fedory.");
        }
    }

    private Map<String, File> fillSubfolderMap() {
        // různé názvy složek - buď přejmenovat na imageserveru na rok, nebo naplnit mapu

        Map<String, String> folderYearMap = getFolderYearMap(); // pro Rudé právo


        Map<String, File> map = new HashMap<>();
        List<File> files = getFolderContentList(imageserverFolderPath);
        for (File file : files) {
            if (file.isDirectory()) {
                // TODO: upravit podle potřeby
                String folderName = file.getName();
                String year = folderYearMap.get(folderName);
                map.put(year, file);
            }
        }
        return map;
    }

    // mapa page title - img filename (pro kontrolu s title z krameria)
    // někdy je u ročníku XML s párováním (přejmenovat na index.xml)
    private Map<String, String> fillTitleImgMap(String indexFileName) throws ParserConfigurationException, IOException, SAXException {
        Map<String, String> map = new HashMap<>();
        File fXmlFile = new File(indexFileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        // varování: i ročník může mít strany (obsah, titulní strana ročníku,..)
        NodeList pages = doc.getElementsByTagName("PeriodicalPage");
        for (int i = 0; i < pages.getLength(); i++) {
            Element page = (Element) pages.item(i);
            String pageNumber = page.getElementsByTagName("PageNumber").item(0).getTextContent();
            String imgFilename = page.getElementsByTagName("PageImage").item(0).getAttributes().getNamedItem("href").getTextContent();
            map.put(imgFilename, pageNumber);
        }
        return map;
    }

    private List<File> getFolderContentList(String imageserverFolderPath) {
        return getFolderContentList(imageserverFolderPath, null);
    }

    private List<File> getFolderContentList(String imageserverFolderPath, String extension) {
        File imageserverFolder = new File("/mnt/imageserver/" + imageserverFolderPath);
        File[] fileList;
        if (extension == null) {
            // get all files
            fileList = imageserverFolder.listFiles();
        } else {
            // get files with specific extension
            fileList = imageserverFolder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(extension);
                }
            });
        }
        Arrays.sort(fileList);
        return new ArrayList(Arrays.asList(fileList));
    }

    @Override
    public String getUsage() {
        return "Náhrada djvu za odkazy na existující JPEG2000 na imageserveru";
    }

    public Map<String, String> getFolderYearMap() {
        // speciálně pro Rudé právo (složky s obrázky nejsou pojmenované podle roku)
        Map<String, String> yearSubfolderMap = new HashMap<>();
        yearSubfolderMap.put("21690", "1860");
        yearSubfolderMap.put("18330", "1858");
        yearSubfolderMap.put("21691", "1861");
        return yearSubfolderMap;
    }
}
