package cz.mzk.k4.tools.fedoraApi;

import retrofit.RestAdapter;

/**
 * Created by rumanekm on 5.2.15.
 */
public class FedoraFactoryService {

    private static final String PROTOCOL = "http://";

    public static RisearchService getService(String url) {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(PROTOCOL + url).setConverter(new RdfConverter());

        RisearchService risearchService = builder.build().create(RisearchService.class);

        return risearchService;

    }
}
