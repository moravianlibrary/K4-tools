package cz.mzk.k4.tools.scripts;

import org.fcrepo.client.FedoraClient;
import org.fcrepo.server.management.FedoraAPIM;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: Martin Rumanek
 * @version: 9/18/13
 */
public class FindNullCharacterInOcr {

    private static final String FEDORA_URL = "http://krameriustest.mzk.cz/fedora/get/";//"http://fedora.mzk.cz/fedora/get/";
    private static String USER = "";//fedora user
    private static String PASS = "";//fedora password
    static final String CONF_FILE_NAME = "k4_tools_config.properties";

    public static void run() {
        String home = System.getProperty("user.home");
        File f = new File(home + "/" + CONF_FILE_NAME);

        Properties properties = new Properties();

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(f);
            properties.load(inputStream);
        } catch (IOException ex) {
            Logger.getLogger(ConvertDjvuToJP2.class.getName()).log(Level.SEVERE, null, ex);
        }

        USER = properties.getProperty("fedora.username");
        PASS = properties.getProperty("fedora.password");

        try {
            FedoraClient fc = new FedoraClient(FEDORA_URL, USER, PASS);
            FedoraAPIM apim = fc.getAPIM();
            //apim.get
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
