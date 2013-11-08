package cz.mzk.k4.tools.scripts;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import cz.mzk.k4.tools.utils.Script;

import javax.ws.rs.core.MediaType;
import java.util.List;


/**
 * @author Jan Holman
 *         Vypíše uuid svazků s neznámou dostupností
 */
public class MissingPolicyUuid implements Script {


    public void run(List<String> args) {
        Client client = Client.create();

        for (int offset = 0; offset < 1081; offset = offset + 20) {
            WebResource resourse = client
                    .resource("http://krameriusndktest.mzk.cz/search/r.jsp?fq=dostupnost:%22%22&offset="
                            + offset);
            String html = resourse.accept(MediaType.APPLICATION_XML).get(
                    String.class);

            String[] uuid_lines = html.split("id=\"res_monograph_uuid:");

            for (int i = 1; i < uuid_lines.length; i++) {
                String[] uuids = uuid_lines[i].split("\"");
                System.out.println(uuids[0]);
            }
        }

    }

    @Override
    public String getUsage() {
        return null;
    }
}