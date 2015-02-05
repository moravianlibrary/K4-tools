package cz.mzk.k4.tools.fedoraApi;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by rumanekm on 5.2.15.
 */
public class FedoraFactoryService {
    public static RisearchService getService(String url) {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(url).setConverter(new RdfConverter());

        RisearchService risearchService = builder.build().create(RisearchService.class);

        return risearchService;

    }
}
