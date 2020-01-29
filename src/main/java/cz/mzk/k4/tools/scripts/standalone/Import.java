package cz.mzk.k4.tools.scripts.standalone;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.GeneralUtils;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * This script is used to import batches of uuids stored in .txt file from Kramerius import folder.
 * Setup lp.xml for import process without any templates.
 *
 * <processes>
 *     <process>
 *         <id>parametrizedimport</id>
 *         <description>Import FOXML</description>
 *         <mainClass>org.kramerius.imports.ParametrizedImport</mainClass>
 *         <standardOs>lrOut</standardOs>
 *         <errOs>lrErr</errOs>
 *         <securedaction>import</securedaction>
 *     </process>
 * </processes>
 *
 * @author Aleksei Ermak
 */
public class Import {

    public static final Logger logger = LogManager.getLogger(Replicate.class);

    private static String filename = "IO/import.txt";
    private static String dirNamePrefix = "/root/.kramerius4/import/";

    public static void main(String[] args) throws FileNotFoundException {

        AccessProvider accessProvider = AccessProvider.getInstance();
        ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
                accessProvider.getKrameriusHost(),
                accessProvider.getKrameriusUser(),
                accessProvider.getKrameriusPassword()
        );

        List<String> uuids = GeneralUtils.loadUuidsFromFile(filename);

        for (String uuid : uuids) {
            try {
                String dirName = uuid.substring(uuid.lastIndexOf(":")+1);
                logger.info("import " + uuid + ", dirName: " + dirName);
                remoteApi.importFromDir(dirNamePrefix + dirName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
