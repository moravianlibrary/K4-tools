package cz.mzk.k4.tools.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 11/13/13
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class AccessProvider {

    private String fedoraHost;
    private String fedoraUser;
    private String fedoraPassword;
    private String krameriusHost;
    private String krameriusUser;
    private String krameriusPassword;
    private String libraryPrefix;
    private Client client;
    private String confFileName = "k4_tools_config.properties";
    private Properties properties;
    private static Logger LOGGER = Logger.getLogger(AccessProvider.class);

    public AccessProvider() {
        // get properties file (/home/{user}/{confFileName})
        String home = System.getProperty("user.home");
        File f = new File(home + File.separatorChar + confFileName);
        properties = new Properties();
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(f);
            properties.load(inputStream);

        
        
        } catch (IOException e) {
            LOGGER.fatal("Cannot load properties file");
        }

        libraryPrefix = properties.getProperty("knihovna");

        krameriusHost = properties.getProperty(libraryPrefix + ".k4.host");
        krameriusUser = properties.getProperty(libraryPrefix + ".k4.username");
        krameriusPassword = properties.getProperty(libraryPrefix + ".k4.password");

        fedoraHost = properties.getProperty(libraryPrefix + ".fedora.host");
        fedoraUser = properties.getProperty(libraryPrefix + ".fedora.username");
        fedoraPassword = properties.getProperty(libraryPrefix + ".fedora.password");
    }

    /**
     *
     * @param query
     * @return
     */
    public WebResource getKrameriusWebResource(String query) {
        Client client = Client.create();
        String url = "http://" + krameriusHost + query;  // např. "/search/api/v4.6/processes/" + uuid + "/logs"
        LOGGER.debug("Kramerius url: " + url);
        WebResource resource = client.resource(url);
        BasicAuthenticationFilter credentials = new BasicAuthenticationFilter(krameriusUser, krameriusPassword);
        resource.addFilter(credentials);
        return resource;
    }

    /**
     *
     * @param query
     * @return
     */
    public WebResource getFedoraWebResource(String query) {
        Client client = Client.create();
        String url = "http://" + fedoraHost + query;
        LOGGER.debug("Fedora url: " + url);
        WebResource resource = client.resource(url);
        BasicAuthenticationFilter credentials = new BasicAuthenticationFilter(fedoraUser, fedoraPassword);
        resource.addFilter(credentials);
        return resource;
    }

    public String getFedoraHost() {
        return fedoraHost;
    }

    public void setFedoraHost(String fedoraHost) {
        this.fedoraHost = fedoraHost;
    }

    public String getFedoraUser() {
        return fedoraUser;
    }

    public void setFedoraUser(String fedoraUser) {
        this.fedoraUser = fedoraUser;
    }

    public String getFedoraPassword() {
        return fedoraPassword;
    }

    public void setFedoraPassword(String fedoraPassword) {
        this.fedoraPassword = fedoraPassword;
    }

    public String getKrameriusHost() {
        return krameriusHost;
    }

    public void setKrameriusHost(String krameriusHost) {
        this.krameriusHost = krameriusHost;
    }

    public String getKrameriusUser() {
        return krameriusUser;
    }

    public void setKrameriusUser(String krameriusUser) {
        this.krameriusUser = krameriusUser;
    }

    public String getKrameriusPassword() {
        return krameriusPassword;
    }

    public void setKrameriusPassword(String krameriusPassword) {
        this.krameriusPassword = krameriusPassword;
    }

    public String getLibraryPrefix() {
        return libraryPrefix;
    }

    public void setLibraryPrefix(String libraryPrefix) {
        this.libraryPrefix = libraryPrefix;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getConfFileName() {
        return confFileName;
    }

    public void setConfFileName(String confFileName) {
        this.confFileName = confFileName;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
