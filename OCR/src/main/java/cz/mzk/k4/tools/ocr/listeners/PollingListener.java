package cz.mzk.k4.tools.ocr.listeners;

import cz.mzk.k4.tools.ocr.domain.Img;
import org.apache.log4j.Logger;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.annotation.OnProcessError;

/**
 * Created by holmanj on 29.6.15.
 */
public class PollingListener implements ItemProcessListener {

    private static final Logger LOGGER = Logger.getLogger(ItemProcessListener.class);

    @Override
    public void beforeProcess(Object item) {

    }

    @Override
    public void afterProcess(Object item, Object result) {

    }

    @Override
    @OnProcessError
    public void onProcessError(Object item, Exception e) {
        Img img = (Img) item;
        LOGGER.warn("Page " + img.getMd5() + " " + img.getPagePid() + " skipped on process: " + e.getMessage());
//        e.printStackTrace();
    }
}
