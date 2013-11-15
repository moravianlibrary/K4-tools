package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.utils.fedoraUtils.exception.CreateObjectException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 11/7/13
 */
public class RegenerateThumbnailPdf implements Script {

    private static final Logger LOGGER = Logger.getLogger(RegenerateThumbnailPdf.class);

    @Override
    public void run(List<String> args) {

        FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());

        try {
            String uuid = new String("uuid:980923a7-efb7-4dae-9b8d-f8d2c6b920e9");

//            String uuid = args.get(0);
//            if (!uuid.contains("uuid:")) {
//                LOGGER.error("problém problém");
//            }

            InputStream pdfStream = fedoraUtils.getPdf(uuid);
            File pdfFile = File.createTempFile("k4_tool", "thumbnail", FileUtils.getTempDirectory());
            FileUtils.copyInputStreamToFile(pdfStream, pdfFile);

            File thumbnailImgFile = new File("/home/holmanj/testPdf/nahled.jpg");

            // /usr/bin/convert -define pdf:use-cropbox=true -colorspace RGB {pdfFilePath}[0] -thumbnail x128 imgFilePath
            List<String> commandParams = new ArrayList<String>();
            commandParams.add("/usr/bin/convert");
            commandParams.add("-define");
            commandParams.add("pdf:use-cropbox=true");
            commandParams.add("-colorspace");
            commandParams.add("RGB");
            commandParams.add(pdfFile.getAbsolutePath() + "[0]");
            commandParams.add("-thumbnail");
            commandParams.add("x128");
            commandParams.add(thumbnailImgFile.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(commandParams);
            Process process = processBuilder.start();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                fedoraUtils.setThumbnail(uuid, thumbnailImgFile.getAbsolutePath());
            } catch (CreateObjectException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getUsage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}