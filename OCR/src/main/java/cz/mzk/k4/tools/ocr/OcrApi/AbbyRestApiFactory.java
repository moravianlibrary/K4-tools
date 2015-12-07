package cz.mzk.k4.tools.ocr.OcrApi;

import com.squareup.okhttp.OkHttpClient;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by holmanj on 16.6.15.
 */
public class AbbyRestApiFactory {

    private static final String PROTOCOL = "http://";

    public static AbbyRestApi getAbbyRestApi(String abbyUrl) {

        final OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(60, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(PROTOCOL + abbyUrl)
                .setClient(new OkClient(okHttpClient))
                .setErrorHandler(new AbbyRestErrorHandler());
        AbbyRestApiJson apiJSON = builder.build().create(AbbyRestApiJson.class);

        builder = new RestAdapter.Builder()
                .setConverter(new StringConverter())
                .setEndpoint(PROTOCOL + abbyUrl)
                .setClient(new OkClient(okHttpClient))
                .setErrorHandler(new AbbyRestErrorHandler());
        AbbyRestApiString apiString = builder.build().create(AbbyRestApiString.class);


        AbbyRestApi api = new AbbyRestApi(apiJSON, apiString);
        return api;
    }
}
