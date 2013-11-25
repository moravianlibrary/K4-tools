package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.domain.DCConent;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.FedoraUtils;
import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.util.DCContentUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 11/19/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class SetPolicyWorker extends UuidWorker {

    private static final Logger LOGGER = Logger.getLogger(SetPolicyWorker.class);
    private FedoraUtils fedoraUtils;
    private KrameriusUtils krameriusUtils;

    public SetPolicyWorker(boolean writeEnabled, AccessProvider accessProvider) {
        super(writeEnabled);
        fedoraUtils = new FedoraUtils(accessProvider);
        krameriusUtils = new KrameriusUtils(accessProvider);
    }

    @Override
    public void run(String uuid) {

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Pattern ROZSAH = Pattern.compile("\\d\\d\\d\\d\\-\\d\\d\\d\\d");

        boolean zpracovavat_dal = true;
        boolean missingInfo = false;
        try {
            // get DC data
            Document document = fedoraUtils.getDCStream(uuid);
            DCConent dcConent = DCContentUtils.parseDCStream(document);
            String type = dcConent.getType();
            int year = Integer.parseInt(dcConent.getDate());

            // periodical, more than 70 years old
            if (type.equals("periodical") && (year < (currentYear - 70))) {
                LOGGER.info("Setting object " + uuid + " to PUBLIC");
//                krameriusUtils.setPublic(uuid);
                return;

                // periodical, less than 70 years old
            } else if (type.equals("periodical")) {
//                krameriusUtils.setPrivate(uuid);
//                LOGGER.info("Setting object " + uuid + " to PRIVATE");
//                return;

                // monograph
            } else if (type.equals("monograph")) {
                // seynam autorů
                List<String> creators = Arrays.asList(dcConent.getCreators());

                for (String creator : creators) {
                    //  letopočty narození a úmrtí
                    Matcher matcher = ROZSAH.matcher(creator);
                    if (matcher.find()) {
                        String rozsah = matcher.group();
                        // rok umrti
                        int umrti = Integer.parseInt(rozsah.substring(5, 9));
                        if (umrti > currentYear - 70) {
                            // private
                            LOGGER.info("Setting object " + uuid + " to PRIVATE, kvuli umrti " + umrti);
                            return;
                        }
                    } else {
                        missingInfo = true;
                        // chybi info - 110 let po vzdani
                    }
                }

                // co došlo sem - všichni autoři jsou dlouho po smrti
                // nebo chybí údaje o některých autorech (a ostatní jsou dlouho mrtví)
                // nebo úplně chybí autoři
                if (missingInfo || creators.isEmpty()) {
                    // chybí info o autorovi a ostatní jsou ok
                    // nebo nejsou uvedeni autoři
                    if (year < (currentYear - 110)) {
                        LOGGER.info("Chybí info, ale " + uuid + " bude PUBLIC (vydání: " + year + ")");
                        return;
                    } else {
                        LOGGER.info("Chybí info, ale " + uuid + " bude PRIVATE (vydání: " + year + ")");
                        return;
                    }

                } else {
                    // všichni dávno mrtví
                    LOGGER.info("Setting object " + uuid + " to PUBLIC, všichni jsou dávno mrtví");
                    return;
                }

            } else {
                LOGGER.info("Nezpracováno: " + uuid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            LOGGER.error(e.getMessage() + " on " + uuid);
        } catch (NullPointerException e) {
            LOGGER.error(e.getMessage() + " on " + uuid);
        }

     /*
    periodikum - 70 let po vydání
    monografie - aspoň 70 stará, všichni autoři aspoň umřeli aspoň před 70 lety
                - když nejsou autoři, tak před rokem teď-110 let

     */

    }
}
