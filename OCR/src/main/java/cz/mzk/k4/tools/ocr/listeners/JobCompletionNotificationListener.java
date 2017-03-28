package cz.mzk.k4.tools.ocr.listeners;

import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.common.K5ApiException;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;

/**
* Created by holmanj on 16.6.15.
*/

public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger LOGGER = LogManager.getLogger(JobCompletionNotificationListener.class);

    private ProcessRemoteApi krameriusApi;
    private FedoraUtils fedoraUtils;

    public JobCompletionNotificationListener(FedoraUtils fedoraUtils, ProcessRemoteApi krameriusApi) {
        this.fedoraUtils = fedoraUtils;
        this.krameriusApi = krameriusApi;
    }

    @Override
    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        // bude asi potřeba k získávání progresu
        // http://stackoverflow.com/questions/27153162/implement-a-spring-batch-progress-bar-get-total-row-count-on-job-execution
//        int total = fedoraUtils.getChildrenUuids(jobExecution.getJobParameters().getString("rootPid"), DigitalObjectModel.PAGE).size();
//        jobExecution.getExecutionContext().put("jobSize", total);
    }

    @Override
    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            String rootPid = jobExecution.getJobParameters().getString("rootPid");
            try {
                krameriusApi.reindexWithoutRemoving(rootPid);
            } catch (K5ApiException e) {
                LOGGER.error("Selhalo plánování reindexace dokumentu " + rootPid);
                LOGGER.error(e.getMessage());
            }
            LOGGER.info("Naplánována reindexace dokumentu " + rootPid);
        }
    }
}
