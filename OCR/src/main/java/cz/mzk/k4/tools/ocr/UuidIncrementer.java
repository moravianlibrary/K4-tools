package cz.mzk.k4.tools.ocr;

import cz.mzk.k4.tools.utils.GeneralUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by holmanj on 29.7.16.
 */

public class UuidIncrementer implements JobParametersIncrementer {

    private static final String LISTFILE = "IO/ocr-list";
    private static final Logger LOGGER = Logger.getLogger(UuidIncrementer.class);


    public JobParameters getNext(JobParameters parameters) {
        LOGGER.debug("got job parameters: " + parameters);
        String nextUuid = getNextUuid(LISTFILE);
        if (nextUuid == null) {
            return null;
        }

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("rootPid", nextUuid);
        builder.addString("overwrite", parameters.getString("overwrite"));

        if (parameters==null || parameters.isEmpty()) {
            builder.addLong("run.id", 1L).toJobParameters();
        }
        long id = parameters.getLong("run.id",1L) + 1;
        builder.addLong("run.id", id);

        return builder.toJobParameters();
    }

    private String getNextUuid(String listFile) {
        String nextUuid;
        List<String> uuidList = GeneralUtils.loadUuidsFromFile(listFile);
        if (!uuidList.isEmpty()) {
            nextUuid = uuidList.get(0);
        } else {
            return null;
        }

        uuidList.remove(nextUuid);
        Path out = Paths.get(listFile);
        try {
            Files.write(out,uuidList, Charset.defaultCharset());
        } catch (IOException e) {
            LOGGER.error("Error while serializing uuid list");
            return null;
        }

        return nextUuid;
    }
}