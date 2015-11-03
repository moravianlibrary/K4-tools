package cz.mzk.k4.tools.ocr.listeners;

import cz.mzk.k4.tools.utils.KrameriusUtils;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
* Created by holmanj on 16.6.15.
*/

public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger LOGGER = Logger.getLogger(JobCompletionNotificationListener.class);

    private KrameriusUtils krameriusUtils;
    private FedoraUtils fedoraUtils;

    public JobCompletionNotificationListener(FedoraUtils fedoraUtils, KrameriusUtils krameriusUtils) {
        this.fedoraUtils = fedoraUtils;
        this.krameriusUtils = krameriusUtils;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // bude asi potřeba k získávání progresu
        // http://stackoverflow.com/questions/27153162/implement-a-spring-batch-progress-bar-get-total-row-count-on-job-execution
//        int total = fedoraUtils.getChildrenUuids(jobExecution.getJobParameters().getString("rootPid"), DigitalObjectModel.PAGE).size();
//        jobExecution.getExecutionContext().put("jobSize", total);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            LOGGER.info("OCR dokončeno");
            String rootPid = jobExecution.getJobParameters().getString("rootPid");
            krameriusUtils.reindex(rootPid);
            LOGGER.info("Naplánována reindexace dokumentu " + rootPid);
        }
    }
}
