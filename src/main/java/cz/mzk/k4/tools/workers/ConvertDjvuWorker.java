package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.FormatConvertor;
import cz.mzk.k4.tools.utils.ImageserverUtils;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.Constants;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hradskam on 27.3.14.
 */
public class ConvertDjvuWorker extends UuidWorker {

    private AccessProvider accessProvider;
    private FedoraUtils fedoraUtils;
    private static final Logger LOGGER = Logger.getLogger(ConvertDjvuWorker.class);

    public ConvertDjvuWorker(boolean writeEnabled) {
        super(writeEnabled);
        accessProvider = new AccessProvider();
        fedoraUtils = new FedoraUtils(accessProvider);
    }

    /**
     * Base method for converting files from djvu to jpeg2000
     *
     * @param uuid
     */
    @Override
    public void run(String uuid) {

        LOGGER.info("Dokument " + uuid + " se začíná zpracovávat.");


        try {
            //Get djvu image for given uuid
            String mimetype = fedoraUtils.getMimeTypeForStream(uuid, Constants.DATASTREAM_ID.IMG_FULL.getValue());
            InputStream djvuInputStream = fedoraUtils.getImgFull(uuid, mimetype);
            if (djvuInputStream != null) {

                //Convert image to jpeg2000
                InputStream jp2Stream = FormatConvertor.convertDjvuToJp2(djvuInputStream);
                LOGGER.info("Proběhla konverze obrázku.");

                //Upload image to image server
                ImageserverUtils imageServer = new ImageserverUtils();
                imageServer.uploadJp2ToImageserver(jp2Stream, uuid);
                LOGGER.info("Proběhl upload na imageserver.");

                //Change datastream references for uuid to new image, for reference to imageserver uses constant IMAGE_SERVER_URL
                String imageServerUrl = AccessProvider.getInstance().getImageserverUrlPath();
                String path = imageServerUrl + uuid.substring("uuid:".length()) + "/";
                fedoraUtils.setImgFullFromExternal(uuid, path + "big.jpg");
                fedoraUtils.setImgThumbnailFromExternal(uuid, path + "thumb.jpg");
                LOGGER.info("Nastaveny datastreamy.");

                //Change RELS-EXT document to refer to imageserver url, for reference to imageserver uses constant IMAGE_SERVER_URL
                changeRelsExt(uuid, imageServerUrl);
                LOGGER.info("Změněno RELS-EXT.");
            }
            LOGGER.info("Konec zpracování dokumentu.");

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }


    }


    private void changeRelsExt(String uuid, String imageServerUrl) throws CreateObjectException, TransformerException, IOException {
        File tempDom = null;
        try {
            Document dom = fedoraUtils.getRelsExt(uuid);
            Element rdf = (Element) dom.getElementsByTagName("rdf:RDF").item(0);
            if(!rdf.hasAttribute("xmlns:kramerius")) {
                rdf.setAttribute("xmlns:kramerius","http://www.nsdl.org/ontologies/relationships#");
            }
            if (dom.getChildNodes().getLength() == 0) {
                dom.appendChild(dom.createElement("rdf:Description"));
            }
            Element currentElement = (Element) dom.getElementsByTagName("rdf:Description").item(0);
            //Check if kramerius:tiles-url element exist
            if (currentElement.getElementsByTagName("kramerius:tiles-url").getLength() == 0) {

                //Add element kramerius:tiles-url
                Element tiles = dom.createElement("kramerius:tiles-url");
                tiles.setTextContent(imageServerUrl + uuid.substring("uuid:".length()));
                currentElement.appendChild(tiles);

                //save XML file temporary
                tempDom = File.createTempFile("relsExt", ".rdf");
                TransformerFactory.newInstance().newTransformer().transform(new DOMSource(dom), new StreamResult(tempDom));
                //Copy temporary file to document
                fedoraUtils.setRelsExt(uuid, tempDom.getAbsolutePath());
            }
        } catch (CreateObjectException e) {
            throw new CreateObjectException("Chyba při změně XML: " + e.getMessage());
        } catch (TransformerConfigurationException e) {
            throw new TransformerConfigurationException("Chyba při změně XML: " + e.getMessage());
        } catch (TransformerException e) {
            throw new TransformerException("Chyba při změně XML: " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("Chyba při změně XML: " + e.getMessage());
        } finally {
            if(tempDom != null) {
                tempDom.delete();
            }
        }
    }
}
