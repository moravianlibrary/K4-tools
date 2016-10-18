package cz.mzk.k4.tools;

import cz.mzk.k4.tools.scripts.*;
import cz.mzk.k4.tools.scripts.lidovky.OdpojeniVadnychClanku;
import cz.mzk.k4.tools.utils.ScriptRunner;
import org.apache.log4j.Logger;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Martin Rumanek
 * @version 30.7.13
 */
public class Tools {
    private static final Logger LOGGER = Logger.getLogger(Tools.class);

    public static void main(String[] args) {
        ScriptRunner runner = new ScriptRunner();

        try {
            runner.register("checkLogs", new CheckLogs());
            runner.register("uuidBezDostupnosti", new MissingPolicyUuid());
            runner.register("spatneZnakyOCR", new FindBadCharacterInOcr(true));
            runner.register("opraveniOdkazuProReplikaci", new RepairLinksForReplication());
            runner.register("vypisVsechnaUuuidModelu", new FindAllDocumentsByModel());
            runner.register("vypisSmutneMonografie", new FindLonelyMonographs());
            runner.register("stavDeleted", new DeletedDocuments());
            runner.register("opravaNahleduPdf", new RegenerateThumbnailPdf());
            runner.register("uuidMetsBalik", new GetUuidFromMetsPackages());
            runner.register("pridatOCR", new AddOcr());
            runner.register("hledaniHaluzi", new WtfSearch());
//        runner.register("kontrolaRajhradu", new RajhradValidate());
//        runner.register("djvuNaJp2", new ConvertDJvuToJp2());
            runner.register("regenerateAudioServer", new RegenerateAudioServer());
            runner.register("stazeniObrazku", new DownloadImages());
            runner.register("importKolekce", new ImportCollection());
            runner.register("getBookOCR", new GetWholeBookOCR());
            runner.register("articleRepair", new OdpojeniVadnychClanku());
            runner.register("solr", new SolrDotaz());
            runner.register("xmlstarlet", new XMLStarlet());
            runner.register("pripojeniPeriodik", new AttachPeriodicalsNDK());
//        runner.register("lidovky", new LnPresunImg());
            runner.register("soundunits", new RepairImgRels());
            runner.register("unindexedFedoraModels", new UnindexedFedoraModels());
            runner.register("hades", new StehovaniHades());
            runner.register("opravitDostupnost", new RepairTrees());
//        runner.register("lidovky2", new LnKonverze());
            runner.register("djvu", new DjvuKonverze());
            runner.register("roots", new GetRoots());
            runner.register("rovnost", new SwitchImages());
            runner.register("batch", new BatchK5Process());
            runner.register("vymenaObrazku", new DjvuVymena());
            runner.register("metadata", new ZmenaMetadat());
            runner.register("ids", new IdentificatorSearch());
            runner.register("sort", new Sorter());
            runner.register("test", new TestScript());
            runner.register("vcrepair", new VCGhosts());
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
            return;
        }

        if (args.length < 1 || args[0] == null) {
            printUsage(runner);
            return;
        } else {
            String scriptName = args[0];
            List<String> params = new ArrayList<String>(Arrays.asList(args));
            params.remove(scriptName);
            try {
                runner.run(scriptName, params);
            } catch (FileNotFoundException e) {
                LOGGER.error(e.getMessage());
                return;
            }
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
