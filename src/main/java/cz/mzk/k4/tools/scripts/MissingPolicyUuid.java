package cz.mzk.k4.tools.scripts;

import com.sun.jersey.api.client.WebResource;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;

import javax.ws.rs.core.MediaType;
import java.util.List;


/**
 * @author Jan Holman
 *
 */
public class MissingPolicyUuid implements Script {

    /**
     * Vypíše uuid svazků s neznámou dostupností (public/private)
     * @param args
     */
    public void run(List<String> args) {
        AccessProvider accessProvider = new AccessProvider();

        for (int offset = 0; offset < 1081; offset = offset + 20) {
            WebResource resourse = accessProvider.getKrameriusWebResource("/search/r.jsp?fq=dostupnost:%22%22&offset="
                    + offset);
            String html = resourse.accept(MediaType.APPLICATION_XML).get(String.class);

            String[] uuid_lines = html.split("id=\"res_monograph_uuid:");

            for (int i = 1; i < uuid_lines.length; i++) {
                String[] uuids = uuid_lines[i].split("\"");
                System.out.println(uuids[0]);
            }

            // TODO: změna viditelnosti podle roku
        }

    }

    @Override
    public String getUsage() {
        return "uuidBezDostupnosti - Vrátí seznam uuid objektů, u kterých chybí příznak POLICY (public/private)";
    }
}