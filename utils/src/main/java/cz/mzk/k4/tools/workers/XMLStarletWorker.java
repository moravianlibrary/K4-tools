package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.commons.io.IOUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rumanekm on 3/10/15.
 */
public class XMLStarletWorker extends UuidWorker {

    private FedoraUtils fedoraUtils;
    private List<String> args;
    private static final Logger LOGGER = LogManager.getLogger(XMLStarletWorker.class);

    public XMLStarletWorker(FedoraUtils fedoraUtils, List<String> args) {
        super(false);
        this.fedoraUtils = fedoraUtils;
        this.args = args;
    }

    @Override
    public void run(String uuid) {
        try {
            Document relsext = fedoraUtils.getRelsExt(uuid);
            Source xmlSource = new DOMSource(relsext);
            File fstream = File.createTempFile("RelsExt",".xml");
            Result outputTarget = new StreamResult(new FileOutputStream(fstream));
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
            List<String> cmdParams = new ArrayList<String>();
            cmdParams.add("xmlstarlet");
            for (String arg : args) {
                cmdParams.add(arg);
            }
            cmdParams.add(fstream.getAbsolutePath());
            Process process = new ProcessBuilder(cmdParams).start();
            String xmlStarletOutput = IOUtils.toString(process.getInputStream());
            LOGGER.info(uuid + " - " + xmlStarletOutput);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }
}
