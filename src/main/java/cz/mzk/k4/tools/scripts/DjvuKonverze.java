package cz.mzk.k4.tools.scripts;

import com.google.common.base.CharMatcher;
import cz.mzk.k4.tools.domain.Page;
import cz.mzk.k4.tools.domain.Publication;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by holmanj on 14.12.15.
 * Can be used for any document type, converts all pages under given uuid
 */
public class DjvuKonverze implements Script {
    public static final Logger LOGGER = LogManager.getLogger(DjvuKonverze.class);
    AccessProvider accessProvider = AccessProvider.getInstance();
    ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);

    public DjvuKonverze() throws FileNotFoundException {
    }

    // TODO: měnit podle potřeby (dohledat uuid a sysno) POZOR sysnoPath bez lomitka na konci !
    private Map<String, String > getPublicationsForConversion() {
        Map<String, String> toConvert = new LinkedHashMap<>();
        //toConvert.put("uuid","sysnoPath");
        return toConvert;
    }

    @Override
    public void run(List<String> args) {
        Map<String, String> uuidSysnopathMapping = getPublicationsForConversion();

        for (Map.Entry<String, String> entry : uuidSysnopathMapping.entrySet()) {
            String uuid = entry.getKey();
            String sysnoPath = entry.getValue();
            String title = null;

            try {
                title = k5Api.getItem(uuid).getRoot_title();
            } catch (K5ApiException e) {
                LOGGER.error(e.getMessage());
            }

            LOGGER.info("Spuštěna konverze z djvu (nebo jpg) do jp2 na " + uuid);
            // odkomentovat kód podle fáze (1. fáze odkomentovaná, pak buď 2. nebo 3. - radši postupně)

            // fáze 1: načíst a serializovat data z K5 (děje se vždycky)
            Publication publication = null;
            String serializedDataName = "IO/" + uuid + ".ser";

            if (new File(serializedDataName).exists()) {
                try {
                    publication = deserialize(serializedDataName);
                } catch (FileNotFoundException ex) {
                    LOGGER.error(ex.getMessage());
                }
            } else {
                LOGGER.warn("Serialized file not found. Loading data from  K5.");
                publication = new Publication(title, uuid);
                try {
                    publication = getDataFromK5(publication);
                } catch (K5ApiException e) {
                    e.printStackTrace();
                }
                serialize(publication, serializedDataName);
            }

            System.out.println( "Data načtena  " + uuid + " pages: " + publication.getPages().size());

            // fáze 2: přesun do imageserveru (případně konverze do jp2)
            // nově: stahovat obrázky z fedory, konvertovat, ukládat do imageserveru
//            try {
//                copyImages(publication, sysnoPath, serializedDataName);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            // fáze 3: doplnění vazeb do fedory
//            try {
//                addDatastreams(publication);
//            } catch (CreateObjectException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (TransformerException e) {
//                e.printStackTrace();
//            }

            // fáze 4: čištění starých djvu - není potřeba, fedora maže nepotřebné datastreamy sama
        }
    }

    private void addDatastreams(Publication publication) throws CreateObjectException, TransformerException, IOException {
        for (Page page : publication.getPages()) {
            page.setImageserverImgLocation(page.getImageserverImgLocation().replaceAll(" ", ""));
            // datastreamy
            fedoraUtils.setImgFullFromExternal(page.getPid(), page.getImageserverImgLocation() + "/big.jpg");
            fedoraUtils.setImgPreviewFromExternal(page.getPid(), page.getImageserverImgLocation() + "/preview.jpg");
            fedoraUtils.setImgThumbnailFromExternal(page.getPid(), page.getImageserverImgLocation() + "/thumb.jpg");

            //vazba na dlaždice
            fedoraUtils.repairImageserverTilesRelation(page.getPid());
        }
        LOGGER.info("Odkazy na obrázky byly doplněny do fedory. " + publication.getPid());
    }

    private void copyImages(Publication publication, String sysnoPath, String serializedDataName) throws IOException {
        // sshfs root@editor.staff.mzk.cz:/mnt/imageserver/ /mnt/imageserver -o follow_symlinks

        LOGGER.info("Converting pages of: " + publication.getPid());
        LOGGER.info("Imageserver location: " + sysnoPath);

        for (Page page : publication.getPages()) {
            String djvuName = page.getDjvuImgName();

            if ("".equals(djvuName) || djvuName == null) {
                djvuName = fedoraUtils.getDjVuImgName(page.getPid());
                page.setDjvuImgName(djvuName); // name
            }

            String imageserverPath = "/mnt/imageserver/" + sysnoPath + "/" + FilenameUtils.removeExtension(page.getDjvuImgName()) + ".jp2";
            page.setImageserverImgLocation("http://imageserver.mzk.cz/" + sysnoPath + "/" + FilenameUtils.removeExtension(page.getDjvuImgName()));
            File tempDjvuImage = new File(page.getDjvuImgName());

            try {
                FileUtils.copyInputStreamToFile(fedoraUtils.getImgFull(page.getPid(), "image/djvu"), tempDjvuImage);
            } catch (FileNotFoundException e) {
                LOGGER.error(e.getMessage());
            }

            File imageserverFile = new File(imageserverPath);
            // copy file to imageserver
            if (imageserverFile.exists() && imageserverFile.length() != 0) {
                LOGGER.warn("Obrázek " + page.getPid() + " " + imageserverPath + " už je na imageserveru, nekopírovat");
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
                LOGGER.error(e.getMessage());
                continue;
            }
        }
        serialize(publication, serializedDataName);
        LOGGER.info("Obrázky jsou zkonvertovány.");
    }

    private Publication getDataFromK5(Publication publication) throws K5ApiException {
        List<String> pageUuids = fedoraUtils.getChildrenUuids(publication.getPid(), DigitalObjectModel.PAGE);
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
            publication.getPages().add(page);
        }

        return publication;
    }

    private void serialize(Publication publication, String filename) {
        FileOutputStream fileOut = null;
        ObjectOutputStream out = null;
        try {
            fileOut = new FileOutputStream(filename);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(publication);
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

    private Publication deserialize(String filename) throws FileNotFoundException {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        Publication publication = null;
        try {
            fileIn = new FileInputStream(filename);
            in = new ObjectInputStream(fileIn);
            publication = (Publication) in.readObject();
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
        return publication;
    }

    @Override
    public String getUsage() {
        return "Konverze stran z djvu/jpeg do jp2 a přesun na imageserver";
    }
}
