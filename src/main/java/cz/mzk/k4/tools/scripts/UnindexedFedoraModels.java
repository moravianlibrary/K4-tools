package cz.mzk.k4.tools.scripts;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k4.tools.workers.XMLStarletWorker;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by rumanekm on 3/10/15.
 */
public class UnindexedFedoraModels implements Script {

    private AccessProvider accessProvider = new AccessProvider();
    private FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);
    public static final Logger LOGGER = Logger.getLogger(UnindexedFedoraModels.class);

    @Override
    public void run(List<String> args) {
        if (args.size() > 0) {
            if (args.get(args.size() - 1).startsWith("model:")) {
                DigitalObjectModel model = DigitalObjectModel.parseString(args.get(args.size() - 1).substring(6));
                fedoraUtils.applyToAllUuidOfModel(model, new UuidWorker(true) {
                    @java.lang.Override
                    public void run(String uuid) {
                        //this is not a API! reverse engineering from indexed models in kramerius
                        WebResource webResource = accessProvider.getKrameriusWebResource("/inc/admin/_indexer_check.jsp?pid=" + uuid);
                        ClientResponse response = webResource.get(ClientResponse.class);
                        if (response.getStatus() != 200) {
                            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
                        }

                        String output = response.getEntity(String.class);
                        if (output.startsWith(Integer.valueOf(0).toString())) {
                            System.out.println(uuid);
                        }
                    }
                });
            }
        }
    }


    @Override
    public String getUsage() {
        return "unindexedFedoraModels\n" +
                "na výstup vrátí uuid všech nezaindexovaných dokumentů konkrétního modelu\n" +
                "Argument: model:* (např model:monograph)";
    }
}
