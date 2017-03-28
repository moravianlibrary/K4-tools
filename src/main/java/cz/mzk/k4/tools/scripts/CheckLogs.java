package cz.mzk.k4.tools.scripts;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import cz.mzk.k5.api.remote.domain.Process;
import cz.mzk.k5.api.remote.domain.ProcessLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Jan Holman
 */
public class CheckLogs implements Script {

    private static ProcessRemoteApi krameriusApi;
    private static Integer defSize; // default number of processes fetched from Kramerius
    private static Logger LOGGER = LogManager.getLogger(CheckLogs.class);

    public CheckLogs() throws FileNotFoundException {
        AccessProvider accessProvider = AccessProvider.getInstance();
        krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
                accessProvider.getKrameriusHost(),
                accessProvider.getKrameriusUser(),
                accessProvider.getKrameriusPassword());
        defSize = Integer.parseInt(accessProvider.getProperties().getProperty("checkLogs.resultSize"));
    }


    /**
     * Log prohledávání v souboru logs/checkK4Logs + sout
     * Při nalezení FINISHED procesu s neprázdným chybovým výstupem vyrobí soubor
     * logs/{uuid}.txt s chybovým výstupem procesu
     *
     * @param args - nebere parametry
     */
    public void run(List<String> args) {
        try {
            // handle URL parameters
            MultivaluedMap queryParams = new MultivaluedMapImpl();
            queryParams.add("checkLogs.resultSize", defSize.toString());
            queryParams.add("state", "FINISHED");
            int resultSize = Integer.parseInt(defSize.toString());

            // fetch processes
            List<Process> processes = krameriusApi.filterProcesses(queryParams);

            // specified resultSize can be bigger than the actual number of
            // processes fetched from Kramerius
            if (resultSize > processes.size()) {
                resultSize = processes.size();
            }

            int count = 0;
            int errorCount = 0;
            int notFoundCount = 0;
            BufferedWriter writer = null;
            File file;
            for (int i = 0; i < processes.size(); i++) {
                count++;
                Process process = processes.get(i);
                try {
                    ProcessLog log = krameriusApi.getProcessLog(process.getUuid());
                    if (!log.getSerr().equals("")) {
                        LOGGER.info(process.getUuid() + ": " + log.getSerr());
                        try {
                            file = new File("logs/" + process.getUuid() + ".txt");
                            writer = new BufferedWriter(new FileWriter(file));
                            writer.write(log.getSerr());
                        } catch (IOException ex) {
                            LOGGER.error(ex.getMessage());
                        } finally {
                            try {
                                writer.close();
                            } catch (Exception ex) {
                                LOGGER.error(ex.getMessage());
                            }
                        }
                        errorCount++;
                    }
                } catch (IllegalStateException ex) {
                    notFoundCount++;
                    continue;
                } finally {
                    if (count == 100) {
                        LOGGER.info(i + 1 + " / " + processes.size()
                                + "; logs not found: " + notFoundCount);
                        count = 0;
                        notFoundCount = 0;
                    }
                }
            }
            LOGGER.info("DONE");
            LOGGER.info("Found " + errorCount + " possible errors.");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getUsage() {
        return "checkLogs - Kontroluje, jestli se v K4 nevyskytují procesy ve stavu FINISHED s neprázdným chybovým výstupem." +
                " Přístup ke K4 z konfig. souboru, nebere parametry." +
                " Výstup (logy problémových procesů) v souborech logs/{uuid procesu}.txt";
    }
}