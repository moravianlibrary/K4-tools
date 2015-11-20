package cz.mzk.k5.api.client;

import com.squareup.okhttp.OkHttpClient;
import cz.mzk.k5.api.common.AuthenticationInterceptor;
import cz.mzk.k5.api.common.ClientRemoteErrorHandler;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by holmanj on 8.2.15.
 */
public class KrameriusClientRemoteApiFactory {

    private static final String PROTOCOL = "http://";
    private static final String KRAMERIUS_CLIENT_API = "/search/api/v5.0";

    // TODO: asi lepší mít 1 interface a custom adapter (converter?), který parsuje json, string i xml podle typu dat v response

    //    public static ClientRemoteApiJSON getJsonService(String krameriusHostUrl, String login, String password) {
//
//        AuthorizationInterceptor authInterceptor = new AuthorizationInterceptor(login, password);
//
//        RestAdapter.Builder builder = new RestAdapter.Builder()
//                .setRequestInterceptor(authInterceptor)
//                .setEndpoint("http://" + krameriusHostUrl + "/search/api/v5.0");
//        ClientRemoteApiJSON clientApi = builder.build().create(ClientRemoteApiJSON.class);
//
//        return clientApi;
//
//    }

    public static ClientRemoteApi getClientRemoteApi(String krameriusHostUrl) {
        return getClientRemoteApi(krameriusHostUrl, "", "");
    }

    public static ClientRemoteApi getClientRemoteApi(String krameriusHostUrl, String login, String password) {

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(60, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);

        // hlavičky Authorization a User-Agent (pro identifikaci v logu)
        final AuthenticationInterceptor authInterceptor = new AuthenticationInterceptor(login, password);

        RestAdapter.Builder builder = new RestAdapter.Builder()
                // přidání hlaviček
                .setRequestInterceptor(authInterceptor)
                .setClient(new OkClient(okHttpClient))
                // základ URL
                .setEndpoint(PROTOCOL + krameriusHostUrl + KRAMERIUS_CLIENT_API)
                .setErrorHandler(new ClientRemoteErrorHandler());
        // deserializace defaultně jako JSON
        ClientRemoteApiJSON apiJSON = builder.build().create(ClientRemoteApiJSON.class);

        builder = new RestAdapter.Builder()
                // přidání hlaviček
                .setRequestInterceptor(authInterceptor)
                .setClient(new OkClient(okHttpClient))
                // základ URL
                .setEndpoint(PROTOCOL + krameriusHostUrl + KRAMERIUS_CLIENT_API)
                // xml deserializace (Document)
                .setConverter(new XmlConverter())
                .setErrorHandler(new ClientRemoteErrorHandler());
        ClientRemoteApiXML apiXML = builder.build().create(ClientRemoteApiXML.class);

        builder = new RestAdapter.Builder()
                .setRequestInterceptor(authInterceptor)
                // deserializace jako String
                .setConverter(new StringConverter())
                .setEndpoint(PROTOCOL + krameriusHostUrl + KRAMERIUS_CLIENT_API)
                .setErrorHandler(new ClientRemoteErrorHandler());
        ClientRemoteApiString apiString = builder.build().create(ClientRemoteApiString.class);

        builder = new RestAdapter.Builder()
                .setRequestInterceptor(authInterceptor)
                // bez deserializace - přepošle raw input stream
                .setConverter(new RawConverter())
                .setEndpoint(PROTOCOL + krameriusHostUrl + KRAMERIUS_CLIENT_API)
                .setErrorHandler(new ClientRemoteErrorHandler());
        ClientRemoteApiRaw apiRaw = builder.build().create(ClientRemoteApiRaw.class);

        // spojení do 1 objektu
        ClientRemoteApi api = new ClientRemoteApi(apiJSON, apiString, apiXML, apiRaw);
        return api;
    }
}
