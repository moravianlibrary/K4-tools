package cz.mzk.k4.tools.workers;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.validators.ArticleValidator;
import org.apache.log4j.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(ValidateWorker.class);
    private FedoraClient fedora;

    private FedoraUtils fedoraUtils;


    //TODO input list with validator objects
    public ValidateWorker(AccessProvider accessProvider) {
        super(false);
        fedoraUtils = new FedoraUtils(accessProvider);
//        try {
//            FedoraCredentials credentials = new FedoraCredentials("http://" + accessProvider.getFedoraHost(),
//                    accessProvider.getFedoraUser(), accessProvider.getFedoraPassword());
//            FedoraClient fedora = new FedoraClient(credentials);
//            FedoraRequest.setDefaultClient(fedora);
//        } catch (MalformedURLException e) {
//            LOGGER.error("Malformed URL to Fedora");
//        }

    }

    @Override
    public void run(String uuid) {
        FedoraResponse response = null;
        ArticleValidator articleValidator = new ArticleValidator();
        try {
            Document relsext = fedoraUtils.getRelsExt(uuid);
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(relsext), new StreamResult(sw));

//            LOGGER.info("Download foxml " + uuid);
//            response = fedora.getDatastream(uuid, "RELS-EXT").execute();
//            String relsext = IOUtils.toString(response.getEntityInputStream());
            if (!articleValidator.validate(sw.toString())) {
                // bohatě by stačilo:
//                if (sw.toString().contains("fedora/null")) {
                LOGGER.warn(uuid + " is corrupted");
            }
        } catch (IOException e) {
            LOGGER.error("IO error " + e);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }


//
//        SupplementValidator supplementValidator = new SupplementValidator();
//
//        if (!supplementValidator.validate(foxml)) {
//            System.out.printf("validation error");
//        }
    }
}