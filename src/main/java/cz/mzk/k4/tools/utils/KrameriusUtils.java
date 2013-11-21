package cz.mzk.k4.tools.utils;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 11/21/13
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class KrameriusUtils {

    private static final Logger LOGGER = Logger.getLogger(KrameriusUtils.class);
    private AccessProvider accessProvider;

    public KrameriusUtils(AccessProvider accessProvider) {
        this.accessProvider = accessProvider;
    }

    /**
     * Naplánuje proces mazání dokumentu (mazání provede Kramerius, vč. rekurze)
     * cz.incad.kramerius.service.impl.DeleteServiceImpl
     * @param uuid
     */
    public void exterminate(String uuid) {
        if (!uuid.contains("uuid")) {
            uuid = "uuid:" + uuid;
        }

        // {"parameters":["uuid:...","uuid:..."]}
        String json = "{\"parameters\":[\"" + uuid + "\",\"" + uuid + "\"]}";
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("def", "delete");
        WebResource resource = accessProvider.getKrameriusWebResource("");
        ClientResponse response = resource.queryParams(queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(json, MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);


        if (response.getStatus() == 201) {
            LOGGER.info("Deleting object " + uuid);
        } else {
            LOGGER.error("An error occured while planning deletion of document " + uuid + " (code " + response.getStatus() + ")");
        }
    }

    public void setPrivate(String uuid) {

    }

    public void setPublic(String uuid) {

    }
}
