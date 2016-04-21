package cz.mzk.k4.tools.utils.fedora;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import cz.mzk.k4.tools.providers.Provider;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.domain.FedoraNamespaces;
import cz.mzk.k4.tools.utils.exception.ConnectionException;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.exception.LexerException;
import cz.mzk.k4.tools.utils.util.PIDParser;
import cz.mzk.k4.tools.utils.util.XMLUtils;
import cz.mzk.k4.tools.workers.UuidWorker;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.fedora.api.DatastreamDef;
import org.fedora.api.RelationshipTuple;
import org.jsoup.Jsoup;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author: Martin Rumanek, incad
 * @version: 9/23/13
 */
public class FedoraUtils {

    private static final Logger LOGGER = Logger.getLogger(FedoraUtils.class);
    private AccessProvider accessProvider;

    public FedoraUtils(AccessProvider accessProvider) {
        this.accessProvider = accessProvider;
    }

    /**
     * @param model  - např. DigitalObjectModel.MONOGRAPH
     * @param worker
     */
    public void applyToAllUuidOfModel(DigitalObjectModel model, final UuidWorker worker) {
        applyToAllUuidOfModel(model, worker, 1);
    }

    /**
     * @param model
     * @param worker
     * @param maxThreads
     */
    public void applyToAllUuidOfModel(DigitalObjectModel model, final UuidWorker worker, Integer maxThreads) {
        List<RelationshipTuple> triplets = getObjectPidsFromModel(model);
        applyToAllUuid(triplets, worker, maxThreads);
    }

    /**
     * @param worker
     */
    public void applyToAllUuidOfStateDeleted(final UuidWorker worker) {
        List<RelationshipTuple> triplets = null;
        try {
            triplets = getObjectsPidsStateDeleted();
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unsupported encoding");
        }
        applyToAllUuid(triplets, worker, 1);
    }

    /**
     * @param triplets
     * @param worker
     */
    public void applyToAllUuid(List<RelationshipTuple> triplets, final UuidWorker worker) {
        applyToAllUuid(triplets, worker, 1);
    }

    /**
     * @param queue  Uuid provider
     * @param worker Uuid worker
     */
    public void applyToAllUuid(final Provider queue, final UuidWorker worker) {
        while (true) {
            try {
                worker.run(queue.take());
            } catch (InterruptedException e) {
                LOGGER.error("K4 tools was interrupted");
            } catch (FileNotFoundException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    /**
     * @param triplets
     * @param worker
     * @param maxThreads
     */
    public void applyToAllUuid(List<RelationshipTuple> triplets, final UuidWorker worker, Integer maxThreads) {

        final Semaphore semaphore = new Semaphore(maxThreads);

        if (triplets != null) {
            for (final RelationshipTuple triplet : triplets) {
                if (triplet.getSubject().contains("uuid")
                        && triplet.getSubject().contains(Constants.FEDORA_INFO_PREFIX)) {

                    final String childUuid =
                            triplet.getSubject().substring((Constants.FEDORA_INFO_PREFIX).length());

                    if (!childUuid.contains("/")) {
                        try {
                            semaphore.acquire();
                            new Thread() {
                                public void run() {
                                    LOGGER.debug("Worker is running on " + childUuid);
                                    try {
                                        worker.run(childUuid);
                                    } catch (FileNotFoundException e) {
                                        LOGGER.error(e.getMessage());
                                    }
                                    semaphore.release();
                                }
                            }.start();
                        } catch (InterruptedException e) {
                            semaphore.release();
                            LOGGER.error("Worker on " + childUuid + " was interrupted");
                        }
                    }
                }
            }
        }
    }

    /**
     * @param uuid
     * @return
     */
    @SuppressWarnings("serial")
    public ArrayList<ArrayList<String>> getAllChildren(String uuid) {
        List<RelationshipTuple> triplets = getObjectPids(uuid);
        ArrayList<ArrayList<String>> children = new ArrayList<ArrayList<String>>();

        if (triplets != null) {
            for (final RelationshipTuple triplet : triplets) {
                if (triplet.getObject().contains("uuid")
                        && triplet.getObject().contains(Constants.FEDORA_INFO_PREFIX)) {

                    final String childUuid =
                            triplet.getObject().substring((Constants.FEDORA_INFO_PREFIX).length());

                    if (!childUuid.contains("/")) {
                        children.add(new ArrayList<String>() {

                            {
                                add(childUuid);
                                add(triplet.getPredicate());
                            }
                        });
                    }
                }
            }
        }
        return children;
    }

    /**
     * @param uuid
     * @param model
     * @return
     * @throws IOException
     */
    public List<String> getChildrenUuids(String uuid, DigitalObjectModel model) {
        return getChildrenUuids(uuid, new ArrayList<String>(), model);
    }

    /**
     * @param uuid
     * @param uuidList
     * @param model
     * @return
     * @throws IOException
     */
    private List<String> getChildrenUuids(String uuid, List<String> uuidList, DigitalObjectModel model) {
        try {
            if (model.equals(getModel(uuid))) {
                uuidList.add(uuid);
                LOGGER.debug("Adding " + uuid);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());  // zapsat i nadřazené uuid?   - dá se najít přes risearch
            return uuidList;
        }
        ArrayList<ArrayList<String>> children = getAllChildren(uuid);

        if (children != null) {
            for (ArrayList<String> child : children) {
                getChildrenUuids(child.get(0), uuidList, model);
            }
        }

        return uuidList;
    }

    /**
     * Projde celý foxml strom a vypíše chyby (vazby, ke kterým chybí objekt)
     *
     * @param uuid
     * @return
     * @throws IOException
     */
    public void checkChildrenExistence(String uuid) {
        try {
            getModel(uuid); // vytáhnutí čehokoliv z fedory - ověří existenci
            LOGGER.debug(uuid + " existuje");
        } catch (IOException e) {
            // TODO: ?
            // objekt není ve fedoře, ale už je zalogování z nižší úrovně
            // zapsat i nadřazené uuid?   - dá se najít přes risearch
        }

        ArrayList<ArrayList<String>> children = getAllChildren(uuid);
        if (children != null) {
            for (ArrayList<String> child : children) {
                checkChildrenExistence(child.get(0));
            }
        }
    }

    /**
     * Projde celý foxml strom a vypíše chyby (vazby, ke kterým chybí objekt)
     *
     * @param uuid
     * @return
     * @throws IOException
     */
    public Boolean checkChildrenAndOcrExistence(String uuid) {
        Boolean containsOcr = null;
        DigitalObjectModel model = null;
        try {
            model = getModel(uuid); // vytáhnutí čehokoliv z fedory - ověří existenci
            LOGGER.debug(uuid + " existuje");
        } catch (IOException e) {
            // TODO: ?
            // objekt není ve fedoře, ale už je zalogování z nižší úrovně
            // zapsat i nadřazené uuid?   - dá se najít přes risearch
        }

        if (model.equals(DigitalObjectModel.PAGE)) {
            containsOcr = (getOcr(uuid) != null);
        } else {
            // TODO: může mít OCR jen model PAGE?
        }

        ArrayList<ArrayList<String>> children = getAllChildren(uuid);
        if (children != null) {
            for (ArrayList<String> child : children) {
                Boolean childResult = checkChildrenAndOcrExistence(child.get(0));
                if (childResult != null && containsOcr != null) {
                    if (!containsOcr.equals(childResult)) {
                        LOGGER.error("OCR problém " + uuid);
                    }
                } else {
                    containsOcr = childResult;
                }
            }
            if (model.equals(DigitalObjectModel.PERIODICALVOLUME)) {
                LOGGER.info("Prohledán ročník " + accessProvider.getFedoraAPIA().getObjectProfile(uuid, null).getObjLabel());
            }
            return containsOcr;
        }
        // pro vyšší úrovně bez dětí (return false)
        return null;
    }

    /**
     * @param uuid
     * @return
     * @throws IOException
     */
    public List<String> getChildrenUuids(String uuid) throws IOException {
        List<String> list = getChildrenUuids(uuid, new ArrayList<String>());
        list.remove(uuid);
        return list;
    }

    /**
     * @param uuid
     * @param uuidList
     * @return
     * @throws IOException
     */
    private List<String> getChildrenUuids(String uuid, List<String> uuidList) throws IOException {
        uuidList.add(uuid);
        ArrayList<ArrayList<String>> children = getAllChildren(uuid);
        if (children != null) {
            for (ArrayList<String> child : children) {
                getChildrenUuids(child.get(0), uuidList);
            }
        }
        return uuidList;
    }

    /**
     * Gets the object pids.
     *
     * @param subjectPid the subject pid
     * @return the object pids
     */
    public List<RelationshipTuple> getObjectPids(String subjectPid) {
        // <info:fedora/[uuid:...]> * *
        return getSubjectOrObjectPids("%3Cinfo:fedora/" + subjectPid + "%3E%20*%20*");
    }

    /**
     * @param model
     * @return
     */
    private List<RelationshipTuple> getObjectPidsFromModel(DigitalObjectModel model) {
        // * * <info:fedora//model:[model]>
        return getSubjectOrObjectPids("%20*%20*%20%3Cinfo:fedora/model:" + model.getValue() + "%3E");
    }

    /**
     * @param uuid
     * @return
     */
    private List<RelationshipTuple> getPagesOfRootUuid(String uuid) {
        // <info:fedora/uuid:542e41d0-dd86-11e2-9923-005056827e52> <http://www.nsdl.org/ontologies/relationships#hasPage> *
        return getSubjectOrObjectPids("%20*%20*%20%3Cinfo:fedora/" + uuid + "%3E");
    }

    /**
     * @return
     * @throws UnsupportedEncodingException
     */
    private List<RelationshipTuple> getObjectsPidsStateDeleted() throws UnsupportedEncodingException {
        return getSubjectOrObjectPids(
                URLEncoder.encode("* <info:fedora/fedora-system:def/model#state> <info:fedora/fedora-system:def/model#Deleted>", "UTF-8"));
    }

    @Deprecated
    // chro vraci cely resource jako string
    public String getAllRelationships(String uuid) {  // plain string returned from risearch
        String query = "%3Cinfo:fedora/" + uuid + "%3E%20*%20*";
        WebResource resource = accessProvider.getFedoraWebResource("/risearch?type=triples&lang=spo&format=N-Triples&query="
                + query);
        String result = resource.get(String.class);
        return result;
    }

    public List<String> getParentUuids(String childUuid) {
        String query = "*%20*%20%3Cinfo:fedora/" + childUuid + "%3E";
        WebResource resource = accessProvider.getFedoraWebResource("/risearch?type=triples&lang=spo&format=N-Triples&query="
                + query);
        List<String> parents = new ArrayList<>();
        String[] result = resource.get(String.class).split("\n");
        for (int i = 0; i < result.length; i++) {
            // <info:fedora/uuid:b80668e6-435d-11dd-b505-00145e5790ea>
            String parentUuid = result[i].split(" ")[0];
            // uuid:b80668e6-435d-11dd-b505-00145e5790ea>
            parentUuid = parentUuid.replace("<info:fedora/", "");
            // uuid:b80668e6-435d-11dd-b505-00145e5790ea
            parentUuid = parentUuid.replace(">", "");
            parents.add(parentUuid);
        }
        return parents;
    }

    /**
     * @param query
     * @return
     */
    public List<RelationshipTuple> getSubjectOrObjectPids(String query) {
        List<RelationshipTuple> retval = new ArrayList<RelationshipTuple>();

        WebResource resource = accessProvider.getFedoraWebResource("/risearch?type=triples&lang=spo&format=N-Triples&query="
                + query);
        String result = resource.get(String.class);
        String[] lines = result.split("\n");
        for (String line : lines) {
//            String[] tokens = line.split(" "); // platná hodnota je i " ", ale to by se rozdělilo na 2x ", nebyl by to triplet
//            String[] tokens = line.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)"); //split on the space only if that comma has zero, or an even number of quotes ahead of it. - nefunguje, když je např. v title lichý počet "
            String[] tokens = line.split(" (?=([<*>]))"); // rozdělit kolem mezer, za kterýma následuje text ve tvaru <..>
            if (tokens.length == 2) {
                String[] rest;
                if (tokens[1].contains("<http://www.w3.org/2001/XMLSchema#dateTime>")) {
                    // 2. token je ve tvaru <info:fedora/fedora-system:def/view#lastModifiedDate> "2011-08-10T09:19:13.641Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
                    tokens[1] = tokens[1].replace("^^<http://www.w3.org/2001/XMLSchema#dateTime>", "");
                }
                // 2. token by měl být ve tvaru: <http://purl.org/dc/elements/1.1/language> "cze" .
                rest = new String[2];
                rest[0] = tokens[1].substring(0, tokens[1].indexOf(" ")); // všechno po první mezeru (bez ní)
                rest[1] = tokens[1].substring(tokens[1].indexOf(" ") + 1); // všechno od první mezery (bez ní)
                String[] tokensCorrected = new String[3];
                tokensCorrected[0] = tokens[0]; // objekt: <...>
                tokensCorrected[1] = rest[0];   // predikát <...>
                tokensCorrected[2] = rest[1];   // subjekt "..."
                tokens = tokensCorrected;
            }
            tokens[2] = tokens[2].substring(0, tokens[2].length() - 2); // odstranění " ." z konce

            if (tokens.length < 3) continue;
            try {
                RelationshipTuple tuple = new RelationshipTuple();
                tuple.setSubject(tokens[0].substring(1, tokens[0].length() - 1));
                tuple.setPredicate(tokens[1].substring(1, tokens[1].length() - 1));
                tuple.setObject(tokens[2].substring(1, tokens[2].length() - 1));
                tuple.setIsLiteral(false);
                retval.add(tuple);
            } catch (Exception ex) {
                LOGGER.warn("Problem parsing RDF, skipping line:" + Arrays.toString(tokens) + " : " + ex);
            }
        }

        return retval;
    }

    // používat FedoraApiM: List<RelationshipTuple> getRelationships; boolean addRelationship; boolean purgeRelationship

    /**
     * @param uuid
     * @return
     * @throws IOException
     */
    public DigitalObjectModel getModel(String uuid) throws IOException {
        DigitalObjectModel model;
        try {
            model = getDigitalObjectModel(uuid);
        } catch (IOException e) {
            LOGGER.error("Digital object " + uuid + " is not in the repository.\n" + e.getMessage());
            throw e;
        }
        return model;
    }

    /**
     * @param uuid
     * @return
     * @throws IOException
     */
    public Document getRelsExt(String uuid) throws IOException {
        String query = "/get/" + uuid + "/RELS-EXT";
//        LOGGER.debug("Reading rels ext from " + accessProvider.getFedoraHost() + query);
        WebResource resource = accessProvider.getFedoraWebResource(query);
        ClientResponse response = resource.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
        if (response.getStatus() == 200) {
            String docString = response.getEntity(String.class);
            if (docString == null) {
                throw new ConnectionException("Cannot get RELS EXT data.");
            }
            try {
                return XMLUtils.parseDocument(docString, true);
            } catch (ParserConfigurationException e) {
                LOGGER.error(e.getMessage(), e);
                throw new IOException(e);
            } catch (SAXException e) {
                LOGGER.error(e.getMessage(), e);
                throw new IOException(e);
            }
        } else {
            throw new IOException("Could not get RELS-EXT for object " + uuid + "\nResponse status:" + response.getStatus());
        }
    }

    /**
     * @param uuid
     * @return
     * @throws IOException
     */
    public Document getFullObjectXml(String uuid) throws IOException {
        String query = "/objects/" + uuid + "/objectXML";
        LOGGER.debug("Reading object XML from " + accessProvider.getFedoraHost() + query);
        WebResource resource = accessProvider.getFedoraWebResource(query);
        ClientResponse response = resource.get(ClientResponse.class);
        if (response.getStatus() == 200) {
            String docString = response.getEntity(String.class);
            if (docString == null) {
                throw new ConnectionException("Cannot get objectXML data.");
            }
            try {
                return XMLUtils.parseDocument(docString, true);
            } catch (ParserConfigurationException e) {
                LOGGER.error(e.getMessage(), e);
                throw new IOException(e);
            } catch (SAXException e) {
                LOGGER.error(e.getMessage(), e);
                throw new IOException(e);
            }
        } else {
            throw new IOException("Could not get objectXML for object " + uuid + "\nResponse status:" + response.getStatus());
        }
    }

    /**
     * @param uuid
     * @return
     * @throws IOException
     */
    public DigitalObjectModel getDigitalObjectModel(String uuid) throws IOException {
        return getDigitalObjectModel(getRelsExt(uuid));
    }

    /**
     * @param relsExt
     * @return
     */
    public DigitalObjectModel getDigitalObjectModel(Document relsExt) {
        try {
            Element foundElement =
                    XMLUtils.findElement(relsExt.getDocumentElement(),
                            "hasModel",
                            FedoraNamespaces.FEDORA_MODELS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                DigitalObjectModel model = DigitalObjectModel.parseString(pidParser.getObjectId());
                return model;
            } else
                throw new IllegalArgumentException("cannot find model of ");
        } catch (DOMException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    public void setPolicy(String uuid, String policy) throws IOException {
        Document relsExt = getRelsExt(uuid);
        File tempRels = null;

        // get policy element
        try {
            Element foundElement =
                    XMLUtils.findElement(relsExt.getDocumentElement(),
                            "policy",
                            FedoraNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                // change value
                foundElement.setTextContent("policy:" + policy);
            } else {
                throw new IllegalArgumentException("Cannot find policy of " + uuid);
            }

            // Save XML file temporary
            tempRels = File.createTempFile("relsExt", ".rdf");
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(relsExt), new StreamResult(tempRels));
            // Send temporary file to fedora
            setRelsExt(uuid, tempRels.getAbsolutePath());

        } catch (DOMException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (CreateObjectException e) {
            e.printStackTrace();
        } finally {
            if (tempRels != null) {
                tempRels.delete();
            }
        }
    }

    public void setPrivate(String uuid) throws IOException {
        setPolicy(uuid, "private");
    }

    public void setPublic(String uuid) throws IOException {
        setPolicy(uuid, "public");
    }

    public boolean isPublic(String uuid) throws IOException {
        return getPolicy(getRelsExt(uuid)).equals("public");
    }

    public boolean isPrivate(String uuid) throws IOException {
        return getPolicy(getRelsExt(uuid)).equals("private");
    }

    public String getPolicy(String uuid) throws IOException {
        return getPolicy(getRelsExt(uuid));
    }

    /**
     * @param relsExt
     * @return
     */
    public String getPolicy(Document relsExt) {
        try {
            Element foundElement =
                    XMLUtils.findElement(relsExt.getDocumentElement(),
                            "policy",
                            FedoraNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String value = foundElement.getTextContent();
                String policy = value.replace("policy:", "");
                return policy;
            } else
                throw new IllegalArgumentException("Cannot find policy");
        } catch (DOMException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Gets the fedora datastreams list.
     *
     * @param uuid the uuid
     * @return the fedora datastreams list
     */
    public String getFedoraDatastreamsList(String uuid) {
        String datastreamsListPath =
                accessProvider.getFedoraHost() + "/objects/" + uuid + "/datastreams?format=xml";
        return datastreamsListPath;
    }

    /**
     * @param uuid
     * @return
     * @throws IOException
     */
    public InputStream getPdf(String uuid) throws IOException {
        ClientResponse response =
                accessProvider.getFedoraWebResource("/objects/" + uuid + "/datastreams/IMG_FULL/content")
                        .accept("application/pdf").get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Getting PDF of " + uuid + " failed. HTTP error code: "
                    + response.getStatus());
        }
        InputStream is = response.getEntityInputStream();
        return is;
    }

    /**
     * @param uuid
     * @return
     * @throws IOException
     */
    public InputStream getImgFull(String uuid, String mimetype) throws IOException {
        return getImgFull(uuid, mimetype, 1);
    }
    /**
     * @param uuid
     * @return
     * @throws IOException
     */
    public InputStream getImgFull(String uuid, String mimetype, int attempt) throws IOException {
        ClientResponse response =
                accessProvider.getFedoraWebResource("/objects/" + uuid + "/datastreams/IMG_FULL/content")
                        .accept(mimetype).get(ClientResponse.class);
        if (response.getStatus() != 200) {
            if (attempt <= 5) {
            LOGGER.warn("Attempt " + attempt + " to get IMG_FULL stream of " + uuid + " failed. HTTP error code: "
                    + response.getStatus() + "\n"
                    + "Trying again.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                attempt++;
                return getImgFull(uuid, mimetype, attempt);
            } else {
                throw new FileNotFoundException("Getting IMG_FULL stream of " + uuid + " failed. HTTP error code: "
                        + response.getStatus() + "\n"
                        + " request: " + accessProvider.getFedoraWebResource("/objects/" + uuid + "/datastreams/IMG_FULL/content").toString());
            }
        }
        InputStream is = response.getEntityInputStream();
        return is;
    }

    /**
     * @param uuid
     * @return
     * @throws IOException
     *         NullPointerException if image url not found
     */
    public InputStream getImgJp2(String uuid) throws IOException {
        String imageUrl = getTilesLocation(uuid);
        ClientResponse response =
                accessProvider.getClient().resource(imageUrl)
                        .accept("image/jp2").get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new FileNotFoundException("Getting jp2 image of " + uuid + " failed. HTTP error code: "
                    + response.getStatus());
        }
        InputStream is = response.getEntityInputStream();
        return is;
    }

    public String getDjVuImgName(String uuid) throws IOException {
        // <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
        //  <rdf:Description rdf:about="info:fedora/uuid:56775c82-435f-11dd-b505-00145e5790ea">
        //      <file xmlns="http://www.nsdl.org/ontologies/relationships#">ABA001_2106500001.djvu</file>
        //  </rdf:Description>
        // </rdf:RDF>
        Document rdf = getRelsExt(uuid);
        NodeList fileList = rdf.getElementsByTagNameNS("http://www.nsdl.org/ontologies/relationships#", "file");
        Node file = fileList.item(0);
        String imgName = file.getTextContent();
        return imgName;
    }

    /**
     * najde umístění obrázku v imageserveru (z datastreamu IMG_FULL)
     *
     * @param uuid
     * @return
     */
    public String getImgLocationFromHtml(String uuid) throws IOException {
        if (uuid == null || uuid.equals("")) {
            LOGGER.error("uuid can't be empty");
        }
        String query = "/objects/" + uuid + "/datastreams/IMG_FULL";
        String username = accessProvider.getFedoraUser();
        String password = accessProvider.getFedoraPassword();
        if (username == null || username.equals("")) {
            LOGGER.warn("Fedora username not specified in config file");
        }
        if (password == null || password.equals("")) {
            LOGGER.warn("Fedora password not specified in config file");
        }
        String login = username + ":" + password;
        String base64login = new String(Base64.encodeBase64(login.getBytes()));
        org.jsoup.nodes.Document html = Jsoup.connect("http://" + accessProvider.getFedoraHost() + query)
                .header("Authorization", "Basic " + base64login)
                .get();
        // http://jsoup.org/apidocs/org/jsoup/select/Selector.html
        // http://try.jsoup.org/
        org.jsoup.nodes.Element locationElement = html.select("td:contains(Datastream Location:) ~ td").first();
        String imgLocation = locationElement.text();
        if (imgLocation.startsWith("http")) {
            return imgLocation.replace("/big.jpg", ".jp2");
        } else {
            // TODO udělat líp
            throw new IOException("Obrázek mimo fedoru nenalezen");
        }
    }

    /**
     * najde umístění obrázku v imageserveru (z tiles-url v RELS-EXT)
     *
     * @param uuid
     * @throws NullPointerException if img not in imageserver (no tiles-url element in RELS-EXT)
     * @return
     */
    public String getTilesLocation(String uuid) throws IOException {

        Document rels = getRelsExt(uuid);
        String tilesUrl = rels.getElementsByTagName("tiles-url").item(0).getTextContent();
        return tilesUrl + ".jp2";
    }

    public String getImgName(String uuid) throws IOException {
        String imgPath = getTilesLocation(uuid);
        String[] splitPath = imgPath.split("/");
        String imgName = splitPath[splitPath.length - 1];
        return imgName;
    }

    /**
     * @param uuid
     * @return
     * @throws IOException
     */
    public void saveImg(String uuid, String mimetype, String outputPath) throws IOException {
        if (getDigitalObjectModel(uuid).equals(DigitalObjectModel.PAGE)) {
            // může mít obrázek i něco jinýho než PAGE?

        }
        InputStream inputStream = getImgFull(uuid, mimetype);

    }

    /**
     * @param uuid
     */
    public void purgeObject(String uuid) {

        ClientResponse response = accessProvider.getFedoraWebResource("/objects/" + uuid).delete(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Purging object " + uuid + " failed. HTTP error code: "
                    + response.getStatus());
        }
    }

    /**
     * @param uuid
     * @return
     */
    public String getOcr(String uuid) {
        String ocrUrl = ocr(uuid);
        LOGGER.debug("Reading OCR +" + ocrUrl);
        try {
            String ocrOutput = accessProvider.getFedoraWebResource("/objects/" + uuid + "/datastreams/TEXT_OCR/content").get(String.class);
            return ocrOutput;
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 404) {
                return null;
            } else {
                throw new UniformInterfaceException(e.getResponse());
            }
        }
    }

    /**
     * @param uuid
     * @return
     */
    public String getAlto(String uuid) {
        String altoUrl = alto(uuid);
        LOGGER.debug("Reading ALTO +" + altoUrl);
        try {
            String altoOutput = accessProvider.getFedoraWebResource("/objects/" + uuid + "/datastreams/ALTO/content").get(String.class);
            return altoOutput;
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 404) {
                return null;
            } else {
                throw new UniformInterfaceException(e.getResponse());
            }
        }
    }

    /**
     * @param uuid
     * @param ocr
     * @return
     * @throws CreateObjectException
     */
    public boolean setOcr(String uuid, String ocr) throws CreateObjectException {
        return insertManagedDatastream(Constants.DATASTREAM_ID.TEXT_OCR, uuid, ocr, false, "text/plain");
    }

    public boolean setAltoOcr(String uuid, String ocr) throws CreateObjectException {
        return insertManagedDatastream(Constants.DATASTREAM_ID.ALTO, uuid, ocr, false, "text/plain");
    }

    /**
     * @param uuid
     * @param path
     * @return
     * @throws CreateObjectException
     */
    public boolean setThumbnail(String uuid, String path) throws CreateObjectException {
        return insertManagedDatastream(Constants.DATASTREAM_ID.IMG_THUMB, uuid, path, true, "image/jpeg");
    }

    /**
     * @param uuid
     * @param path
     * @return
     * @throws CreateObjectException
     */
    public boolean setPreview(String uuid, String path) throws CreateObjectException {
        return insertManagedDatastream(Constants.DATASTREAM_ID.IMG_PREVIEW, uuid, path, true, "image/jpeg");
    }

    /**
     * Method sets IMG_FULL datastream value to external source
     *
     * @param uuid Uuid of file to be changed
     * @param path Path to external source
     * @return True if successful
     * @throws CreateObjectException
     */
    public boolean setImgPreviewFromExternal(String uuid, String path) throws CreateObjectException {
        return insertExternalDatastream(Constants.DATASTREAM_ID.IMG_PREVIEW, uuid, path, false, "image/jpeg");
    }

    /**
     * Method sets IMG_FULL datastream value to external source
     *
     * @param uuid Uuid of file to be changed
     * @param path Path to external source
     * @return True if successful
     * @throws CreateObjectException
     */
    public boolean setImgFullFromExternal(String uuid, String path) throws CreateObjectException {
        return insertExternalDatastream(Constants.DATASTREAM_ID.IMG_FULL, uuid, path, false, "image/jpeg");
    }

    /**
     * Method sets IMG_THUMB datastream value to external source
     *
     * @param uuid Uuid of file to be changed
     * @param path Path to external source
     * @return True if successful
     * @throws CreateObjectException
     */
    public boolean setImgThumbnailFromExternal(String uuid, String path) throws CreateObjectException {
        return insertExternalDatastream(Constants.DATASTREAM_ID.IMG_THUMB, uuid, path, false, "image/jpeg");
    }

    /**
     * Methods replaces RELS-EXT file
     *
     * @param uuid Uuid of file to be changed
     * @param path Path to new RELS-EXT file
     * @return True if successful
     * @throws CreateObjectException
     */
    public boolean setRelsExt(String uuid, String path) throws CreateObjectException {
        return insertXDataStream(Constants.DATASTREAM_ID.RELS_EXT, uuid, path, true, "application/rdf+xml");
    }

    /**
     * Ocr.
     *
     * @param uuid the uuid
     * @return the string
     */
    private String ocr(String uuid) {
        String fedoraObject =
                accessProvider.getFedoraHost() + "/objects/" + uuid + "/datastreams/TEXT_OCR/content";
        return fedoraObject;
    }

    /**
     * Alto.
     *
     * @param uuid the uuid
     * @return the string
     */
    private String alto(String uuid) {
        String fedoraObject =
                accessProvider.getFedoraHost() + "/objects/" + uuid + "/datastreams/ALTO/content";
        return fedoraObject;
    }

    /**
     * Insert managed datastream.
     *
     * @param dsId              the ds id
     * @param uuid              the uuid
     * @param filePathOrContent the file path or content
     * @param isFile            the is file
     * @param mimeType          the mime type
     * @return true, if successful
     * @throws CreateObjectException the create object exception
     */
    private boolean insertManagedDatastream(Constants.DATASTREAM_ID dsId,
                                            String uuid,
                                            String filePathOrContent,
                                            boolean isFile,
                                            String mimeType) throws CreateObjectException {
        return insertDataStream(dsId, uuid, filePathOrContent, isFile, mimeType, "M", "true", "A");
    }

    //    Inserts datastream with controulGroup="R"
    //Inserts datastream with controulGroup="E"
    private boolean insertExternalDatastream(Constants.DATASTREAM_ID dsId,
                                             String uuid,
                                             String filePathOrContent,
                                             boolean isFile,
                                             String mimeType) throws CreateObjectException {
//        return insertDataStream(dsId, uuid, filePathOrContent, isFile, mimeType, "R", "false", "A");
        return insertDataStream(dsId, uuid, filePathOrContent, isFile, mimeType, "E", "false", "A");
    }

    //Inserts datastream with controulGroup="X"
    private boolean insertXDataStream(Constants.DATASTREAM_ID dsId,
                                      String uuid,
                                      String filePathOrContent,
                                      boolean isFile,
                                      String mimeType) throws CreateObjectException {
        return insertDataStream(dsId, uuid, filePathOrContent, isFile, mimeType, "X", "true", "A");
    }

    /**
     * Inserts any stream
     *
     * @param dsId
     * @param uuid
     * @param filePathOrContent
     * @param isFile
     * @param mimeType
     * @param controlGroup      control group - M or R
     * @param versionable
     * @param dsState
     * @return
     * @throws CreateObjectException
     */
    private boolean insertDataStream(Constants.DATASTREAM_ID dsId,
                                     String uuid,
                                     String filePathOrContent,
                                     boolean isFile,
                                     String mimeType,
                                     String controlGroup,
                                     String versionable,
                                     String dsState) throws CreateObjectException {

        String query =
                "/objects/" + (uuid.contains("uuid:") ? uuid : "uuid:".concat(uuid)) + "/datastreams/" + dsId.getValue();

        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("controlGroup", controlGroup);
        queryParams.add("versionable", versionable);
        queryParams.add("dsState", dsState);
        queryParams.add("mimeType", mimeType);

        WebResource resource = accessProvider.getFedoraWebResource(query);
        ClientResponse response = null;
        if (controlGroup == "R" || controlGroup == "E") {
            queryParams.add("dsLocation", filePathOrContent);
            resource.delete();
        }

        try {
            if (isFile) {
                response = resource.queryParams(queryParams).post(ClientResponse.class, new File(filePathOrContent));
            } else {
                response = resource.queryParams(queryParams).post(ClientResponse.class, filePathOrContent);
            }

        } catch (UniformInterfaceException e) {
            int status = e.getResponse().getStatus();
            if (status == 404) {
                LOGGER.fatal("Process not found");
            }

            LOGGER.error(e.getMessage());
            e.printStackTrace();
            throw new CreateObjectException("Unable to post "
                    + (isFile ? ("a file: " + filePathOrContent + " as a ") : "")
                    + "managed datastream to the object: " + uuid);
        }

        if (response.getStatus() == 201) {
            LOGGER.debug("An " + dsId.getValue() + (isFile ? (" file: " + filePathOrContent) : "")
                    + " has been inserted to the digital object: " + uuid + " as a " + dsId.getValue()
                    + " datastream.");

            return true;
        } else {
            LOGGER.error("An error occured during inserting an " + dsId.getValue()
                    + (isFile ? (" file: " + filePathOrContent) : "") + " to the digital object: " + uuid
                    + " as a " + dsId.getValue() + " datastream.");
            LOGGER.error("Error " + response.getStatus());
            return false;

        }
    }

    /**
     * @param pid
     * @return
     * @throws IOException
     */
    public Document getDCStream(String pid) throws IOException {
        pid = checkPid(pid);
        try {
            WebResource resource = accessProvider.getFedoraWebResource("/objects/" + pid + "/datastreams/DC/content");
            Document result = resource.accept(MediaType.APPLICATION_XML_TYPE).get(Document.class);
            return result;
        } catch (UniformInterfaceException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    /**
     * @param pid
     * @return
     * @throws IOException
     */
    public Document getMODSStream(String pid) throws IOException {
        pid = checkPid(pid);
        try {
            WebResource resource = accessProvider.getFedoraWebResource("/objects/" + pid + "/datastreams/BIBLIO_MODS/content");
            Document result = resource.accept(MediaType.APPLICATION_XML_TYPE).get(Document.class);
            return result;
        } catch (UniformInterfaceException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    /**
     * @param pid            - pid objektu ve fedoře
     * @param datastreamName název datastreamu
     * @return
     * @throws IOException
     * @see cz.mzk.k4.tools.utils.fedora.Constants
     */
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        List<DatastreamDef> datastreams = accessProvider.getFedoraAPIA().listDatastreams(pid, null);
        for (DatastreamDef datastream : datastreams) {
            if (datastream.getID().equals(datastreamName)) {
                return datastream.getMIMEType();
            }
        }
        // neobsahuje daný datastream
        throw new IOException("Objekt neobsahuje datastream " + datastreamName);
    }

    // TODO: isPidValid
    private String checkPid(String pid) {
        if (!pid.contains("uuid")) {
            pid = "uuid:" + pid;
        }
        return pid;
    }

    /**
     * Opraví vazbu na dlaždice v RELS-EXT
     * Smaže příp. vazbu na DjVu nebo DeepZoom cache
     *
     * @param uuid
     */
    // TODO asi spíš smazat všechny file / tiles a dát nový odkaz
    public void repairImageserverTilesRelation(String uuid) throws IOException, CreateObjectException, TransformerException {
        File tempDom = null;
        String krameriusNS = "http://www.nsdl.org/ontologies/relationships#";
        String imagePath = "";
        // najít cestu k obrázku v imageserveru
        try {
            imagePath = getImgLocationFromHtml(uuid); // get tiles by nefungovalo
            if (imagePath.equals("")) {
                throw new IOException();
            } else {
                imagePath = imagePath.replace(".jp2", "");
            }
        } catch (IOException e) {
            IOException exception = new IOException("Napodařilo se získat cestu k obrázku objektu " + uuid);
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }

        // oprava img datastreamů TODO zrušit?
        repairImageStreams(uuid, imagePath);

        // je cesta k obrázku - smazat všechny elementy <file> a <tiles-url>, nahradit novou vazbou
        try {
            Document dom = getRelsExt(uuid);

            // smazat elementy <file> (např. vazba na DjVu obrázek)
            NodeList fileNodeList = dom.getElementsByTagNameNS(krameriusNS, "file");
            int fileNodeListLength = fileNodeList.getLength(); // délka seznamu se během cyklu mění
            for (int i = 0; i < fileNodeListLength; i++) {
                Node file = fileNodeList.item(0); // seznam se odmazáváním zmenšuje, maže se vždycky první uzel
                file.getParentNode().removeChild(file);
            }

            // smazat elementy <tiles-url> (případné existující vazby na imageserver nebo DeepZoom cache)
            NodeList tilesUrlNodeList = dom.getElementsByTagNameNS(krameriusNS, "tiles-url");

            int tilesNodeListLength = tilesUrlNodeList.getLength(); // délka seznamu se během cyklu mění
            for (int i = 0; i < tilesNodeListLength; i++) {
                Node tilesUrlNode = tilesUrlNodeList.item(0); // seznam se odmazáváním zmenšuje, maže se vždycky první uzel
                tilesUrlNode.getParentNode().removeChild(tilesUrlNode);
            }

            // potřeba? Co atribut rdf:about?
            if (dom.getChildNodes().getLength() == 0) {
                dom.appendChild(dom.createElement("rdf:Description"));
            }
            Element currentElement = (Element) dom.getElementsByTagName("rdf:Description").item(0);
            //Add element kramerius:tiles-url
            Element tiles = dom.createElementNS(krameriusNS, "tiles-url");
            tiles.setTextContent(imagePath); // find from img stream
            currentElement.appendChild(tiles);

            //save XML file temporary
            tempDom = File.createTempFile("relsExt", ".rdf");
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(dom), new StreamResult(tempDom));
            //Copy temporary file to document
            setRelsExt(uuid, tempDom.getAbsolutePath());

        } catch (CreateObjectException e) {
            throw new CreateObjectException("Chyba při změně XML: " + e.getMessage());
        } catch (TransformerConfigurationException e) {
            throw new TransformerConfigurationException("Chyba při změně XML: " + e.getMessage());
        } catch (TransformerException e) {
            throw new TransformerException("Chyba při změně XML: " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("Chyba při změně XML: " + e.getMessage());
        } finally {
            if (tempDom != null) {
                tempDom.delete();
            }
        }
    }

    public void repairImageStreams(String uuid, String imageUrl) throws CreateObjectException {
        imageUrl = imageUrl.replace(".jp2", ""); // odstranit suffix, pokud ho url obsahuje
        setImgFullFromExternal(uuid, imageUrl + "/big.jpg");
        setImgPreviewFromExternal(uuid, imageUrl + "/preview.jpg");
        setImgThumbnailFromExternal(uuid, imageUrl + "/thumb.jpg");
    }

    public void addChild(String parent, String child) throws CreateObjectException, TransformerException, IOException {
        File tempDom = null;
        String krameriusNS = "http://www.nsdl.org/ontologies/relationships#";

        // je cesta k obrázku - smazat všechny elementy <file> a <tiles-url>, nahradit novou vazbou
        try {
            Document dom = getRelsExt(parent);

            Element currentElement = (Element) dom.getElementsByTagName("rdf:Description").item(0);
            //Add element
            DigitalObjectModel childModel = getModel(child);
            Element childElement = null;
            if (childModel.equals(DigitalObjectModel.PERIODICALITEM)) {
                childElement = dom.createElementNS(krameriusNS, "hasItem");
            } else if (childModel.equals(DigitalObjectModel.SUPPLEMENT)) {
                childElement = dom.createElementNS(krameriusNS, "hasIntCompPart");
            } else if (childModel.equals(DigitalObjectModel.PERIODICALVOLUME)) {
                childElement = dom.createElementNS(krameriusNS, "hasVolume");
            } else {
                throw new NotImplementedException(childModel + " not yet implemented");
            }
            childElement.setAttribute("rdf:resource", "info:fedora/" + child);
            currentElement.appendChild(childElement);

            //save XML file temporary
            tempDom = File.createTempFile("relsExt", ".rdf");
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(dom), new StreamResult(tempDom));
            //Copy temporary file to document
            setRelsExt(parent, tempDom.getAbsolutePath());

        } catch (CreateObjectException e) {
            throw new CreateObjectException("Chyba při změně XML: " + e.getMessage());
        } catch (TransformerConfigurationException e) {
            throw new TransformerConfigurationException("Chyba při změně XML: " + e.getMessage());
        } catch (TransformerException e) {
            throw new TransformerException("Chyba při změně XML: " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("Chyba při změně XML: " + e.getMessage());
        } finally {
            if (tempDom != null) {
                tempDom.delete();
            }
        }
    }
}
