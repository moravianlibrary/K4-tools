package cz.mzk.k4.tools.ocr.listeners;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by holmanj on 30.6.15.
 */
@Component
public class StepCompletionStatisticsListener extends StepExecutionListenerSupport {

    private static final Logger LOGGER = Logger.getLogger(StepCompletionStatisticsListener.class);

    @Override
    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
//        LOGGER.info(stepExecution.getSummary());
        LOGGER.info("OCR dokončeno");
        LOGGER.info("Bylo zpracováno celkem " + stepExecution.getReadCount() + " stran.");
        LOGGER.info("U " + stepExecution.getSkipCount() + " z nich došlo k chybě.");
        LOGGER.info(stepExecution.getFilterCount() + " stran bylo přeskočeno (už obsahují OCR i ALTO).");

        try {
            Path resultFile = Paths.get("IO/results");
            Files.write(resultFile, "OCR dokončeno\n".getBytes(), StandardOpenOption.APPEND);
            Files.write(resultFile, ("Bylo zpracováno celkem " + stepExecution.getReadCount() + " stran.\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(resultFile, ("U " + stepExecution.getSkipCount() + " z nich došlo k chybě.\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(resultFile, (stepExecution.getFilterCount() + " stran bylo přeskočeno (už obsahují OCR i ALTO).\n\n").getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        return stepExecution.getExitStatus();
    }
}
