package cz.mzk.k4.tools;

import cz.mzk.k4.tools.scripts.*;
import cz.mzk.k4.tools.utils.ScriptRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Martin Rumanek
 * @version 30.7.13
 */
public class Tools {

    public static void main(String[] args) {
        ScriptRunner runner = new ScriptRunner();
        runner.register("checkLogs", new CheckLogs());
        runner.register("uuidBezDostupnosti", new MissingPolicyUuid());
        runner.register("spatneZnakyOCR", new FindBadCharacterInOcr(false));
        runner.register("opraveniOdkazuProReplikaci", new RepairLinksForReplication());
        runner.register("vypisVsechnaUuuidModelu", new FindAllDocumentsByModel());
        runner.register("vypisBezdetneMonografie", new FindChildlessMonographs());
        runner.register("stavDeleted", new DeletedDocuments());

        if (args.length < 1 || args[0] == null) {
            printUsage(runner);
            return;
        } else {
            String scriptName = args[0];
            List<String> params = new ArrayList<String>(Arrays.asList(args));
            params.remove(scriptName);
            runner.run(scriptName, params);
        }
    }

    private static void printUsage(ScriptRunner runner) {
        System.out.println("K4-tools je souhrn nástrojů, které pomáhají hromadně vyhledat/opravit data s Krameriem související");
        System.out.println("Použítí: java -jar tools.jar skript [parametry...]");
        System.out.println("Seznam dostupných skriptů:");

        for (String name : runner.getAllScriptsName()) {
            System.out.println(name);
        }
    }

}
