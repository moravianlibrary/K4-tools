package cz.mzk.k4.tools.scripts;

import com.sun.jersey.api.client.WebResource;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 12/5/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestScript implements Script {

    private static FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());

    @Override
    public void run(List<String> args) {

        String subjectPid = args.get(0);
        AccessProvider accessProvider = new AccessProvider();
        String query = "%3Cinfo:fedora/" + subjectPid + "%3E%20*%20*";
        WebResource resource = accessProvider.getFedoraWebResource("/risearch?type=triples&lang=spo&format=N-Triples&query="
                + query);
        String result = resource.get(String.class);

        if (result.equals("")) {
            System.out.println(subjectPid);
        }
    }

    @Override
    public String getUsage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
