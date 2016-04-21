package cz.mzk.k4.tools.scripts;

import com.sun.jersey.api.client.UniformInterfaceException;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.utils.util.DCContentUtils;
import cz.mzk.k4.tools.workers.ImageUrlWorker;
import cz.mzk.k4.tools.workers.PresunImgWorker;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import javax.ws.rs.core.MediaType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by holmanj on 3/19/14.
 */
public class ChangeImgs implements Script {
    private static final Logger LOGGER = Logger.getLogger(ChangeImgs.class);
    private AccessProvider accessProvider = AccessProvider.getInstance();
    private FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);
    private ClientRemoteApi clientApi = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    private ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    private PresunImgWorker stehovak;
    private ImageUrlWorker prepisovakUrl;

    boolean isMonograph;
    String imageserverFolderPath;
    String topUuid;

    private static final String ROCNIK = "1956";
    //    private static String ROCNIK;
    private static final int LAST_ITEM = 0;
    private static final String PERIODIKUM = "ss";

    public ChangeImgs() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {
        stehovak = new PresunImgWorker(args.contains("writeEnabled"), accessProvider, fedoraUtils);
        prepisovakUrl = new ImageUrlWorker(args.contains("writeEnabled"), accessProvider, fedoraUtils);

        topUuid = args.get(0);

        // Katalog
//        String topUuid = "uuid:accc7e4e-9c97-4b25-aae7-a09c90e7b528"; ROCNIK = "1986";
        String action = args.get(1);

        if (action.equals("kontrola")) {
            // počet všech stran pro kontrolu (případně vypsání jejich uuid)
            LOGGER.info("Spuštěna kontrola počtu stran periodika " + PERIODIKUM + ", ročníku " + ROCNIK);
//            System.out.println("Počet stran: " + getPageModelNumber(topUuid, null));
            System.out.println("Počet stran: " + getPageModelNumber(topUuid, "kontrola.txt"));
        } else if (action.equals("vypis-cest")) {
            // vytvoření souboru s vazbami uuid strany - umístění obrázku
            LOGGER.info("Spuštěn výpis stránek a cest k obrázkům periodika " + PERIODIKUM + ", ročníku " + ROCNIK);
            pairImgPages(topUuid, PERIODIKUM + "-" + ROCNIK + "-uuids-paths");
            // na další krok je potřeba ruční zformátování souboru vazeb na (uspořádaný) seznam umístění obrázků (lokální, struktura složek jako na imageserveru)
        } else if (action.equals("konverze")) {
            // konverze + přesun obrázků do správné složky
            // soubor s cestama, složka s tiffy
            LOGGER.info("Spuštěna konverze skenů periodika " + PERIODIKUM + ", ročníku " + ROCNIK);
            stehovak.run("paths-ready/" + PERIODIKUM + "-" + ROCNIK + "-paths", "/home/holmanj/FSV/fsv-zbytek/" + PERIODIKUM + "/" + ROCNIK);
        } else if (action.equals("oprava-url")) {
            // oprava odkazů na obrázky (přidání /imageserver) + oprava RELS-EX (odkaz na dlaždice místo DeepZoom)
            LOGGER.info("Spuštěna oprava url v datastreamech periodika " + PERIODIKUM + ", ročníku " + ROCNIK + " " + topUuid);
            prepisovakUrl.run(topUuid);
        }

        LOGGER.info("Konec");
    }

    @Override
    public String getUsage() {
        return "Výměna djvu za jp2 - nahrazení linků ve fedoře za nové" +
                "\n\n" +
                "pomocný skript na doplnění chybějících obrázků do imageserveru.\n" +
                "kroky obnovy: \n" +
                "výpis cest do imageserveru - seznam obrázků + cest\n" +
                "ruční úprava souboru na uspořádaný seznam umístění\n" +
                "konverze obrázků z tiff do jp2 a přesun na správná místa (lokální struktura jako na imageserveru, potom hromadný ruční přesun přes scp; struktura složek musí existovat\n" +
                "oprava datastreamů a vazeb na dlaždice v RELS-EXT";
    }

    private String getTitle(String uuid) {
        String title = "";
        Document dcStream = null;
        try {
            dcStream = fedoraUtils.getDCStream(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        title = DCContentUtils.titleFromDC(dcStream);
        return title;
    }

    private int getPageModelNumber(String topUuid, String filename) {
        List<String> pages = fedoraUtils.getChildrenUuids(topUuid, DigitalObjectModel.PAGE);

        if (filename != null) {
            PrintWriter pageWriter = null;
            try {
                pageWriter = new PrintWriter(filename, "UTF-8");
                for (String page : pages) {
                    pageWriter.println(page);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } finally {
                pageWriter.close();
            }

        }
        return pages.size();
    }

    private void pairImgPages(String topUuid, String filename) {
        // <cislo rocniku, <cislo strany, uuid strany>>
        Map<String, Map<String, String>> uuidMap = new HashMap<>();
        List<String> itemUuids = fedoraUtils.getChildrenUuids(topUuid, DigitalObjectModel.PERIODICALITEM); // seznam uuid všech svazků v ročníku
        List<String> pageUuids;
        LOGGER.info("Načteno " + itemUuids.size() + " čísel");

        int numberOfPages = 0;
        for (String item : itemUuids) {
            // mapa cislo strany (title) + uuid strany
            Map<String, String> pages = new HashMap<>();
            String itemTitle = getTitle(item);
            pageUuids = fedoraUtils.getChildrenUuids(item, DigitalObjectModel.PAGE);
            numberOfPages += pageUuids.size();
            for (String page : pageUuids) {
                String pageTitle = getTitle(page);
                if (pages.containsKey(pageTitle)) {
                    LOGGER.warn("Strana " + pageTitle + " čísla " + itemTitle + " se opakuje!");
                }
                pages.put(pageTitle, page);
            }
            if (uuidMap.containsKey(itemTitle)) {
                LOGGER.warn("Číslo " + itemTitle + " se opakuje!");
            }
            uuidMap.put(itemTitle, pages);
        }
        LOGGER.info("Načteno " + numberOfPages + " stran");

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename, "UTF-8");
            int lastItem = itemUuids.size();
            for (int i = 1; i <= lastItem; i++) {
                Map<String, String> foundPages = uuidMap.get(Integer.toString(i));
                if (i == LAST_ITEM+1) {
                    LOGGER.warn("Asi je nějaké číslo víckrát");
                    break;
                }
                if (foundPages == null) {
                    LOGGER.warn("Nenalezen ročník " + i);
                    lastItem++; // číslo (title) posledního ročníku je vyšší než počet všech čísel (items)
                    continue;
                }
                writer.println("ITEM " + i);
                for (int j = 1; j <= foundPages.size(); j++) {
                    String pageUuid = "";
                    pageUuid = foundPages.get(Integer.toString(j));

                    // najít umístění obrázku v imageserveru
                    String datastreamString = null;
                    try {
                        datastreamString = accessProvider.getFedoraWebResource("/objects/" + pageUuid + "/datastreams/IMG_FULL").accept(MediaType.TEXT_HTML).get(String.class);
                    } catch (UniformInterfaceException ex) {
                        LOGGER.error("Chyba při získávání datastramu\nČíslo " + i + ", strana " + j);
                        LOGGER.error(ex.getMessage());
                        return;
                    }
                    // např. http://imageserver.mzk.cz/mzk01/000/662/379/9dab98f2-da26-47f6-a22a-57ad64578fc0/big.jpg

                    // Datastream Location: http://meditor.fsv.cuni.cz/mzk01/000/117/728/bc8e5a19-8c37-38f0-89b7-7f433a4abc78/big.jpg
                    String[] locationArray = datastreamString.split(accessProvider.getImageserverHost());

                    // formát: /mzk01/000/117/728/bc8e5a19-8c37-38f0-89b7-7f433a4abc78.jp2
                    locationArray = locationArray[1].split("/");

                    String imgDestName = "/imageserver-data/" + locationArray[1] + "/" + locationArray[2] + "/" + locationArray[3] + "/" + locationArray[4] + "/" + locationArray[5];  // meditor.fsv.cuni.cz
//                    String imgDestName = "/imageserver-data/" + locationArray[1] + "/" + locationArray[2] + "/" + locationArray[3] + "/" + locationArray[4] + "/" + locationArray[5]  + "/" + locationArray[6];  // meditor.fsv.cuni.cz

                    // zápis do souboru
                    writer.println("PAGE " + j + " : " + pageUuid + " -> /home/holmanj/fsv-obnova/" + imgDestName);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }
}