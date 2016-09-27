package cz.mzk.k4.tools.ocr;

import cz.mzk.k4.tools.ocr.OcrApi.AbbyRestApi;
import cz.mzk.k4.tools.ocr.OcrApi.AbbyRestApiFactory;
import cz.mzk.k4.tools.ocr.domain.Img;
import cz.mzk.k4.tools.ocr.domain.Ocr;
import cz.mzk.k4.tools.ocr.exceptions.BadRequestException;
import cz.mzk.k4.tools.ocr.exceptions.ConflictException;
import cz.mzk.k4.tools.ocr.exceptions.InternalServerErroException;
import cz.mzk.k4.tools.ocr.exceptions.ItemNotFoundException;
import cz.mzk.k4.tools.ocr.listeners.JobCompletionNotificationListener;
import cz.mzk.k4.tools.ocr.listeners.PollingListener;
import cz.mzk.k4.tools.ocr.listeners.ReadListener;
import cz.mzk.k4.tools.ocr.listeners.StepCompletionStatisticsListener;
import cz.mzk.k4.tools.ocr.step.ImgReader;
import cz.mzk.k4.tools.ocr.step.OcrWriter;
import cz.mzk.k4.tools.ocr.step.PollingProcessor;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory;
import cz.mzk.k5.api.remote.ProcessRemoteApi;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit.converter.ConversionException;

import java.io.FileNotFoundException;

/**
 * Created by holmanj on 12.6.15.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private AccessProvider accessProvider = AccessProvider.getInstance();
    private ProcessRemoteApi krameriusApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(
            accessProvider.getKrameriusHost(),
            accessProvider.getKrameriusUser(),
            accessProvider.getKrameriusPassword());
    private FedoraUtils fedoraUtils = new FedoraUtils(accessProvider);
    private AbbyRestApi abbyApi = AbbyRestApiFactory.getAbbyRestApi("localhost:8085/AbbyyRest/ocr"); // s tunelem na docker
//    private static AbbyRestApi abbyApi = AbbyRestApiFactory.getAbbyRestApi("localhost:9090/AbbyyRest/ocr"); // na localhostu

    // kvůli vstupu rootPid v readeru (konstruktor očekává string)
    public static final String NAHRAZENO_V_KONSTRUKTORU = "nahrazeno v konstruktoru";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private JobBuilderFactory jobs;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private StepBuilderFactory steps;

    public BatchConfiguration() throws FileNotFoundException {
    }

    @Bean
    @StepScope
    public ItemReader<Img> reader(@Value("#{jobParameters[rootPid]}") String rootPid, @Value("#{jobParameters[overwrite]}") String overwrite) {
        boolean overwriteOcr = "overwrite".equals(overwrite);
        return new ImgReader(fedoraUtils, abbyApi, rootPid, overwriteOcr);
//        return new MockReader(rootPid);
    }

    @Bean
    public ItemProcessor<Img, Ocr> processor() {
        return new PollingProcessor(abbyApi);
//        return new MockProcessor();
    }

    @Bean
    public ItemWriter<Ocr> writer() {
        return new OcrWriter(fedoraUtils);
//        return new MockWriter();
    }

    @Bean
    public JobCompletionNotificationListener jobCompletionListener() {
        return new JobCompletionNotificationListener(fedoraUtils, krameriusApi);
    }

    @Bean
    public ItemReadListener readListener() {
        return new ReadListener();
    }

    @Bean
    public ItemProcessListener processListener() {
        return new PollingListener();
    }

    @Bean
    public StepListener statisticsListener() {
        return new StepCompletionStatisticsListener();
    }

    @Bean
    public JobParametersIncrementer jobIncrementer() {
        return new UuidIncrementer();
    }

    @Bean
    public Step step() {
        return steps.get("step")
                .<Img, Ocr>chunk(1) // počet najednou zpracovávaných stran
                .reader(reader(NAHRAZENO_V_KONSTRUKTORU, NAHRAZENO_V_KONSTRUKTORU))
                .processor(processor())
                .writer(writer())
                .listener(readListener())
                .listener(processListener())
                .faultTolerant()
                .skipLimit(20) // maximální počet chyb během zpracovávání dokumentu
                .skip(BadRequestException.class)
                .skip(ConflictException.class)
                .skip(InternalServerErroException.class)
                .skip(ItemNotFoundException.class)
                .skip(IllegalStateException.class)
                .skip(CreateObjectException.class)
                .skip(ClassCastException.class) // pro případy java.lang.String cannot be cast to cz.mzk.k4.tools.ocr.domain.QueuedImage
                .skip(ConversionException.class)
                .listener(statisticsListener())
                .build();
    }

    @Bean
    public Job job(Step step) throws Exception {
        return jobs.get("job")
                .listener(jobCompletionListener())
                .incrementer(jobIncrementer())
                .flow(step)
                .end()
                .build();
    }
}
