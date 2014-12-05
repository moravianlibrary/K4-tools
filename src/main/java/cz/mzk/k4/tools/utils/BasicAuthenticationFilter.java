//package cz.mzk.k4.tools.utils;
//
//import org.glassfish.jersey.client.ClientRequest;
//import org.glassfish.jersey.client.ClientResponse;
//import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
//import org.glassfish.jersey.internal.util.Base64;
//
//import javax.ws.rs.client.ClientRequestContext;
//import javax.ws.rs.client.ClientRequestFilter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author pavels
// *
// */
//public class BasicAuthenticationFilter extends HttpAuthenticationFeature {
//
//    public BasicAuthenticationFilter(final String username, final String password) {
//        this.username = username;
//        this.password = password;
//    }
//
//    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
//
//        // encode the password
//        byte[] encoded = Base64.encode((username + ":" + password).getBytes());
//
//        // add the header
//        List<Object> headerValue = new ArrayList<Object>();
//        headerValue.add("Basic " + new String(encoded));
//        clientRequest.getMetadata().put("Authorization", headerValue);
//
//        return getNext().handle(clientRequest);
//    }
//
//    private String username;
//    private String password;
//
//}
