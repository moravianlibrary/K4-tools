package cz.mzk.k4.tools;

//import cz.mzk.k4.tools.scripts.FindBadCharacterInOcr;
//import cz.mzk.k4.tools.scripts.RepairLinksForReplication;
//import cz.mzk.k4.tools.scripts.Search;

import cz.mzk.k4.tools.scripts.CheckLogs;
import cz.mzk.k4.tools.scripts.FindBadCharacterInOcr;
import cz.mzk.k4.tools.scripts.RepairLinksForReplication;
import cz.mzk.k4.tools.scripts.Search;

/**
 * @author: Martin Rumanek
 * @version: 30.7.13
 */
public class Tools {
    public static void main(String[] args) {
        if (args.length < 1 || args[0] == null) {
            printUsage();
            return;
        }

        if (args[0].equals("checklogs")) {
            CheckLogs.run();
        } else if (args[0].equals("search")) {
            Search.run();
        } else if (args[0].equals("spatneZnakyOCR")) {
            if (args.length > 2 && "opravit".equals(args[1])) {
                FindBadCharacterInOcr.run(args[1], true);
            } else {
                FindBadCharacterInOcr.run(args[1]);
            }
        } else if (args[0].equals("opraveniOdkazuProReplikaci")) {
            RepairLinksForReplication.run(args[1]);
        }


    }

    private static void printUsage() {
        System.out.println("Mozne parametry:");
        //System.out.println("checkLogs");
        System.out.println("search");
        System.out.println("opraveniOdkazuProReplikaci");

    }

}
