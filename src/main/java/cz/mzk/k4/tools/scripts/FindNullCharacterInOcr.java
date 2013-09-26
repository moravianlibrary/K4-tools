package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.fedoraUtils.domain.DigitalObjectModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 9/18/13
 */
public class FindNullCharacterInOcr {

    private static FedoraUtils fu = new FedoraUtils();

    public static void run() {

        List<String> list;
        try {
            list = getChildren("uuid:ae864f11-435d-11dd-b505-00145e5790ea", new ArrayList<String>());

            for (String uuid: list) {
                System.out.println(fu.getOcr(uuid));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static List<String> getChildren(String uuid, List<String> uuidList) throws IOException {
        if (DigitalObjectModel.PAGE.equals(FedoraUtils.getModel(uuid))) {
            uuidList.add(uuid);
            findBadCharacter(fu.getOcr(uuid));
        }
        DigitalObjectModel parentModel = null;
        ArrayList<ArrayList<String>> children = fu.getAllChildren(uuid);

        if (children != null) {
            for (ArrayList<String> child : children) {
                getChildren(child.get(0), uuidList);
            }
        }

        return uuidList;
    }

    private static void findBadCharacter(String text) {
        for (char ch : text.toCharArray()) {
            if (ch == '\0xdafb') {
                System.out.println("GOTCHA");
            }

        }
    }
}
