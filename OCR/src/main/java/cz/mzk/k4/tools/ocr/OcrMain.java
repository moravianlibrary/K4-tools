package cz.mzk.k4.tools.ocr;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by holmanj on 12.6.15.
 */

@ComponentScan
@EnableAutoConfiguration
public class OcrMain {
    // spouštění s 1 povinným parametrem: uuid dokumentu/stromu a 1 volitelným: "overwrite" pro nepřeskakování stran s OCR a dělání všeho znova
    public static void main(String[] args) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("rootPid", args[0]);
        if (args.length >= 2) {
            jobParametersBuilder.addString("overwrite", args[1]);
        }
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();

        SpringApplication app = new SpringApplication(BatchConfiguration.class);
        app.setWebEnvironment(false);
        ConfigurableApplicationContext ctx = app.run(args);
        JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);
        jobLauncher.run(ctx.getBean("job", Job.class), jobParameters);

        System.exit(0);
    }
}
