package cz.mzk.k4.tools;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @author: Martin Rumanek
 * @version: 11/13/13
 */
public class Configuration {

    private PropertiesConfiguration configuration;

    public Configuration() {
        try {
            configuration = new PropertiesConfiguration("rajhrad.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

    }

    public String getPathPrivateKey() {
        return configuration.getString("privateKey");
    }


    public String getPrivateKeypassphrase() {
        return configuration.getString("privateKeyPassphrase");
    }

    //marc export
    public String getSshUserMarcExport() {
        return configuration.getString("marcExportUser");
    }

    public String getSshHostMarcExport() {
        return configuration.getString("marcExportHost");
    }

    public String getPathMarcExport() {
        return configuration.getString("marcExportPath", "/work/aleph/data/aktualizace_zaznamu/mzk03.m21");
    }

    public String getPasswordMarcExport() {
        return configuration.getString("marcExportPassword");
    }

    //sysifos
    public String getSshUserWorkspace() {
        return configuration.getString("workspaceUser");
    }

    public String getSshHostWorkspace() {
        return configuration.getString("workspaceHost");
    }

    public String getPasswordWorkspace() {
        return configuration.getString("workspacePassword");
    }

    //imageserver
    public String getImageServerUserWorkspace() {
        return configuration.getString("imageserverUser");
    }

    public String getImageServerHostWorkspace() {
        return configuration.getString("imageserverHost");
    }

}
