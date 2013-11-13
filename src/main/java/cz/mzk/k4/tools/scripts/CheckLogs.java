package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.domain.KrameriusProcess;
import cz.mzk.k4.tools.domain.ProcessLog;
import cz.mzk.k4.tools.utils.ProcessManager;
import cz.mzk.k4.tools.utils.Script;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * Log prohledávání v souboru logs/checkK4Logs + sout
 * Při nalezení FINISHED procesu s neprázdným chybovým výstupem vyrobí soubor 
 * 		logs/{uuid}.txt s chybovým výstupem procesu
 *
 * 	@author Jan Holman
 * 
 */
public class CheckLogs implements Script {

	private static String LIBRARY_PREFIX = "";
	private static String HOST;
	private static ProcessManager pm;
	private static Integer defSize; // default number of processes fetched from Kramerius
	private static org.apache.log4j.Logger LOGGER = Logger
			.getLogger(CheckLogs.class);
	static final String CONF_FILE_NAME = "k4_tools_config.properties";

	public void run(List<String> args) {

		// get properties file (/home/{user}/properties)
		String home = System.getProperty("user.home");
		File f = new File(home + "/" + CONF_FILE_NAME);

		Properties properties = new Properties();
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(f);
			properties.load(inputStream);
		} catch (IOException e) {
			LOGGER.fatal("Cannot load properties file");
		}

		defSize = Integer.parseInt(properties.getProperty("checkLogs.resultSize"));
        LIBRARY_PREFIX = properties.getProperty("knihovna");
        HOST = properties.getProperty(LIBRARY_PREFIX + ".host");
		LOGGER.info("Knihovna: " + LIBRARY_PREFIX);
		String username = properties.getProperty(LIBRARY_PREFIX + ".username");
		String password = properties.getProperty(LIBRARY_PREFIX + ".password");
		pm = new ProcessManager(HOST, username, password);

		try {
			// handle URL parameters
			Map<String, String> params = new HashMap<String, String>();
			params.put("checkLogs.resultSize", defSize.toString());
			params.put("state", "FINISHED");
			int resultSize = Integer.parseInt(defSize.toString());

			// fetch processes
			List<KrameriusProcess> processes = pm.searchByParams(params);

			// specified resultSize can be bigger than the actual number of
			// processes fetched from Kramerius
			if (resultSize > processes.size()) {
				resultSize = processes.size();
			}

			int count = 0;
			int errorCount = 0;
			int notFoundCount = 0;
			BufferedWriter writer = null;
			File file = null;
			for (int i = 0; i < processes.size(); i++) {
				count++;
				KrameriusProcess p = processes.get(i);
				try {
					ProcessLog log = pm.getLog(p.getUuid());
					if (!log.getSerr().equals("")) {
						LOGGER.info(p.getUuid() + ": " + log.getSerr());
						try {
							file = new File("logs/" + p.getUuid() + ".txt");
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
        return null;
    }
}