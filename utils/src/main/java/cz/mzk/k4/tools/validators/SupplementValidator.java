package cz.mzk.k4.tools.validators;

import cz.mzk.k4.client.api.KrameriusClientApi;
import cz.mzk.k4.tools.utils.AccessProvider;

/**
 * Created by rumanekm on 20.1.15.
 */
public class SupplementValidator {
    private AccessProvider accessProvider;

    public SupplementValidator(AccessProvider accessProvider) {
        this.accessProvider = accessProvider;
    }

    public boolean validate(String uuid) {
        KrameriusClientApi krameriusApi = new KrameriusClientApi(accessProvider.getKrameriusHost(),
                accessProvider.getKrameriusUser(),
                accessProvider.getKrameriusPassword());
        String foxml = krameriusApi.getFoxml(uuid);
        System.out.println(foxml);
        //TODO validate foxml
        return true;
    }

}
