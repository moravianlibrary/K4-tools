package cz.mzk.k4.tools.ocr.step;

import cz.mzk.k4.tools.ocr.domain.Img;
import cz.mzk.k4.tools.ocr.domain.Ocr;
import cz.mzk.k4.tools.ocr.exceptions.BadRequestException;
import cz.mzk.k4.tools.ocr.exceptions.InternalServerErroException;
import cz.mzk.k4.tools.ocr.exceptions.ItemNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;

import java.io.IOException;

/**
 * Created by holmanj on 16.6.15.
 */

public class MockProcessor implements ItemProcessor<Img, Ocr> {

    private static final Logger LOGGER = LogManager.getLogger(MockProcessor.class);

    public MockProcessor() {
    }

    @Override
    public Ocr process(final Img image) throws BadRequestException, ItemNotFoundException, InterruptedException, InternalServerErroException, IOException {

        LOGGER.info("Processing document " + image.getPagePid());
        return new Ocr(image.getPagePid(), null, null);
    }
}
