package cz.mzk.k4.tools;

import cz.mzk.k4.tools.scripts.CheckLogs;
import cz.mzk.k4.tools.scripts.FindAllDocumentsFromModel;
import cz.mzk.k4.tools.scripts.FindBadCharacterInOcr;
import cz.mzk.k4.tools.scripts.MissingPolicyUuid;
import cz.mzk.k4.tools.scripts.RepairLinksForReplication;

/**
 * @author Martin Rumanek
 * @version 30.7.13
 */
public class Tools {
    public static void main(String[] args) {
        if (args.length < 1 || args[0] == null) {
            printUsage();
            return;
        }

        if (args[0].equals("checkLogs")) {
            /**
             * Projde logy procesů v K4 a vypíše ty,
             * které mají stav FINISHED a zároveň neprázdný chybový výstup
             * @param ~/k4_tools_config.properties
             * @return log v {projekt}/logs/checkK4Logs.txt + sout
             * @return nalezene chyby v {projekt}/logs/{uuid}.txt
             * @author Jan Holman
             */
            CheckLogs.run();

        } else if (args[0].equals("uuidBezDostupnosti")) {
            /**
             * Vypíše uuid svazků s neznámou dostupností
             * (ani public ani private)
             * @return seznam uuid v sout
             * @author Jan Holman
             */
            MissingPolicyUuid.run();

        } else if (args[0].equals("spatneZnakyOCR")) {
            /**
             * Projde u dokumentu všechny rekurzivně OCR u všech stran, v případě nevalidních znaků (XML) se zalogují
             * uuid těchto stran. V případě parametru "opravit" jsou nevalidní znaky odstraněny.
             * které mají stav FINISHED a zároveň neprázdný chybový výstup
             *  @param ~/k4_tools_config.properties
             *  @param uuid
             *  @param opravit
             *  @author Martin Rumánek
             */
            if (args.length > 2 && "opravit".equals(args[1])) {
                //
                FindBadCharacterInOcr.run(args[1], true);
            } else {
                //
                FindBadCharacterInOcr.run(args[1]);
            }

        } else if (args[0].equals("opraveniOdkazuProReplikaci")) {
            /**
             * replikace z NK obsahovala špatné cesty k OCR a obrázkům, tyto cesty jsou zkontrolovány
             * a pokud jsou v jiné úrovni jsou odkazy upraveny
             * @param path
             * @author Martin Rumánek
             */
            RepairLinksForReplication.run(args[1]);

        } else if (args[0].equals("vypisVsechnaUUID")) {
            FindAllDocumentsFromModel.run();
        }else {
            printUsage();
        }


    }

    private static void printUsage() {
        System.out.println("Mozne parametry:");
        System.out.println("checkLogs");
        System.out.println("uuidBezDostupnosti");
        System.out.println("spatneZnakyOCR"); // "opravit"?
        System.out.println("opraveniOdkazuProReplikaci");

    }

}
