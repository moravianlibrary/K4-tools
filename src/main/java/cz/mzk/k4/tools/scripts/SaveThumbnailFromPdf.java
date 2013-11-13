package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.workers.UuidWorker;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author: Martin Rumanek
 * @version: 11/7/13
 */
public class SaveThumbnailFromPdf extends UuidWorker {

    public SaveThumbnailFromPdf(boolean writeEnabled) {
        super(writeEnabled);
    }

    @Override
    public void run(String uuid) {

        try {
            byte[] pdf = FedoraUtils.getPdf(uuid);
            File pdfFile = File.createTempFile("k4_tool", "tmp", FileUtils.getTempDirectory());
            FileUtils.writeByteArrayToFile(pdfFile, pdf);

        } catch (IOException e) {

        }

    }
}
