package cz.mzk.k4.tools.ocr.listeners;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * Created by holmanj on 30.6.15.
 */
public class StepCompletionStatisticsListener implements StepExecutionListener {

    private static final Logger LOGGER = Logger.getLogger(JobCompletionNotificationListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
//        LOGGER.info(stepExecution.getSummary());
        LOGGER.info("OCR dokončeno");
        LOGGER.info("Bylo zpracováno celkem " + stepExecution.getReadCount() + " stran.");
        LOGGER.info("U " + stepExecution.getSkipCount() + " z nich došlo k chybě.");
        LOGGER.info(stepExecution.getFilterCount() + " stran bylo přeskočeno (už obsahují OCR i ALTO).");
        return stepExecution.getExitStatus();
    }
}
