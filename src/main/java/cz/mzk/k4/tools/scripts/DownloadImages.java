package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.Script;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by hradskam on 2.7.14.
 */
public class DownloadImages implements Script{
    public static final Logger LOGGER = Logger.getLogger(DownloadImages.class);
    public static final String DATASTREAM_NAME = "IMG_FULL";
    public static final String DATASTREAM_LOCATION_TAG = "foxml:contentLocation";
    public static final String DATASTREAM_SUFFIX= "/big.jpg";

    public static final String INPUT_ADRESS = "/data/imageserver/";

    @Override
    public void run(List<String> args) {
        if(args.size() != 1) {
            System.out.println("Součástí příkazu musí být i cesta k cílovému adresáři. (A nic dalšího)");
            return;
        }
        File homeFile = new File(args.get(0));
        File[] homeDirs = homeFile.listFiles((java.io.FileFilter) DirectoryFileFilter.DIRECTORY);
        for (File dir : homeDirs) {

            File imagesPathFile = new File(dir.getAbsolutePath() + "/images");
            if(!imagesPathFile.exists()) {
                imagesPathFile.mkdir();
            }
            LOGGER.info("Vytvořen adresář images v adresáři " + dir.getName());

            File homeDir = new File(dir.getAbsolutePath());

            Collection<File> foxmlFiles = FileUtils.listFiles(homeDir, new SuffixFileFilter(".xml"), DirectoryFileFilter.DIRECTORY);
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                if(!foxmlFiles.isEmpty()) {
                    for (File foxml : foxmlFiles) {
                        Document document = builder.parse(foxml);

                        XPathFactory xPathFactory = XPathFactory.newInstance();
                        XPath xpath = xPathFactory.newXPath();
                        Element datastream = (Element) xpath.evaluate("//*[@ID='"+DATASTREAM_NAME+"']", document, XPathConstants.NODE);
                        if(datastream !=null) {
                            Element urlElement = (Element) datastream.getElementsByTagName(DATASTREAM_LOCATION_TAG).item(0);
                            String imgLocaion = urlElement.getAttribute("REF");
                            imgLocaion = imgLocaion.substring(imgLocaion.indexOf("NDK/"), imgLocaion.length() - DATASTREAM_SUFFIX.length());
                            imgLocaion += ".jp2";

                            File inputImgLocation = new File(INPUT_ADRESS + imgLocaion);
                            File outputImgLocation = new File(homeDir.getAbsolutePath() + "/images/"+inputImgLocation.getName());
                            FileUtils.copyFile(inputImgLocation, outputImgLocation);
                            LOGGER.info("Stažen obrázek " + inputImgLocation.getName());
                        }


                    }
                }
            } catch (ParserConfigurationException e) {
                LOGGER.error("Chyba ve zpracování XML: ");
                e.printStackTrace();
            } catch (SAXException e) {
                LOGGER.error("Chyba ve zpracování XML: ");
                e.printStackTrace();
            } catch (IOException e) {
                LOGGER.error("Chyba při otvírání XML souboru: ");
                e.printStackTrace();
            } catch (XPathExpressionException e) {
                LOGGER.error("Chyba v XPath: ");
                e.printStackTrace();
            }

        }
        LOGGER.info("Dokončeno stahování obrázků v " + homeFile.getName());

    }

    @Override
    public String getUsage() {
        return "V adresáři najde všechny xml soubory, vytáhne si adresu imageserveru a stáhne obrázky do složky images.";
    }

}
