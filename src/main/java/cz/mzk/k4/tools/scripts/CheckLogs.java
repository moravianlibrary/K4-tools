package cz.mzk.k4.tools.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import cz.mzk.k4.tools.domain.Knihovna;
import cz.mzk.k4.tools.domain.KrameriusProcess;
import cz.mzk.k4.tools.domain.ProcessLog;
import cz.mzk.k4.tools.utils.ProcessManager;

/**
 * 
 * Log prohledávání v souboru logs/checkK4Logs + sout
 * Při nalezení FINISHED procesu s neprázdným chybovým výstupem vyrobí soubor 
 * 		logs/{uuid}.txt s chybovým výstupem procesu
 * 
 */
public class CheckLogs {

	private static Knihovna knihovna;
	private static String host;
	private static ProcessManager pm;
	// default number of processes fetched from Kramerius
	private static Integer defSize;
	private static org.apache.log4j.Logger LOGGER = Logger
			.getLogger(CheckLogs.class);
	static final String CONF_FILE_NAME = "k4_tools_config.properties";

	public static void run() {

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

		defSize = Integer.parseInt(properties.getProperty("resultSize"));
		knihovna = Knihovna.valueOf(properties.getProperty("knihovna"));
		host = properties.getProperty(knihovna + ".host");
		LOGGER.info("Knihovna: " + knihovna);
		String username = properties.getProperty(knihovna + ".username");
		String password = properties.getProperty(knihovna + ".password");
		pm = new ProcessManager(host, username, password);

		try {
			// handle URL parameters
			Map<String, String> params = new HashMap<String, String>();
			params.put("resultSize", defSize.toString());
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
}