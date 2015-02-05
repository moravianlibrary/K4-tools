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
import org.apache.log4j.Logger;
import org.fedora.api.DatastreamDef;
import org.fedora.api.RelationshipTuple;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
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
                                    worker.run(childUuid);
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
    public void checkChildrenExistance(String uuid) {
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
                checkChildrenExistance(child.get(0));
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
    public Boolean checkChildrenAndOcrExistance(String uuid) {
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
                Boolean childResult = checkChildrenAndOcrExistance(child.get(0));
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
            String[] tokens = line.split(" ");
            if (tokens.length < 3) continue;
            try {
                RelationshipTuple tuple = new RelationshipTuple();
                tuple.setSubject(tokens[0].substring(1, tokens[0].length() - 1));
                tuple.setPredicate(tokens[1].substring(1, tokens[1].length() - 1));
                tuple.setObject(tokens[2].substring(1, tokens[2].length() - 1));
                tuple.setIsLiteral(false);
                retval.add(tuple);
            } catch (Exception ex) {
                LOGGER.info("Problem parsing RDF, skipping line:" + Arrays.toString(tokens) + " : " + ex);
            }
        }

        return retval;
    }

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
            throw new RuntimeException("Failed : HTTP error code : "
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
    //TODO dopsat mimetypy jako enum
    public InputStream getImgFull(String uuid, String mimetype) throws IOException {
        ClientResponse response =
                accessProvider.getFedoraWebResource("/objects/" + uuid + "/datastreams/IMG_FULL/content")
                        .accept(mimetype).get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new FileNotFoundException("Failed : HTTP error code : "
                    + response.getStatus());
        }
        InputStream is = response.getEntityInputStream();
        return is;
    }

    /**
     * @param uuid
     */
    public void purgeObject(String uuid) {

        ClientResponse response = accessProvider.getFedoraWebResource("/objects/" + uuid).delete(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
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

    //Inserts datastream with controulGroup="R"
    private boolean insertExternalDatastream(Constants.DATASTREAM_ID dsId,
                                            String uuid,
                                            String filePathOrContent,
                                            boolean isFile,
                                            String mimeType) throws CreateObjectException {
        return insertDataStream(dsId, uuid, filePathOrContent, isFile, mimeType, "R", "false", "A");
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
     * @param dsId
     * @param uuid
     * @param filePathOrContent
     * @param isFile
     * @param mimeType
     * @param controlGroup control group - M or R
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
        if(controlGroup == "R") {
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
            LOGGER.info("An " + dsId.getValue() + (isFile ? (" file: " + filePathOrContent) : "")
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
}
