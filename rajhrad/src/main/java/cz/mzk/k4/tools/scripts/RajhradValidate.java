package cz.mzk.k4.tools.scripts;

import com.jcraft.jsch.*;
import cz.mzk.k4.tools.domain.aleph.RecordHolder;
import cz.mzk.k4.tools.utils.Configuration;
import cz.mzk.k4.tools.utils.ImageserverUtils;
import cz.mzk.k4.tools.utils.LsItem;
import org.apache.commons.io.*;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.joda.time.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

/**
 * Created by rumanekm on 27.3.14.
 */
public class RajhradValidate {

    private static final String EXPORT_PATH = System.getProperty("user.home") + "/mzk03.m21";

    private Configuration configuration = new Configuration();


    private ImageserverUtils imageserverUtils = new ImageserverUtils();

    public static void main(String[] args) {
        RajhradValidate validator = new RajhradValidate();
        validator.run(args[1]);
    }


    protected void run(String path) {
        List<LsItem> lsList = null;

        try {
            lsList = imageserverUtils.getFilenames(path);
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> list = new ArrayList<String>();
        for (LsItem item : lsList) {
            if (item.isDirectory()) {
                run(path + "/" + item.getFilename());
            } else {
                list.add(item.getFilename());
            }
        }
        System.out.println("##Kontrola adresáře " + path);
        RecordHolder holder = new RecordHolder(list);

        try {
            this.copyBase();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Validation validation = new Validation();
        validation.validate(holder, EXPORT_PATH);
        System.out.println("------------------------------");
        //holder.writeImageserverScript();
        System.out.println("------------------------------");
        //holder.writeAlephScript();
        Map<Integer, Integer> imageStatusCounter = new HashMap<Integer, Integer>();
        for (String url : holder.getImageserverLinkList()) {
            Client client = new ResteasyClientBuilder().build();
            try {
                WebTarget webTarget = client.target(url + "/preview.jpg");
                Response response = webTarget.request().get();
                if (imageStatusCounter.containsKey(new Integer(response.getStatus()))) {
                    Integer counter = imageStatusCounter.get(new Integer(response.getStatus()));
                    counter++;
                } else {
                    imageStatusCounter.put(response.getStatus(), 1);
                }
                client.close();
            } catch (IllegalArgumentException e) {
                // TODO
            }
        }
        //System.out.println("Kontrola imageserveru dobehla");
    }

    //ensure that base is updated
    private void copyBase() throws JSchException, SftpException, IOException {

        File file = new File(EXPORT_PATH);

        if (file.exists()) {
            DateTime modifiedDate = new DateTime(file.lastModified());
            if (file.length() > 0 && modifiedDate.withTimeAtStartOfDay().isAfter(new DateTime().minusDays(1).withTimeAtStartOfDay())) {
                //base on export path is already updated
                return;
            }
        } else {
            file.createNewFile();
        }

        JSch jsch = new JSch();
        Session session;
        if (configuration.getPasswordMarcExport() == null) {
            jsch.addIdentity(configuration.getPathPrivateKey(), configuration.getPrivateKeypassphrase());
            session = jsch.getSession(configuration.getSshUserMarcExport(), configuration.getSshHostMarcExport(), 22);
        } else {
            session = jsch.getSession(configuration.getSshUserMarcExport(), configuration.getSshHostMarcExport(), 22);
            session.setPassword(configuration.getPasswordMarcExport());
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp c = (ChannelSftp) channel;

        InputStream is = c.get(configuration.getPathMarcExport());

        try (FileOutputStream out = new FileOutputStream(EXPORT_PATH)) {
            IOUtils.copy(is, out);
        }
    }
}
