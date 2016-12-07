package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.exceptions.OcrExistsExeption;
import cz.mzk.k4.tools.utils.AbbyUtils;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.fedora.Constants;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.utils.FormatConvertor;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import org.apache.log4j.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author: Martin Rumanek
 * @version: 11/26/13
 */
public class AddOcrWorker extends UuidWorker {
    private static final Logger LOGGER = Logger.getLogger(AddOcrWorker.class);
    FedoraUtils fedoraUtils;
    AbbyUtils abbyUtils;

    public AddOcrWorker(boolean writeEnabled) throws FileNotFoundException {
        super(writeEnabled);
        fedoraUtils = new FedoraUtils(new AccessProvider());
        abbyUtils = new AbbyUtils();
    }

    @Override
    public void run(String uuid) throws OcrExistsExeption {
        try {
            String mimetype = fedoraUtils.getMimeTypeForStream(uuid, Constants.DATASTREAM_ID.IMG_FULL.getValue());
            InputStream rawInputStream = fedoraUtils.getImgFull(uuid, mimetype);
            if (fedoraUtils.getOcr(uuid) == null) {
                InputStream jpgInputStream;
                if ("image/vnd.djvu".equals(mimetype)) {
                    jpgInputStream = FormatConvertor.convertDjvuToJpg(rawInputStream);
                } else {
                    jpgInputStream = rawInputStream;
                }
                byte[] img = org.apache.commons.io.IOUtils.toByteArray(jpgInputStream);
                LOGGER.debug("Page " + uuid + " is being sent to OCR server");
                String[] ocr = abbyUtils.getOcr(img);
                try {
                    fedoraUtils.setOcr(uuid, ocr[0]);
                    fedoraUtils.setAltoOcr(uuid, ocr[1]);
                    LOGGER.debug("OCR to " + uuid + " was added");
                } catch (CreateObjectException e) {
                    LOGGER.error("CreateObject exception " + uuid);
                }
            } else {
                throw new OcrExistsExeption("Page " + uuid + " already has OCR");
            }

        } catch (IOException e) {
            LOGGER.error("IO error " + uuid);
        }
    }
}
