package cz.mzk.k4.tools.utils.fedora;

import javax.xml.namespace.NamespaceContext;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: holmanj
 * Date: 11/29/13
 * Time: 5:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class FedoraNamespaceContext implements NamespaceContext {
    private static final Map<String, String> MAP_PREFIX2URI = new IdentityHashMap<String, String>();
    private static final Map<String, String> MAP_URI2PREFIX = new IdentityHashMap<String, String>();

    static {
        MAP_PREFIX2URI.put("mods", FedoraNamespaces.BIBILO_MODS_URI);
        MAP_PREFIX2URI.put("dc", FedoraNamespaces.DC_NAMESPACE_URI);
        MAP_PREFIX2URI.put("fedora-models", FedoraNamespaces.FEDORA_MODELS_URI);
        MAP_PREFIX2URI.put("kramerius", FedoraNamespaces.KRAMERIUS_URI);
        MAP_PREFIX2URI.put("rdf", FedoraNamespaces.RDF_NAMESPACE_URI);
        MAP_PREFIX2URI.put("oai", FedoraNamespaces.OAI_NAMESPACE_URI);
        MAP_PREFIX2URI.put("sparql", FedoraNamespaces.SPARQL_NAMESPACE_URI);
        MAP_PREFIX2URI.put("apia", FedoraNamespaces.FEDORA_ACCESS_NAMESPACE_URI);
        MAP_PREFIX2URI.put("apim", FedoraNamespaces.FEDORA_MANAGEMENT_NAMESPACE_URI);

        for (Map.Entry<String, String> entry : MAP_PREFIX2URI.entrySet()) {
            MAP_URI2PREFIX.put(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public String getNamespaceURI(String arg0) {
        return MAP_PREFIX2URI.get(arg0.intern());
    }

    @Override
    public String getPrefix(String arg0) {
        return MAP_URI2PREFIX.get(arg0.intern());
    }

    @Override
    public Iterator getPrefixes(String arg0) {
        String prefixInternal = MAP_URI2PREFIX.get(arg0.intern());
        if (prefixInternal != null) {
            return Arrays.asList(prefixInternal).iterator();
        } else {
            return Collections.emptyList().iterator();
        }
    }

    class FedoraNamespaces {
        /**
         * RDF namespace
         */
        public static final String RDF_NAMESPACE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

        /**
         * Our ontology relationship namespace
         */
        public static final String ONTOLOGY_RELATIONSHIP_NAMESPACE_URI = "http://www.nsdl.org/ontologies/relationships#";

        /**
         * Dublin core namespace
         */
        public static final String DC_NAMESPACE_URI = "http://purl.org/dc/elements/1.1/";

        /**
         * Fedora models namespace
         */
        public static final String FEDORA_MODELS_URI = "info:fedora/fedora-system:def/model#";

        public static final String KRAMERIUS_URI = "http://www.nsdl.org/ontologies/relationships#";

        /**
         * Biblio modesl namespace
         */
        public static final String BIBILO_MODS_URI = "http://www.loc.gov/mods/v3";

        /**
         * OAI namespace
         */
        public static final String OAI_NAMESPACE_URI = "http://www.openarchives.org/OAI/2.0/";

        /**
         * Sparql namespace
         */
        public static final String SPARQL_NAMESPACE_URI = "http://www.w3.org/2001/sw/DataAccess/rf1/result";

        /**
         * Namespace used in API-A results
         */
        public static final String FEDORA_ACCESS_NAMESPACE_URI = "http://www.fedora.info/definitions/1/0/access/";


        /**
         * Namespace used in API-M results
         */
        public static final String FEDORA_MANAGEMENT_NAMESPACE_URI = "http://www.fedora.info/definitions/1/0/management/";
    }

}
