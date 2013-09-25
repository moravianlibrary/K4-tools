package cz.mzk.k4.tools.fedoraUtils;

import cz.mzk.k4.tools.fedoraUtils.util.IOUtils;
import cz.mzk.k4.tools.fedoraUtils.util.RESTHelper;
import org.apache.log4j.Logger;
import org.fedora.api.RelationshipTuple;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author: Martin Rumanek, incad
 * @version: 9/23/13
 */
public class FedoraUtils {

    /**
     * The Constant LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(FedoraUtils.class);
    private static final String FEDORA_URL = "http://krameriustest.mzk.cz/fedora/get/";//"http://fedora.mzk.cz/fedora/get/";
    private static String USER = "";//fedora user
    private static String PASS = "";//fedora password
    static final String CONF_FILE_NAME = "k4_tools_config.properties";

    public FedoraUtils() {
        String home = System.getProperty("user.home");
        File f = new File(home + "/" + CONF_FILE_NAME);


        Properties properties = new Properties();

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(f);
            properties.load(inputStream);
        } catch (IOException ex) {
            LOGGER.log(null, ex);
        }

        USER = properties.getProperty("fedora.username");
        PASS = properties.getProperty("fedora.password");
    }

    @SuppressWarnings("serial")
    public static ArrayList<ArrayList<String>> getAllChildren(String uuid) {
        List<RelationshipTuple> triplets = FedoraUtils.getObjectPids(uuid);
        ArrayList<ArrayList<String>> children = new ArrayList<ArrayList<String>>();

        if (triplets != null) {
            for (final RelationshipTuple triplet : triplets) {
                if (triplet.getObject().contains("uuid")
                        && triplet.getObject().contains(Constants.FEDORA_INFO_PREFIX)) {

                    final String childUuid =
                            triplet.getObject().substring((Constants.FEDORA_INFO_PREFIX).length());

                    if (!childUuid.contains("/")) {
                        children.add(new ArrayList<String>() {

                            {
                                add(childUuid);
                                add(triplet.getPredicate());
                            }
                        });
                    }
                }
            }
        }
        return children;
    }

    /**
     * Gets the object pids.
     *
     * @param subjectPid the subject pid
     * @return the object pids
     */
    public static List<RelationshipTuple> getObjectPids(String subjectPid) {
        return getSubjectOrObjectPids("%3Cinfo:fedora/" + subjectPid + "%3E%20*%20*");
    }

    private static List<RelationshipTuple> getSubjectOrObjectPids(String restOfCommand) {
        List<RelationshipTuple> retval = new ArrayList<RelationshipTuple>();
//        String command =
//                configuration.getFedoraHost() + "/risearch?type=triples&lang=spo&format=N-Triples&query="
//                        + restOfCommand;
        String command =
                FEDORA_URL + "/risearch?type=triples&lang=spo&format=N-Triples&query="
                        + restOfCommand;
        InputStream stream = null;
        try {
            stream =
                    RESTHelper.get(command,
                            USER, PASS,
//                            configuration.getFedoraLogin(),
//                            configuration.getFedoraPassword(),
            true);
            if (stream == null) return null;
            String result = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
            String[] lines = result.split("\n");
            for (String line : lines) {
                String[] tokens = line.split(" ");
                if (tokens.length < 3) continue;
                try {
                    RelationshipTuple tuple = new RelationshipTuple();
                    tuple.setSubject(tokens[0].substring(1, tokens[0].length() - 1));
                    tuple.setPredicate(tokens[1].substring(1, tokens[1].length() - 1));
                    tuple.setObject(tokens[2].substring(1, tokens[2].length() - 1));
                    tuple.setIsLiteral(false);
                    retval.add(tuple);
                } catch (Exception ex) {
                    LOGGER.info("Problem parsing RDF, skipping line:" + Arrays.toString(tokens) + " : " + ex);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (stream != null) try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return retval;
    }

}
