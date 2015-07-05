package cz.mzk.k4.tools.ocr.step;

import cz.mzk.k4.tools.ocr.OcrApi.AbbyRestApi;
import cz.mzk.k4.tools.ocr.domain.Img;
import cz.mzk.k4.tools.ocr.domain.QueuedImage;
import cz.mzk.k4.tools.ocr.exceptions.ConflictException;
import cz.mzk.k4.tools.ocr.exceptions.InternalServerErroException;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.context.annotation.Scope;
import retrofit.mime.TypedFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by holmanj on 15.6.15.
 */

@Scope("Step")
public class ImgReader implements ItemReader<Img> {

    private static final Logger LOGGER = Logger.getLogger(ImgReader.class);
    private static final String JPEG_MIMETYPE = "image/jpeg";
    private static final String JPEG2000_MIMETYPE = "image/jp2";

    private FedoraUtils fedoraUtils;
    private AbbyRestApi abbyApi;

    private List<String> pagePids;

    public ImgReader(FedoraUtils fedoraUtils, AbbyRestApi abbyApi, String rootPid) {
        this.fedoraUtils = fedoraUtils;
        this.abbyApi = abbyApi;
        LOGGER.info("Spuštěno OCR na dokumentu " + rootPid);
        pagePids = fedoraUtils.getChildrenUuids(rootPid, DigitalObjectModel.PAGE);
        LOGGER.info("Načteno " + pagePids.size() + " stran");
    }

    @Override
    public Img read() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ConflictException, InternalServerErroException {

        String pagePid;
        if (!pagePids.isEmpty()) {
            pagePid = pagePids.remove(0);
        } else {
            return null; // konec
        }

        LOGGER.debug("Reading item " + pagePid);
        if (fedoraUtils.getOcr(pagePid) != null) {
            return new Img(pagePid, null); // strana už má OCR (filter - strana se dál nezpracovává)
        }

        String mimetype = JPEG_MIMETYPE;
        // stahuje IMG_FULL datastream -> jpeg a mnohem menší kvalita, než jpeg2000 na imageserveru
        InputStream imgStream = fedoraUtils.getImgFull(pagePid, mimetype);
        String md5 = sendImageToOcrEngine(imgStream, pagePid, mimetype);
        LOGGER.debug(md5);
        return new Img(pagePid, md5);
    }

    private String sendImageToOcrEngine(InputStream imgStream, String pagePid, String mimeType) throws IOException, ConflictException, InternalServerErroException {
        File temp = new File(pagePid + ".temp");
        FileUtils.copyInputStreamToFile(imgStream, temp);
        TypedFile fileToSend = new TypedFile(mimeType, temp);

        QueuedImage result = abbyApi.sendImageJpeg(fileToSend);

        // možná není potřeba - stav bude processing (event. done), nebo vyhodí výjimku
        // na druhou stranu v read listeneru je jen výjimka, ne item -> není přístup k uuid a md5 objektu
        if (!result.getState().equals(QueuedImage.STATE_PROCESSING)) {
            throw new IllegalStateException("Abby API vrátilo stav " + result.getState() + " se zprávou " + result.getMessage() + " u objektu " + pagePid);
        } else if (result.getId() == null || result.getId().equals("")) {
            throw new IllegalStateException("Abby API nevrátilo ID objektu " + pagePid);
        }

        LOGGER.debug("Strana " + pagePid + result.getId() + " byla odeslána na OCR server: " + result.getMessage());
        temp.delete();
        return result.getId();
    }
}
