package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.client.domain.Item;
import cz.mzk.k5.api.common.K5ApiException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by hradskam on 2.7.14.
 */
public class DownloadImages implements Script {
    public static final Logger LOGGER = Logger.getLogger(DownloadImages.class);
    private AccessProvider accessProvider;
    private FedoraUtils fedoraUtils;
    private ClientRemoteApi clientApi;

    public DownloadImages() throws FileNotFoundException {
        accessProvider = AccessProvider.getInstance();
        fedoraUtils = new FedoraUtils(accessProvider);
        clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    }

    @Override
    public void run(List<String> args) {
//        List<String> rootUuids = new ArrayList<>();
//        rootUuids.add(args.get(0));
        List<String> rootUuids = GeneralUtils.loadUuidsFromFile(args.get(0));
        String imagesTargetFolder = args.get(1);
        String imageserverMountPoint = args.get(2);

        for (String rootUuid : rootUuids) {
            File imagesPathFile = new File(imagesTargetFolder + "/" + rootUuid.replace("uuid:", ""));
            if (!imagesPathFile.exists()) {
                imagesPathFile.mkdir();
            }
            LOGGER.info("Downloading images of " + rootUuid + " to " + imagesPathFile.getAbsolutePath());
            try {
                String model = clientApi.getItem(rootUuid).getModel();
                if (model.equals("monograph")) {
                    List<Item> pages = clientApi.getChildren(rootUuid);
                    LOGGER.info("Loaded " + pages.size() + " pages of " + rootUuid + " (monograph)");
                    for (Item page : pages) {
                        String pageUuid = page.getPid();
                        try {
//                            downloadImage(pageUuid, imagesPathFile.getPath());
                            copyImage(pageUuid, imagesPathFile.getPath(), imageserverMountPoint);
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage() + " " + pageUuid);
                            e.printStackTrace();
//                            continue;
                        }
                    }
                } else if (model.equals("periodical")) {
                    List<Item> volumes = clientApi.getChildren(rootUuid);
                    LOGGER.info("Loaded " + volumes.size() + " volumes of " + rootUuid + " (periodical)");
                    for (Item volume : volumes) {
                        imagesPathFile = new File(imagesTargetFolder + "/" + rootUuid.replace("uuid:", "") + "/" + volume.getPid().replace("uuid:", ""));
                        if (!imagesPathFile.exists()) {
                            imagesPathFile.mkdir();
                        }
                        List<String> pageUuids = fedoraUtils.getChildrenUuids(volume.getPid(), DigitalObjectModel.PAGE);
                        LOGGER.info("Downloading " + pageUuids.size() + " images of " + volume.getDetails().getYear() + " " + volume.getPid() + " to " + imagesPathFile.getAbsolutePath());
                        for (String pageUuid : pageUuids) {
                            try {
                                copyImage(pageUuid, imagesPathFile.getPath(), imageserverMountPoint);
                            } catch (IOException e) {
                                LOGGER.error(pageUuid + ": " + e.getMessage());
                            }
                        }
                    }
                }
                LOGGER.info("Finished downloading images of " + rootUuid + " " + clientApi.getItem(rootUuid).getTitle());
            } catch (K5ApiException e) {
                LOGGER.error(e.getMessage());
                continue;
            }
        }

    }

    // copies images from locally mounted imageserver directory
    private void copyImage(String pageUuid, String targetFolderPath, String imageserverMountpoint) throws IOException {
        // sshfs holmanj@krameriusndktest.mzk.cz:/data/imageserver/NDK/ /mnt/imageserver/NDK -o follow_symlinks
        // sshfs holmanj@editor.staff.mzk.cz:/mnt/imageserver/mzk01 /mnt/imageserver/mzk01 -o follow_symlinks
        String imageImageserverPath = fedoraUtils.getImgLocationFromHtml(pageUuid);
        imageImageserverPath = imageImageserverPath.replace("http://imageserver.mzk.cz/", "");
        String localOriginalImagePath = imageserverMountpoint + "/" + imageImageserverPath;
        File originalImage = new File(localOriginalImagePath);
        originalImage.setWritable(false);
        File newImage = new File(targetFolderPath + "/" + pageUuid.replace("uuid:", "") + ".jp2");
        FileUtils.copyFile(originalImage, newImage);
        LOGGER.debug("Page " + pageUuid + " image was copied to " + newImage.getAbsolutePath());
    }

    private void downloadImage(String pageUuid, String imagesFolderPath) throws IOException {
        InputStream imgStream = fedoraUtils.getImgJp2(pageUuid);
        String newImagePath = imagesFolderPath + "/" + pageUuid.replace("uuid:", "") + ".jp2";
        FileUtils.copyInputStreamToFile(imgStream, new File(newImagePath));
        LOGGER.debug("Page " + pageUuid + " image was downloaded to " + newImagePath);

    }

    @Override
    public String getUsage() {
        return "Postahuje obrázky ze seznamu root uuid (monograph, periodical). \n" +
                "Argumenty: [soubor se seznamem root uuid] [cílový adresář] [lokální cesta k imageserveru (mountpoint)]";
    }
}
