package cz.mzk.k4.tools.scripts;

import com.google.common.base.CharMatcher;
import cz.mzk.k4.tools.domain.Page;
import cz.mzk.k4.tools.domain.Volume;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.FormatConvertor;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.common.K5ApiException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.xml.transform.TransformerException;
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
 * Created by secikd on 1/27/17.
 * Similar to DjvuKonverze but splits periodical document to volumes and allow to skip selected ones
 */
public class DjvuKonverzePeriodika implements Script {
    public static final Logger LOGGER = LogManager.getLogger(DjvuKonverzePeriodika.class);
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);

    // TODO: měnit podle potřeby (dohledat sysno) musi  koncit lomitkom !
    private static final String IMAGESERVER_PATH = "mzk01/000/278/602/";

    public DjvuKonverzePeriodika() throws FileNotFoundException {
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
        LOGGER.info("Spuštěna konverze z djvu (nebo jpg) do jp2 na " + rootUuid + " " + title);
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
            if (toSkip.contains(volume.getPid())) {
                LOGGER.info("Skipping volume " + volume.getYear() + " " + volume.getPid());
                continue;
            }

            for (Page page : volume.getPages()) {
                page.setImageserverImgLocation(page.getImageserverImgLocation().replaceAll(" ", ""));
                // datastreamy
                fedoraUtils.setImgFullFromExternal(page.getPid(), page.getImageserverImgLocation() + "/big.jpg");
                fedoraUtils.setImgPreviewFromExternal(page.getPid(), page.getImageserverImgLocation() + "/preview.jpg");
                fedoraUtils.setImgThumbnailFromExternal(page.getPid(), page.getImageserverImgLocation() + "/thumb.jpg");

                //vazba na dlaždice
                fedoraUtils.repairImageserverTilesRelation(page.getPid());
            }
            LOGGER.info("Odkaz na obrázky z ročníku " + volume.getYear() + " byly doplněny do fedory.");
        }
    }

    private void copyImages(List<Volume> periodikum, String serializedDataName) throws IOException {
        // sshfs root@editor.staff.mzk.cz:/mnt/imageserver/ /mnt/imageserver -o follow_symlinks

        // print uuid, year and number of pages of volumes, used if something goes wrong
        int pages = 0;
        for (Volume volume : periodikum) {

            LOGGER.info(volume.getPid() + " " + volume.getYear() + " " + volume.getPages().size());
            pages = pages + volume.getPages().size();
        }

        LOGGER.info("Total number of pages: " + pages);

        List<String> toSkip = skipVolumes();
        for (Volume volume : periodikum) {
            //LOGGER.info(volume.getPid() + " " + volume.getYear());
            if (toSkip.contains(volume.getPid())) {
                LOGGER.info("Skipping volume " + volume.getYear() + " " + volume.getPid());
                continue;
            }

            LOGGER.info("Converting " + volume.getPages().size() + " pages of volume " + volume.getYear());

            for (Page page : volume.getPages()) {
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
                    //LOGGER.error(e.getMessage());
                }

                File imageserverFile = new File(imageserverPath);

                // copy file to imageserver
                if (imageserverFile.exists() && imageserverFile.length() != 0) {
                    LOGGER.warn("Obrázek " + page.getPid() + " " + page.getDjvuImgName() + " už je na imageserveru, nekopírovat");
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
                    // pri niektorych obrazkoch sa rozbiju metadata po dvojitom konvertovani do tifu
                    // v takom pripade staci zakomentovat konverziu do tifu v sktipte (compress.sh)
                    LOGGER.error("Kopírování obrázku "+ page.getPid()+" z " + djvuName + " do " + imageserverPath + " selhalo.");
                    LOGGER.error(e.getMessage());
                    continue;
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
        //toSkip.add("uuid");
        return toSkip;
    }

    private List<Volume> getDataFromK5(List<Volume> periodikum, String rootUuid) throws K5ApiException {
        List<Item> volumes = k5Api.getChildren(rootUuid);
        for (Item volume : volumes) {
            periodikum.add(new Volume(volume.getDetails().getYear().replaceAll("\\s",""), volume.getPid()));
            // print uuid and year of volumes - useful to create toSkip
            LOGGER.info(volume.getPid() + " " + volume.getDetails().getYear());
        }
        // buď tak, nebo ručně povkládat seznam ročník - uuid (v případě velkého periodika, kde je jen pár djvu ročníků):
        // periodikum.add(new Volume("1933", "uuid:5c39cf90-dfa6-11dc-9c02-000d606f5dc6"));

        List<String> toSkip = skipVolumes();

        for (Volume volume : periodikum) {
            if (toSkip.contains(volume.getPid())) {
                LOGGER.info("Skipping volume " + volume.getYear() + " " + volume.getPid());
                continue;
            }

            List<String> pageUuids = fedoraUtils.getChildrenUuids(volume.getPid(), DigitalObjectModel.PAGE);
            List<Item> pageItems = new ArrayList<>();

            for (String pid : pageUuids){
                pageItems.add(k5Api.getItem(pid));
            }

            for (int j = 0; j < pageItems.size(); j++) {
                Page page = new Page();
                Item pageItem = pageItems.get(j);
                String itemTitle = pageItem.getDetails().getPagenumber();
                itemTitle = CharMatcher.WHITESPACE.trimFrom(itemTitle); // trim() nezvládá non-breaking space
                page.setTitle(itemTitle);
                page.setPid(pageItem.getPid());
                volume.getPages().add(page);
            }

            String almostFileName = "IO/periodikum-almost.ser";
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
        return "Konverze stran periodik z djvu/jpeg do jp2 a přesun na imageserver";
    }
}