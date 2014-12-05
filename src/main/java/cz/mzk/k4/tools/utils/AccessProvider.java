package cz.mzk.k4.tools.utils;


import org.apache.log4j.Logger;
import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIAService;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 11/13/13
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class AccessProvider {

    private static AccessProvider accessProvider;

    private FedoraAPIA fedoraAPIA;
    private String fedoraHost;
    private String fedoraUser;
    private String fedoraPassword;
    private String krameriusHost;
    private String krameriusUser;
    private String krameriusPassword;
    private String imageserverUser;
    private String imageserverHost;
    private String imageserverPassword;
    private String imageserverPath;
    private String imageserverUrlPath;
    private String imageserverPrivateKey;
    private String imageserverPrivatePassphrase;
    private String libraryPrefix;
    private Client client;
    private String confFileName = "k4_tools_config.properties";
    private Properties properties;
    private static Logger LOGGER = Logger.getLogger(AccessProvider.class);
    public static String K4_REMOTE_API_PATH = "/search/api/v4.6/processes";

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

        imageserverHost = properties.getProperty(libraryPrefix + ".imageserver.host");
        imageserverUser = properties.getProperty(libraryPrefix + ".imageserver.username");
        imageserverPassword = properties.getProperty(libraryPrefix + ".imageserver.password");
        imageserverPath = properties.getProperty(libraryPrefix + ".imageserver.path");
        imageserverUrlPath = properties.getProperty(libraryPrefix + ".imageserver.urlPath");
        imageserverPrivateKey = properties.getProperty(libraryPrefix + ".imageserver.privateKey");
        imageserverPrivatePassphrase = properties.getProperty(libraryPrefix + ".imageserver.privateKeyPassphrase");

        client = ClientBuilder.newClient();
    }

    public static AccessProvider getInstance() {
        if(accessProvider == null) {
            accessProvider = new AccessProvider();
        }
        return accessProvider;
    }


    /**
     * Vrací webresource s cestou ke K4 RemoteAPI
     * @param query
     * @return
     */
    public WebTarget getKrameriusRESTWebResource(String query) {
        String url = "http://" + krameriusHost + K4_REMOTE_API_PATH + query;  // např. "/" + uuid + "/logs"
//        LOGGER.debug("Kramerius remote api url: " + url);
        WebTarget resource = client.target(url);
        HttpAuthenticationFeature credentials = HttpAuthenticationFeature.basic(krameriusUser, krameriusPassword);
//        BasicAuthenticationFilter credentials = new BasicAuthenticationFilter(krameriusUser, krameriusPassword);
        resource.register(credentials);
        return resource;
    }

    /**
     * Vrací webresource s cestou ke K4
     * @param query
     * @return
     */
    public WebTarget getKrameriusWebResource(String query) {
        String url = "http://" + krameriusHost + query;  // např. "/" + uuid + "/logs"
//        LOGGER.debug("Kramerius url: " + url);
        WebTarget resource = client.target(url);
        HttpAuthenticationFeature credentials = HttpAuthenticationFeature.basic(krameriusUser, krameriusPassword);
//        BasicAuthenticationFilter credentials = new BasicAuthenticationFilter(krameriusUser, krameriusPassword);
        resource.register(credentials);
        return resource;
    }

    /**
     *
     * @param query
     * @return
     */
    public WebTarget getFedoraWebResource(String query) {
        String url = "http://" + fedoraHost + query;
//        LOGGER.debug("Fedora url: " + url);
        WebTarget resource = client.target(url);
        HttpAuthenticationFeature credentials = HttpAuthenticationFeature.basic(krameriusUser, krameriusPassword);
//        BasicAuthenticationFilter credentials = new BasicAuthenticationFilter(fedoraUser, fedoraPassword);
        resource.register(credentials);
        return resource;
    }

    /**
     *
     * @param query
     * @return
     */
    public WebTarget getSolrWebResource(String query) {
        String url = "http://" + krameriusHost + "/solr" + query;
//        LOGGER.debug("Fedora url: " + url);
        WebTarget resource = client.target(url);
//        BasicAuthenticationFilter credentials = new BasicAuthenticationFilter(krameriusUser, krameriusPassword);
//        resource.addFilter(credentials);
        return resource;
    }

    /*
     * @see https://wiki.duraspace.org/display/FEDORA35/API-A
     */
    public FedoraAPIA getFedoraAPIA() {
        if (fedoraAPIA == null) {
            initAPIA();
        }
        return fedoraAPIA;
    }
    /**
     * Inits the apia.
     */
    private void initAPIA() {
        final String user = getFedoraUser();
        final String pwd = getFedoraPassword();
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pwd.toCharArray());
            }
        });

        FedoraAPIAService APIAservice = null;
        try {
            APIAservice =
                    new FedoraAPIAService(
                            new URL("http://" + getFedoraHost() + "/wsdl?api=API-A"),
                            new QName("http://www.fedora.info/definitions/1/0/api/",
                                    "Fedora-API-A-Service"));
//                            new QName("http://www.fedora.info/definitions/1/0/types/",
//                                    "Fedora-API-A-Service"));
        } catch (MalformedURLException e) {
            LOGGER.error("InvalidURL API-A:" + e);
            throw new RuntimeException(e);
        }
        fedoraAPIA = APIAservice.getPort(FedoraAPIA.class);
        ((BindingProvider) fedoraAPIA).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user);
        ((BindingProvider) fedoraAPIA).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pwd);
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

    public String getImageserverUser() { return imageserverUser;
    }

    public void setImageserverUser(String imageserverUser) {
        this.imageserverUser = imageserverUser;
    }

    public String getImageserverHost() {
        return imageserverHost;
    }

    public void setImageserverHost(String imageserverHost) {
        this.imageserverHost = imageserverHost;
    }

    public String getImageserverPassword() {
        return imageserverPassword;
    }

    public void setImageserverPassword(String imageserverPassword) {
        this.imageserverPassword = imageserverPassword;
    }

    public String getImageserverPath() {
        return imageserverPath;
    }

    public void setImageserverPath(String imageserverPath) {
        this.imageserverPath = imageserverPath;
    }

    public String getImageserverUrlPath() {
        return imageserverUrlPath;
    }

    public void setImageserverUrlPath(String imageserverUrlPath) {
        this.imageserverUrlPath = imageserverUrlPath;
    }

    public String getImageserverPrivatePassphrase() {
        return imageserverPrivatePassphrase;
    }

    public void setImageserverPrivatePassphrase(String imageserverPrivatePassphrase) {
        this.imageserverPrivatePassphrase = imageserverPrivatePassphrase;
    }

    public String getImageserverPrivateKey() {
        return imageserverPrivateKey;
    }

    public void setImageserverPrivateKey(String imageserverPrivateKey) {
        this.imageserverPrivateKey = imageserverPrivateKey;
    }
}
