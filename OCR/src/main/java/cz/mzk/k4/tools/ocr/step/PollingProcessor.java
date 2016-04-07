package cz.mzk.k4.tools.ocr.step;

import cz.mzk.k4.tools.ocr.OcrApi.AbbyRestApi;
import cz.mzk.k4.tools.ocr.domain.Img;
import cz.mzk.k4.tools.ocr.domain.Ocr;
import cz.mzk.k4.tools.ocr.domain.QueuedImage;
import cz.mzk.k4.tools.ocr.exceptions.BadRequestException;
import cz.mzk.k4.tools.ocr.exceptions.InternalServerErroException;
import cz.mzk.k4.tools.ocr.exceptions.ItemNotFoundException;
import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import java.io.IOException;

/**
 * Created by holmanj on 16.6.15.
 */

public class PollingProcessor implements ItemProcessor<Img, Ocr> {

    private static final Logger LOGGER = Logger.getLogger(PollingProcessor.class);

    private AbbyRestApi abbyApi;

    public PollingProcessor(AbbyRestApi abbyApi) {
        this.abbyApi = abbyApi;
    }

    @Override
    public Ocr process(final Img image) throws BadRequestException, ItemNotFoundException, InterruptedException, InternalServerErroException, IOException {

        if (image.getMd5() == null) {
            LOGGER.debug("Strana " + image.getPagePid() + " už má OCR i ALTO"); // filter
            return null;
        }

        QueuedImage result = pollOcr(image);

        // získávání neexistujícího OCR se projeví chybou retrofit: nejde cast String na QueuedImage
        // AbbyRestErrorHandler očekává QueuedImage, ale getOCR vrací String (viz StringConverter)
        String textOcr = abbyApi.getOCR("txt", result.getId());
        String altoOcr = abbyApi.getOCR("alto", result.getId());
        final Ocr pageOcr = new Ocr(image.getPagePid(), textOcr, altoOcr);
        abbyApi.deleteItem(result.getId());

        LOGGER.debug("Page " + image.getPagePid() + image.getMd5() + " OCR processed.");
        return pageOcr;
    }

    private QueuedImage pollOcr(Img image) throws InterruptedException, BadRequestException, ItemNotFoundException, InternalServerErroException {
        boolean poll = true;
        QueuedImage result = null;
        while (poll) {
                result = abbyApi.pollOcrState(image.getMd5());
                poll = result.getState().equals(QueuedImage.STATE_PROCESSING);
                LOGGER.debug(result.getMessage() + "\n" +
                        "polling " + image.getPagePid() + " " + image.getMd5());
                Thread.sleep(5000);
        }
        LOGGER.debug(result.getMessage());
        return result;
    }
}
