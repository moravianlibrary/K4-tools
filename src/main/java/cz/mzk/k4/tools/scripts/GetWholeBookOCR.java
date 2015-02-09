package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.api.*;
import cz.mzk.k4.tools.utils.*;
import domain.*;
import org.apache.log4j.*;
import java.io.*;
import java.util.*;

/**
 * Created by holmanj on 5.2.15.
 */
public class GetWholeBookOCR implements Script {
    private ClientRemoteApi api;
    private static final Logger LOGGER = Logger.getLogger(GetWholeBookOCR.class);

    public GetWholeBookOCR() {
        AccessProvider accessProvider = new AccessProvider();
        api = KrameriusClientRemoteApiFactory.getClientRemoteApi(accessProvider.getKrameriusHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());    }

    public void run(List<String> args) {
        String monographUuid = args.get(0);
        List<Item> children = api.getChildren(monographUuid);
        LOGGER.info("Got children of object " + monographUuid);
        PrintWriter out = null;

        try {
            out = new PrintWriter("Full_OCR");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (Item item : children) {
            String pageUuid = item.getPid();
            String pageOCR = api.getOCR(pageUuid);
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
