package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.fedoraUtils.domain.DigitalObjectModel;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 9/18/13
 */
public class FindBadCharacterInOcr {

    private static final Logger LOGGER = Logger.getLogger(FindBadCharacterInOcr.class);

    private static FedoraUtils fu = new FedoraUtils();

    public static void run(String uuid) {

        List<String> list;
        try {
            list = getChildren("uuid", new ArrayList<String>());

            for (String uuidChild: list) {
                if (containBadCharacter(fu.getOcr(uuid))) {
                    LOGGER.info(uuidChild + " contain some bad characters");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static List<String> getChildren(String uuid, List<String> uuidList) throws IOException {
        if (DigitalObjectModel.PAGE.equals(FedoraUtils.getModel(uuid))) {
            uuidList.add(uuid);
        }
        DigitalObjectModel parentModel = null;
        ArrayList<ArrayList<String>> children = fu.getAllChildren(uuid);

        if (children != null) {
            for (ArrayList<String> child : children) {
                getChildren(child.get(0), uuidList);
            }
        }

        return uuidList;
    }


    // http://blog.mark-mclaren.info/2007/02/invalid-xml-characters-when-valid-utf8_5873.html
    private static boolean containBadCharacter(String text) {
        int codePoint;
        int i = 0;
        while (i < text.length())
        {
            // This is the unicode code of the character.
            codePoint = text.codePointAt(i);
            if (!((codePoint == 0x9) ||
                    (codePoint == 0xA) ||
                    (codePoint == 0xD) ||
                    ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                    ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                    ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF)))) {
                return false;
            }
            i += Character.charCount(codePoint);
        }
        return true;
    }
}
