package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.workers.SetPolicyWorker;
import cz.mzk.k4.tools.workers.UuidWorker;
import org.apache.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Jan Holman
 */
public class MissingPolicyUuid implements Script {

    private static final Logger LOGGER = Logger.getLogger(MissingPolicyUuid.class);

    /**
     * Vypíše uuid svazků s neznámou dostupností (public/private)
     *
     * @param args
     */
    public void run(List<String> args) {
        LOGGER.info("Searching for uuids");
        AccessProvider accessProvider = new AccessProvider();
        Client client = ClientBuilder.newClient();
        List<String> uuidList = new ArrayList<String>();

        // get object with no policy
//        for (int offset = 0; offset < 1081; offset = offset + 20) {
        int offset = 0;
            String url = "http://" + accessProvider.getKrameriusHost()
                    + "/search/r.jsp?fq=dostupnost:%22%22&offset="
                    + offset;

            LOGGER.debug("Getting uuids from " + url);
            WebTarget resource = client.target(url);
            String html = resource.request(MediaType.APPLICATION_XML).get(String.class);

            // parse response html
            // String[] uuid_lines = html.split("id=\"res_monograph_uuid:");
            String[] uuid_lines = html.split("id=\"res_periodical_uuid:");
            LOGGER.debug("počet: " + uuid_lines.length);
            for (int i = 1; i < uuid_lines.length; i++) {
                String[] uuids = uuid_lines[i].split("\"");
                System.out.println(uuids[0]);
                uuidList.add(uuids[0]);
            }
//        }

        // SetPolicyWorker
        if (args.contains("writeEnabled")) {
            UuidWorker worker = new SetPolicyWorker(true, accessProvider);
            for (String uuid : uuidList) {
                worker.run(uuid);
            }
        }
    }

    @Override
    public String getUsage() {
        return "uuidBezDostupnosti - Vrátí seznam uuid objektů, u kterých chybí příznak POLICY (public/private)";
    }
}