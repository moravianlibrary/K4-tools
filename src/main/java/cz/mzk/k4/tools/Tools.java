package cz.mzk.k4.tools;

import cz.mzk.k4.tools.scripts.CheckLogs;
import cz.mzk.k4.tools.scripts.FindAllDocumentsByModel;
import cz.mzk.k4.tools.scripts.FindBadCharacterInOcr;
import cz.mzk.k4.tools.scripts.MissingPolicyUuid;
import cz.mzk.k4.tools.scripts.RepairLinksForReplication;
import cz.mzk.k4.tools.utils.ScriptRunner;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.utils.fedoraUtils.domain.DigitalObjectModel;

/**
 * @author Martin Rumanek
 * @version 30.7.13
 */
public class Tools {

    public static void main(String[] args) {
        ScriptRunner runner = new ScriptRunner();
        runner.register("checkLogs", new CheckLogs());
        runner.register("uuidBezDostupnosti", new MissingPolicyUuid());
        runner.register("spatneZnakyOCR", new FindBadCharacterInOcr());
        runner.register("opraveniOdkazuProReplikaci", new RepairLinksForReplication());
        runner.register("vypisVsechnaUuuidModelu", new FindAllDocumentsByModel());

        if (args.length < 1 || args[0] == null) {
            printUsage(runner);
            return;
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
