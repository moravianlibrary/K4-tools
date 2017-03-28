package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.workers.UuidWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 9/18/13
 */
public class FindBadCharacterInOcr extends UuidWorker implements Script {

    private static final Logger LOGGER = LogManager.getLogger(FindBadCharacterInOcr.class);
    private boolean repair;
    FedoraUtils fedoraUtils;

    public FindBadCharacterInOcr(boolean writeEnabled) throws FileNotFoundException {
        super(writeEnabled);
        fedoraUtils = new FedoraUtils(new AccessProvider());
    }

    /**
     * @param text
     * @return
     */
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

    /**
     * @param codePoint
     * @return
     */
    private static boolean isBadCharacter(int codePoint) {
        return (!((codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))));
    }

    /**
     * @param text
     * @return
     */
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

    public void setRepair(boolean repair) {
        this.repair = repair;
    }

    /**
     * @param uuid
     */
    public void run(String uuid)  {

        if (isWriteEnabled()) {
            repair = true;
        }
        List<String> list;
        list = fedoraUtils.getChildrenUuids(uuid, DigitalObjectModel.PAGE);

        for (String uuidChild : list) {
            if (containBadCharacter(fedoraUtils.getOcr(uuidChild))) {
                LOGGER.warn(uuidChild + " contain some bad characters");
                if (repair) {
                    try {
                        fedoraUtils.setOcr(uuidChild, removeBadCharacters(fedoraUtils.getOcr(uuidChild)));
                    } catch (CreateObjectException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                LOGGER.info(uuidChild + " is just fine");
            }
        }


    }

    /**
     * @param args
     */
    public void run(List<String> args) {
        run(args.get(0));
        //fedoraUtils.applyToAllUuidFromModel(args[1], findBadCharacterInOcr);
        //                FindBadCharacterInOcr findBadCharacterInOcr = new FindBadCharacterInOcr();
        //findBadCharacterInOcr.setRepair(args[2].equals("opravit"));
        //findBadCharacterInOcr.run(args[1]);
    }

    // TODO: getUsage FindBadCharacterInOcr
    @Override
    public String getUsage() {
        return null;
    }

}
