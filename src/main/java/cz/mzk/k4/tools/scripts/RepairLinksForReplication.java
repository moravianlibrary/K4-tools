package cz.mzk.k4.tools.scripts;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import cz.mzk.k4.tools.utils.Script;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 8/21/13
 * <p/>
 * Prisla nejaka data z jinych knihoven, jsou ve formatu Krameria3, ale maji rozbite odkazy
 * replikace z NK obsahovala špatné cesty k OCR a obrázkům, tyto cesty jsou zkontrolovány
 * a pokud jsou v jiné úrovni jsou odkazy upraveny
 */
public class RepairLinksForReplication implements Script {

    private static org.apache.log4j.Logger LOGGER = Logger.getLogger(RepairLinksForReplication.class);

    public static final String IMAGES_PATH = "jpg" + File.separator;
    public static final String OCR_PATH = "txt" + File.separator;

    /**
     * @param args
     */
    public void run(List<String> args) {
        String path = args.get(0);
        Iterator<File> iterator = FileUtils.iterateFiles(new File(path), new SuffixFileFilter(".xml"), TrueFileFilter.INSTANCE);

        while (iterator.hasNext()) {
            File file = iterator.next();
            checkAndChangeXML(file);
        }

    }

    // TODO: getUsage RepairLinksForReplication
    @Override
    public String getUsage() {
        return null;
    }

    protected void checkAndChangeXML(File file) {
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(file);

            List listImgHref = document.selectNodes("//PageImage/@href");

            for (Iterator iter = listImgHref.iterator(); iter.hasNext(); ) {
                Attribute attribute = (Attribute) iter.next();
                String img = attribute.getValue();
                String directoryPath = FilenameUtils.getFullPath(file.getAbsoluteFile().toString());

                File imageFile = new File(directoryPath + img);
                if (!imageFile.isFile()) {
                    if (!new File(directoryPath + IMAGES_PATH + img).isFile()) {
                        LOGGER.error("Chybi obrazek! " + directoryPath + IMAGES_PATH + img);
                    } else {
                        attribute.setValue(IMAGES_PATH + img);
                    }
                }
            }

            List listOcrHref = document.selectNodes("//PageText/@href");

            for (Iterator iter = listOcrHref.iterator(); iter.hasNext(); ) {
                Attribute attribute = (Attribute) iter.next();
                String ocr = attribute.getValue();
                String directoryPath = FilenameUtils.getFullPath(file.getAbsoluteFile().toString());
                File ocrFile = new File(directoryPath + ocr);

                if (!ocrFile.isFile()) {
                    if (!new File(directoryPath + OCR_PATH + ocr).isFile()) {
                        LOGGER.error("Chybi OCR! " + directoryPath + OCR_PATH + ocr);
                        continue;
                    } else {
                        ocrFile = new File(directoryPath + OCR_PATH + ocr);
                        attribute.setValue(OCR_PATH + ocr);
                    }
                }
                repairEncoding(ocrFile);
            }

            XMLWriter writer = null;
            try {
                writer = new XMLWriter(
                        new FileWriter(file)
                );
                writer.write(document);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void repairEncoding(File ocrFile) {

        try {
            CharsetDetector cd = new CharsetDetector();
            cd.setText(FileUtils.readFileToByteArray(ocrFile));
            CharsetMatch cm = cd.detect();

            //If it is not UTF-8 that's probably Windows-1250
            if (!"UTF-8".equals(cm.getName())) {
                String ocr = IOUtils.toString(new FileInputStream(ocrFile), "WINDOWS-1250");
                LOGGER.info("OCR saving in UTF-8");
                IOUtils.write(ocr, new FileOutputStream(ocrFile), "UTF-8");
            } else {
                LOGGER.debug("OCR is OK");
            }
        } catch (IOException e) {
            LOGGER.error("Error saving OCR " + e.toString());
        }
    }


}
