package cz.mzk.k4.tools.ocr.step;

import cz.mzk.k4.tools.ocr.domain.Ocr;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import java.util.List;

/**
 * Created by holmanj on 15.6.15.
 */

public class OcrWriter implements ItemWriter<Ocr> {

    private static final Logger LOGGER = Logger.getLogger(OcrWriter.class);

    private FedoraUtils fedoraUtils;

    public OcrWriter(FedoraUtils fedoraUtils) {
        this.fedoraUtils = fedoraUtils;
    }

    @Override
    public void write(List<? extends Ocr> items) throws CreateObjectException {
        for (Ocr ocr : items) {
            fedoraUtils.setOcr(ocr.getPid(), ocr.getOcrText());
            fedoraUtils.setAltoOcr(ocr.getPid(), ocr.getOcrAlto());
            LOGGER.info("Saved OCR of " + ocr.getPid());
        }
    }
}
