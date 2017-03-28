package cz.mzk.k4.tools.ocr.step;

import cz.mzk.k4.tools.ocr.domain.Ocr;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * Created by holmanj on 15.6.15.
 */

public class MockWriter implements ItemWriter<Ocr> {

    private static final Logger LOGGER = LogManager.getLogger(MockWriter.class);

    public MockWriter() {
    }

    @Override
    public void write(List<? extends Ocr> items) throws CreateObjectException {
        LOGGER.info("Writing document " + items.get(0).getPid());
    }
}
