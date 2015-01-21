package cz.mzk.k4.client.api;


import javax.ws.rs.client.Client;


import javax.ws.rs.client.WebTarget;

/**
 * Created by rumanekm on 20.1.15.
 */
public class KrameriusClientApi {
    private String krameriusHost;
    private String krameriusUser;
    private String krameriusPassword;

    public KrameriusClientApi(String krameriusHost, String krameriusUser, String krameriusPassword) {
        this.krameriusHost = krameriusHost;
        this.krameriusUser = krameriusUser;
        this.krameriusPassword = krameriusPassword;
    }

    public String getFoxml(String uuid) {
//        Client client = JerseyClientBuilder.newClient();
//        WebTarget target = client.target(krameriusHost + "/search/api/v5.0/");
//        String foxml = target.path("item/" + uuid + "/foxml").request().get(String.class);
//        return foxml;
        throw new IllegalArgumentException("Not implemented");

    }

}
