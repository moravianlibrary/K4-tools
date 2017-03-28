package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by rumanekm on 20.1.15.
 */
public class ValidateWorker extends UuidWorker {

    private static final Logger LOGGER = LogManager.getLogger(ValidateWorker.class);
    private FedoraUtils fedoraUtils;

    public ValidateWorker(AccessProvider accessProvider) {
        super(false);
        fedoraUtils = new FedoraUtils(accessProvider);
    }

    @Override
    public void run(String uuid) {
        try {
            Document relsext = fedoraUtils.getRelsExt(uuid);
            LOGGER.info("Download RDF " + uuid);

            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(relsext), new StreamResult(sw));

            if (sw.toString().contains("fedora/null")) {
                LOGGER.warn(uuid + " is corrupted");
            }
        } catch (IOException e) {
            LOGGER.error("IO error " + e);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}