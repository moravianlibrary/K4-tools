package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k5.api.client.ClientRemoteApi;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.client.KrameriusClientRemoteApiFactory;
import cz.mzk.k5.api.client.domain.Item;
import org.apache.log4j.Logger;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by holmanj on 5.2.15.
 */
public class GetWholeBookOCR implements Script {
    private ClientRemoteApi api;
    private static final Logger LOGGER = Logger.getLogger(GetWholeBookOCR.class);

    public GetWholeBookOCR() {
        AccessProvider accessProvider = new AccessProvider();
        api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
    }

    public void run(List<String> args) {
        String monographUuid = args.get(0);
        List<Item> children = null;
        try {
            children = api.getChildren(monographUuid);
        } catch (K5ApiException e) {
            e.printStackTrace();
        }
        LOGGER.info("Got children of object " + monographUuid);
        PrintWriter out = null;

        try {
            out = new PrintWriter("Full_OCR");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (Item item : children) {
            String pageUuid = item.getPid();
            String pageOCR = null;
            try {
                pageOCR = api.getOCR(pageUuid);
            } catch (K5ApiException e) {
                e.printStackTrace();
            }
            out.println(pageOCR);
            out.println();
            LOGGER.info("Got OCR stream of object " + pageUuid);
        }
        out.close();
    }

    @Override
    public String getUsage() {
        return "getBookOCR\n" +
                "Uloží do 1 souboru OCR celé knihy/čísla\n" +
                "Argument: uuid monografie/čísla";
    }
}
