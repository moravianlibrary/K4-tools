package cz.mzk.k4.tools.scripts.my_scripts;

import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.exceptions.K4ToolsException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.utils.fedora.Constants;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.workers.UuidWorker;
import cz.mzk.k4.tools.utils.Script;

import org.fedora.api.RelationshipTuple;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;


//periodical : uuid:da5f3a10-65eb-11e2-9d9f-005056827e52, parents number 1
//        uuid:60253fb0-65eb-11e2-bc24-005056827e51


/**
 * Created by aleksei on 20.7.17.
 */
public class RelationshipStatistic implements Script {

    private static final Logger LOGGER = LogManager.getLogger(RelationshipStatistic.class);
    private final AccessProvider accessProvider = new AccessProvider();
    private final FedoraUtils fedora = new FedoraUtils(accessProvider);

    private Set<String> userModels = null;
    private static final List<String> modelNames = Arrays.asList(
            "monograph", "periodical", "periodicalvolume", "periodicalitem", "page",
            "soundrecording", "internalpart", "article", "manuscript", "archive", "map",
            "supplement", "monographunit", "sheetmusic", "soundunit");

    private final PrintWriter wrongRelationsNumber = new PrintWriter("IO/wrongRelationsNumber.txt");

    private static int objectsWithoutParents = 0;
    private static int objectsHaveMoreParents = 0;
    private static int objectsWithoutChildren = 0;

    private int offset = 0;
    private int numFound = 1;           // some magic

    public RelationshipStatistic() throws FileNotFoundException {
    }

    @Override
    public void run(List<String> args) {

        Collection<String> models;

        if (checkConcreteModels(args)) {
            models = userModels;
        } else {
            models = modelNames;
        }

        for (String model : models) {
            checkObjectsWithModel(model);
        }

        LOGGER.info("Objects without parents: " + objectsWithoutParents);
        LOGGER.info("Objects have more parents: " + objectsHaveMoreParents);
        LOGGER.info("Objects without children: " + objectsWithoutChildren);

        wrongRelationsNumber.close();
    }

    private boolean checkConcreteModels(List<String> args) {
        if (args.size() < 1) {
            return false;
        }
        userModels = new HashSet<String>();
        for (String arg : args) {
            if (!modelNames.contains(arg)) {
                LOGGER.error("Wrong argument!!!");
                System.exit(1);
            }
            userModels.add(arg);
        }
        return true;
    }

    private void checkObjectsWithModel(String model) {
        List<String> objectsUuid = null;
        LOGGER.info("Statistic for model: " + model);
        int counter = 1;
        while (offset < numFound) {
            try {
                objectsUuid = getUuidsByModel(model);
            } catch (SolrServerException e) {
                e.printStackTrace();
                System.exit(2);
            }
            LOGGER.info("Objects: " + objectsUuid.size());
            for (String uuid : objectsUuid) {
                List<String> parents = getParents(uuid);
                List<String> children = getChildren(uuid);
                LOGGER.info("object:" + counter + " uuid: " + uuid);
                LOGGER.info("\tParents number: " + parents.size());
                LOGGER.info("\tChildren number: " + children.size());
                checkRelationsNumber(model, uuid, parents, children);
                counter++;
            }
        }
    }

    private void checkRelationsNumber(String model, String uuid,
                                      List<String> parentsUuids, List<String> childrenUuid) {
        int parentsNumber = parentsUuids.size();
        int childrenNumber = childrenUuid.size();
        switch (model) {
            case "periodicalvolume":
            case "periodicalitem":
            case "monographunit":
            case "internalpart":
            case "supplement":
            case "soundunit":
            case "article":
            case "track":
                if (childrenNumber == 0) {
                    saveUuid(model, uuid, childrenUuid, "children");
                    objectsWithoutChildren++;
                }
            case "page":
                if (parentsNumber != 1) {
                    saveUuid(model, uuid, parentsUuids, "parents");
                    if (parentsNumber > 1)
                        objectsHaveMoreParents++;
                    else
                        objectsWithoutParents++;
                }
                break;
            case "map":
            case "archive":
            case "monograph":
            case "sheetmusic":
            case "periodical":
            case "manuscript":
            case "soundrecording":
                if (parentsNumber > 0) {
                    saveUuid(model, uuid, parentsUuids, "parents");
                    objectsHaveMoreParents++;
                }
                if (childrenNumber == 0) {
                    saveUuid(model, uuid, childrenUuid, "children");
                    objectsWithoutChildren++;
                }
                break;
        }
    }

    private void saveUuid(String model, String uuid, List<String> parentsUuids, String who) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";
        wrongRelationsNumber.println(model + " : " + uuid + ", " + who +" number " + parentsUuids.size());
        for (String puuid : parentsUuids) {
            wrongRelationsNumber.println("\t" + puuid);
        }
        wrongRelationsNumber.println();
        wrongRelationsNumber.flush();
        LOGGER.warn(ANSI_RED + "Wrong " + who + " number! Saved in wrongRelationsNumber.txt" + ANSI_RESET);
    }

    private List<String> getParents(String uuid) {
        List<String> parentsUuid = fedora.getParentUuids(uuid);
        parentsUuid.removeAll(Arrays.asList("", null));
        parentsUuid.remove(uuid);
        return parentsUuid;
    }

    private List<String> getChildren(String uuid) {
        List<String> childrenUuid = new ArrayList<String>();
        List<RelationshipTuple> triplets = fedora.getObjectPids(uuid);
        if (triplets != null) {
            for (RelationshipTuple triplet : triplets) {
                if (triplet.getObject().contains("uuid")
                        && triplet.getObject().contains(Constants.FEDORA_INFO_PREFIX)) {

                    final String childUuid =
                            triplet.getObject().substring((Constants.FEDORA_INFO_PREFIX).length());

                    if (!childUuid.contains("/")) {
                        childrenUuid.add(childUuid);
                    }
                }
            }
        }
        return childrenUuid;
    }

    private List<String> getUuidsByModel(String model) throws SolrServerException {
        String query = "fedora.model:\"" + model + "\"";
        List<String> objectsUuids = solrQuery(query, "PID");
        return objectsUuids;
    }

    // vlastni solrQuery
    // zaskani UUID objektu, urcite, mohlo by byt implementovano pres rozhrani fedory
    // ale objektu je moc, a fedora pri odesilani vsech UUIDicek zaraz
    // vyhazuje chybu Gateway TimeOut, takze je treba ziskavat UUID po castech,
    // proto potrebujeme nejaky offset.
    // Nejsem odbornik na fedoru, nedokazal jsem najit kde je mozne nastavit offset ve
    // fedore, proto delam vyhledavani UUID objektu pres solr.
    // Doufam, ze neni to chybna cesta :)
    private List<String> solrQuery(String query, String fields) throws SolrServerException {
        HttpSolrServer solr = new HttpSolrServer("http://" + accessProvider.getSolrHost() + "/kramerius");
        List<String> retList = new ArrayList<String>();

        SolrQuery parameters = new SolrQuery();
        int cycle = 0;

        // offset < numFound neni-li pocet objectu nasobkem 10 000
        // cycle < 5 je-li pocet objectu > 50 000, zpracovani po castech, aby nezatezovalo pamet
        while (offset < numFound && cycle < 5) {

            parameters.setQuery(query);
            parameters.setFields(fields);
            parameters.set("start", Integer.toString(offset));
            parameters.set("rows", "10000");
            parameters.setRequestHandler("select");

            //LOGGER.info("Calling solr url: " + solr.getBaseURL() + " query: "
            // + parameters.getQuery() + " return field: " + fields);

            QueryResponse response = solr.query(parameters);
            SolrDocumentList results = response.getResults();

            if (results.get(0).get(fields) instanceof String) {
                for (SolrDocument result : results) {
                    retList.add((String) result.get(fields));
                }
            } else if (results.get(0).get(fields) instanceof List) {
                for (SolrDocument result : results) {
                    for (String s : (List<String>) result.get(fields))
                        retList.add(s);
                }
            }

            numFound += results.size();
            offset += 10000;
            cycle++;

            LOGGER.info(numFound + " objects already read...");
        }

        return retList;
    }

    private List<String> fedoraQueryUuids(String model) {
        List<String> retval = new ArrayList<String>();
        fedora.applyToAllUuidOfModel(DigitalObjectModel.parseString(model),
                new UuidWorker(false) {
            @Override
            public void run(String uuid) throws K4ToolsException {
                retval.add(uuid);
            }
        });
        return retval;
    }


    @Override
    public String getUsage() {
        String message = "Script zkouma objekty pozadovaneho typu" +
                "urcuje pocet jeho rodicu a nasledniku, a odhaluje chybne nebo zbytecne vazby.\n" +
                "statistic [model]\n" +
                "modely:\n";
        for (String s : modelNames) {
            message += "\t" + s + "\n";
        }
        return message;
    }
}
