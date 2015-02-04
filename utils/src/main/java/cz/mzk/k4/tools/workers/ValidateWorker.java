package cz.mzk.k4.tools.workers;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.client.request.FedoraRequest;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.validators.ArticleValidator;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by rumanekm on 20.1.15.
 */
public class ValidateWorker extends UuidWorker {

    private static final Logger LOGGER = Logger.getLogger(ValidateWorker.class);
    private FedoraClient fedora;


    //TODO input list with validator objects
    public ValidateWorker(AccessProvider accessProvider) {
        super(false);

        try {
            FedoraCredentials credentials = new FedoraCredentials("http://" + accessProvider.getFedoraHost(),
                    accessProvider.getFedoraUser(), accessProvider.getFedoraPassword());
            FedoraClient fedora = new FedoraClient(credentials);
            FedoraRequest.setDefaultClient(fedora);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed URL to Fedora");
        }

    }

    @Override
    public void run(String uuid) {
        FedoraResponse response = null;
        ArticleValidator articleValidator = new ArticleValidator();
        try {
            LOGGER.info("Download foxml " + uuid);
            response = fedora.getObjectXML(uuid).execute();
            String xml = IOUtils.toString(response.getEntityInputStream());
            if (!articleValidator.validate(xml)) {
                LOGGER.warn(uuid + " is corrupted");
            }
        } catch (FedoraClientException e) {
            LOGGER.error("Fedora client exception");
        } catch (IOException e) {
            LOGGER.error("IO error " + e);
        }


//
//        SupplementValidator supplementValidator = new SupplementValidator();
//
//        if (!supplementValidator.validate(foxml)) {
//            System.out.printf("validation error");
//        }
    }
}
