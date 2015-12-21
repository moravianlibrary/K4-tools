package cz.mzk.k4.tools.scripts;

import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.client.domain.Item;
import org.apache.log4j.Logger;
import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by holmanj on 7.5.15.
 */
public class StehovaniHades implements Script {
    private static AccessProvider accessProvider = AccessProvider.getInstance();
    private static FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);
//    private static KrameriusUtils krameriusUtils = new KrameriusUtils(accessProvider);
    private static ClientRemoteApi k5Api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    public static final Logger LOGGER = Logger.getLogger(StehovaniHades.class);

    @Override
    public void run(List<String> args) {
        // nová metoda pro opravu linků po hromadném přesunu obrázků z hada na imageserver
        // vstup je csv ve formátu uuid_[uuid strany]@[url obrázku]
        Map<String, String> pages = null;
        try {
            pages = loadCsv("hades-round3.txt");
        } catch (IOException e) {
            LOGGER.error("Došlo k chybě při načítání vstupního csv souboru");
            e.printStackTrace();
        }
        float counter = 0;
        int size = pages.size();
        float percentage;
        for (Map.Entry<String, String> entry : pages.entrySet()) {
            try {
                fedoraUtils.repairImageStreams(entry.getKey(), entry.getValue());
                fedoraUtils.repairImageserverTilesRelation(entry.getKey());
            } catch (CreateObjectException e) {
                LOGGER.error("Došlo k chybě při ukládání datastreamů k objektu " + entry.getKey());
                e.printStackTrace();
            } catch (TransformerException e) {
                LOGGER.error("Došlo k chybě při opravě odkazů na dlaždice k objektu " + entry.getKey());
                e.printStackTrace();
            } catch (IOException e) {
                LOGGER.error("Došlo k chybě při opravě odkazů na dlaždice k objektu " + entry.getKey());
                e.printStackTrace();
            }
            counter++;
            percentage = (counter * 100 / size);
            if (counter % 170 == 0 && percentage != 0) {
                LOGGER.info(percentage + " % hotovo (" + counter + " z " + size + ")");
            }

        }
    }

    private Map loadCsv(String path) throws IOException {
        Map<String, String> map = new HashMap();

        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

        String line;
        while ((line = reader.readLine()) != null) {
            String[] page = line.split("@");
            map.put(page[0].replace('_', ':'), page[1]);
        }

        return map;
    }


    public void runOlder(List<String> args) {

        // starší metoda na opravu pokažených linků do hada
        // nakonec se stejně všechno přesouvalo na imageserver - viz novější metoda

        // najít postižené dokumenty
        // přesunout obrázky?
        // opravit IMG_* datastreamy
        // opravit RELS-EXT (bude to potřeba?)

        // částečně mimo:
        // uuid:760607f4-5a62-4015-9d49-7afc17042e65 má odkazy do imageserveru ve tvaru http://imageserver.mzk.cz/2619298376/001/050/692/2619298376/1050692_00001/big.jpg
        // ale místo prvního 2619298376 by mělo být mzk03

        // načíst strany
        String topUuid = "uuid:760607f4-5a62-4015-9d49-7afc17042e65";
//        List<String> pages = fedoraUtils.getChildrenUuids(topUuid, DigitalObjectModel.PAGE);
        List<Item> pages = null;
        try {
            pages = k5Api.getChildren(topUuid);
        } catch (K5ApiException e) {
            e.printStackTrace();
        }
        System.out.println(pages.size());
        for (int i = 0; i < pages.size(); i++) {
            String uuid = pages.get(i).getPid();
            try {
                // vzít url
                String imageUrl = fedoraUtils.getImgLocationFromHtml(uuid);
                // opravit
                imageUrl = imageUrl.replace("2619298376/001", "mzk03/001");
                System.out.println(imageUrl);
                // přidat IMG datastreamy
                fedoraUtils.setImgFullFromExternal(uuid, imageUrl.replace(".jp2", "/big.jpg"));
                fedoraUtils.setImgPreviewFromExternal(uuid, imageUrl.replace(".jp2", "/preview.jpg"));
                fedoraUtils.setImgThumbnailFromExternal(uuid, imageUrl.replace(".jp2", "/preview.jpg"));
                // opravit RESL-EXT
                fedoraUtils.repairImageserverTilesRelation(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CreateObjectException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String getUsage() {
        return "Skript na opravu vazeb na obrázky při jejich stěhování z hades.mzk.cz na imageserver.mzk.cz";
    }
}
