package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by Jan on 2.8.14.
 */
public class ImageUrlWorker {
    private static final Logger LOGGER = LogManager.getLogger(ImageUrlWorker.class);
    private AccessProvider accessProvider;
    private FedoraUtils fedoraUtils;
    private boolean writeEnabled;
    public static final String DATASTREAM_LOCATION_TAG = "foxml:contentLocation";
    public static final String FULL_DATASTREAM_NAME = "IMG_FULL";
    public static final String THUMB_DATASTREAM_NAME = "IMG_THUMB";
    public static final String PREVIEW_DATASTREAM_NAME = "IMG_PREVIEW";
    public static final String FULL_SUFFIX = "/big.jpg";
    public static final String THUMB_SUFFIX = "/thumb.jpg";
    public static final String PREVIEW_SUFFIX = "/preview.jpg";

    public ImageUrlWorker(boolean writeEnabled, AccessProvider accessProvider, FedoraUtils fedoraUtils) {
        this.accessProvider = accessProvider;
        this.fedoraUtils = fedoraUtils;
        this.writeEnabled = writeEnabled;
    }

    public void run(String topUuid) {
        // dostane uuid ročníku, vytáhne stránky, pozměňuje URL
//        String pageUuid = "uuid:caf92128-713c-40dd-b883-9847779c51c0";

        List<String> pages = fedoraUtils.getChildrenUuids(topUuid, DigitalObjectModel.PAGE);

        int i = 0;
        for (String pageUuid : pages) {
            try {
                LOGGER.info("Strana " + pageUuid + " , oprava " + THUMB_DATASTREAM_NAME);
                repairImgUrl(THUMB_DATASTREAM_NAME, pageUuid);
                LOGGER.info("Strana " + pageUuid + " , oprava " + FULL_DATASTREAM_NAME);
                repairImgUrl(FULL_DATASTREAM_NAME, pageUuid);
                LOGGER.info("Strana " + pageUuid + " , oprava " + PREVIEW_DATASTREAM_NAME);
                repairImgUrl(PREVIEW_DATASTREAM_NAME, pageUuid);
                LOGGER.info("Strana " + pageUuid + ", oprava RELS-EXT");
                repairRelsExt(pageUuid);
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            } catch (CreateObjectException e) {
                e.printStackTrace();
            }
        }
    }

    private void repairImgUrl(String datastreamName, String pageUuid) throws XPathExpressionException, CreateObjectException, IOException {
        Document objectXML = fedoraUtils.getFullObjectXml(pageUuid);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        Element datastream = (Element) xpath.evaluate("//*[@ID='" + datastreamName + "']", objectXML, XPathConstants.NODE);
        if (datastream != null) {
            String controlGroup = datastream.getAttribute("CONTROL_GROUP");
            if (controlGroup.equals("R")) {
                Element urlElement = (Element) datastream.getElementsByTagName(DATASTREAM_LOCATION_TAG).item(0);
                String imgLocaion = getImgLocation(pageUuid);

                if (!imgLocaion.contains("imageserver")) {
                    String[] pieces = imgLocaion.split(".cz");
                    imgLocaion = pieces[0] + ".cz/imageserver" + pieces[1];
                }

                if (datastreamName.equals(FULL_DATASTREAM_NAME)) {
                    imgLocaion = imgLocaion.replace(PREVIEW_SUFFIX, FULL_SUFFIX);
                    imgLocaion = imgLocaion.replace(THUMB_SUFFIX, FULL_SUFFIX);
                    if (!imgLocaion.contains(FULL_SUFFIX)) {
                        imgLocaion += FULL_SUFFIX;
                    }
                    LOGGER.info("New image URL in datastream " + datastreamName + " page " + pageUuid + ": " + imgLocaion);
                    fedoraUtils.setImgFullFromExternal(pageUuid, imgLocaion);
                }
                if (datastreamName.equals(THUMB_DATASTREAM_NAME)) {
                    imgLocaion = imgLocaion.replace(PREVIEW_SUFFIX, THUMB_SUFFIX);
                    imgLocaion = imgLocaion.replace(FULL_SUFFIX, THUMB_SUFFIX);
                    if (!imgLocaion.contains(THUMB_SUFFIX)) {
                        imgLocaion += THUMB_SUFFIX;
                    }
                    LOGGER.info("New image URL in datastream " + datastreamName + " page " + pageUuid + ": " + imgLocaion);
                    fedoraUtils.setImgThumbnailFromExternal(pageUuid, imgLocaion);
                }
                if (datastreamName.equals(PREVIEW_DATASTREAM_NAME)) {
                    imgLocaion = imgLocaion.replace(FULL_SUFFIX, PREVIEW_SUFFIX);
                    imgLocaion = imgLocaion.replace(THUMB_SUFFIX, PREVIEW_SUFFIX);
                    if (!imgLocaion.contains(PREVIEW_SUFFIX)) {
                        imgLocaion += PREVIEW_SUFFIX;
                    }
                    LOGGER.info("New image URL in datastream " + datastreamName + " page " + pageUuid + ": " + imgLocaion);
                    fedoraUtils.setImgPreviewFromExternal(pageUuid, imgLocaion);
                }
            } else {
                LOGGER.warn("Datastream " + datastreamName + " control group is not R but " + controlGroup);
            }
        }
    }

    private void repairRelsExt(String pageUuid) throws XPathExpressionException, CreateObjectException, IOException {

        // už je lepší metoda: fedoraUtils.getImgLocation(uuid);

        // oprava RELS-EXT: pridat element <tiles-url xmlns="http://www.nsdl.org/ontologies/relationships#">[cesta k obrazku]</tiles-url>
        // cesta k obrazku napr: http://meditor.fsv.cuni.cz/imageserver/mzk01/000/181/899/128dc2bf-b28b-343f-97f2-6443a6eb4060

        // get RELS-EXT
        Document relsExt = fedoraUtils.getRelsExt(pageUuid);
        Element relsExtElement = relsExt.getDocumentElement();
        // get Description node
        NodeList childList = relsExtElement.getChildNodes();
        Node description = childList.item(0);
        while (description.getLocalName() == null || !description.getLocalName().equals("Description")) {
            description = description.getNextSibling();
        }

        removeChildElement(description, "tiles-url");

        // get image location
        String imagePathString = getImgLocation(pageUuid);

        // create and insert new node
        Text imagePath = relsExt.createTextNode(imagePathString);
        Element tilesUrl = relsExt.createElementNS("http://www.nsdl.org/ontologies/relationships#", "tiles-url");
        tilesUrl.appendChild(imagePath);
        description.appendChild(relsExt.createTextNode("\t"));
        description.appendChild(tilesUrl);
        description.appendChild(relsExt.createTextNode("\n"));

        // serialize new xml
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        PrintWriter printWriter = null;
        String relsExtFileName = "RELS-EXT/" + pageUuid + "_RELS-EXT";
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(relsExt), new StreamResult(writer));
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            String output = writer.getBuffer().toString();
            printWriter = new PrintWriter(relsExtFileName, "UTF-8");
            printWriter.print(output);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } finally {
            printWriter.close();
        }

        fedoraUtils.setRelsExt(pageUuid, relsExtFileName);

    }

    private String getImgLocation(String pageUuid) throws XPathExpressionException, CreateObjectException, IOException {
        Document objectXML = fedoraUtils.getFullObjectXml(pageUuid);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        Element datastream = (Element) xpath.evaluate("//*[@ID='" + FULL_DATASTREAM_NAME + "']", objectXML, XPathConstants.NODE);
        if (datastream != null) {
            Element urlElement = (Element) datastream.getElementsByTagName(DATASTREAM_LOCATION_TAG).item(0);
            String imgLocaion = urlElement.getAttribute("REF");
            imgLocaion = imgLocaion.replace(FULL_SUFFIX, "");
            return imgLocaion;
        }
        throw new IOException("Datastream " + FULL_DATASTREAM_NAME + " not found (" + pageUuid + ")");
    }

    private void removeChildElement(Node parentNode, String elementLocalName) {
        NodeList childList = parentNode.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (child.getLocalName() != null && child.getLocalName().equals(elementLocalName)) {
                parentNode.removeChild(child);
                LOGGER.info("Removed old element \"" + elementLocalName + "\" containing: " + child.getTextContent());
            }
        }
    }
}

