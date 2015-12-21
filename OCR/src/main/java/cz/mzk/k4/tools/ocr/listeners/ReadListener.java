package cz.mzk.k4.tools.ocr.listeners;

import cz.mzk.k4.tools.ocr.step.ImgReader;
import org.apache.log4j.Logger;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.annotation.OnReadError;

/**
 * Created by holmanj on 29.6.15.
 */
public class ReadListener implements ItemReadListener {

    private static final Logger LOGGER = Logger.getLogger(ImgReader.class);

    @Override
    public void beforeRead() {

    }

    @Override
    public void afterRead(Object item) {

    }

    @Override
    @OnReadError
    public void onReadError(Exception ex) {
        LOGGER.warn("Page skipped on read: " + ex.getMessage());
//        ex.printStackTrace();
    }
}
