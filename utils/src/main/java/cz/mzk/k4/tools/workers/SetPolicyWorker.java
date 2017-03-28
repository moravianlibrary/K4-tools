package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.domain.DCConent;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.utils.util.DCContentUtils;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger LOGGER = LogManager.getLogger(SetPolicyWorker.class);
    private FedoraUtils fedoraUtils;
    private ProcessRemoteApi krameriusApi;
//    private KrameriusUtils krameriusUtils; // nahrazano retrofit API

    public SetPolicyWorker(boolean writeEnabled, AccessProvider accessProvider) {
        super(writeEnabled);
        fedoraUtils = new FedoraUtils(accessProvider);
        krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(accessProvider.getFedoraHost(), accessProvider.getKrameriusUser(), accessProvider.getKrameriusPassword());
//        krameriusUtils = new KrameriusUtils(accessProvider);
    }

    @Override
    public void run(String uuid) {

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Pattern ROZSAH = Pattern.compile("\\d\\d\\d\\d\\-\\d\\d\\d\\d");

        boolean missingInfo = false;
        try {
            // get DC data
            Document document = fedoraUtils.getDCStream(uuid);
            DCConent dcConent = DCContentUtils.parseDCStream(document);
            String type = dcConent.getType();
            int year = Integer.parseInt(dcConent.getDate());

            // periodical, more than 70 years old
            if (type.equals("periodical") && (year < (currentYear - 70))) {
                LOGGER.info("Setting object " + uuid + " to PUBLIC (starý periodikum - " + year + ")");
                krameriusApi.setPublic(uuid);
                return;

                // periodical, less than 70 years old
            } else if (type.equals("periodical")) {
                krameriusApi.setPrivate(uuid);
                LOGGER.info("Setting object " + uuid + " to PRIVATE (mladý periodikum - " + year + ")");
                return;

                // monograph
            } else if (type.equals("monograph")) {
                // seznam autorů
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
                            krameriusApi.setPrivate(uuid);
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
                        krameriusApi.setPublic(uuid);
                        return;
                    } else {
                        LOGGER.info("Chybí info, ale " + uuid + " bude PRIVATE (vydání: " + year + ")");
                        krameriusApi.setPrivate(uuid);
                        return;
                    }

                } else {
                    // všichni dávno mrtví
                    LOGGER.info("Setting object " + uuid + " to PUBLIC, všichni jsou dávno mrtví");
                    krameriusApi.setPublic(uuid);
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
        } catch (K5ApiException e) {
            e.printStackTrace();
        }

    }
}
