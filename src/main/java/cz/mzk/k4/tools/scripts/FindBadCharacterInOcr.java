package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.utils.fedoraUtils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedoraUtils.exception.CreateObjectException;
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
        run(uuid, false);
    }

    public static void run(String uuid, boolean repair) {

        List<String> list;
        try {
            list = getChildren(uuid, new ArrayList<String>());

            for (String uuidChild : list) {
                if (containBadCharacter(fu.getOcr(uuidChild))) {
                    LOGGER.info(uuidChild + " contain some bad characters");
                    if (repair) {
                        try {
                            fu.setOcr(uuidChild, removeBadCharacters(fu.getOcr(uuidChild)));
                        } catch (CreateObjectException e) {
                            e.printStackTrace();
                        }
                    }
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
    protected static boolean containBadCharacter(String text) {
        if (text == null) return false;

        int codePoint;
        int i = 0;
        while (i < text.length()) {
            // This is the unicode code of the character.
            codePoint = text.codePointAt(i);
            if (isBadCharacter(codePoint)) {
                return true;
            }
            i += Character.charCount(codePoint);
        }
        return false;
    }

    private static boolean isBadCharacter(int codePoint) {
        return (!((codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))));
    }

    protected static String removeBadCharacters(String text) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current;

        if (text == null || ("".equals(text))) return "";
        for (int i = 0; i < text.length(); i++) {
            current = text.charAt(i);
            if (!isBadCharacter(current)) {
                out.append(current);
            }
        }
        return out.toString();
    }

}
