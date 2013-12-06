package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.Script;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import cz.mzk.k4.tools.utils.exception.CreateObjectException;
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

    /**
     * Znovu vygeneruje náhled (thumbnail) PDF dokumentu a vloží ho do fedory.
     *
     * @param args - uuid PDF dokumentu (1. argument)
     */
    @Override
    public void run(List<String> args) {

        FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());
        String uuid;
        try {
            if (args.size() > 0) {
                uuid = args.get(0);
                if (!uuid.contains("uuid:")) {
                    System.out.println(getUsage());
                    return;
                }
            } else {
                System.out.println(getUsage());
                return;
            }

            InputStream pdfStream = fedoraUtils.getPdf(uuid);
            File pdfFile = File.createTempFile("k4_tool", "thumbnail", FileUtils.getTempDirectory());
            FileUtils.copyInputStreamToFile(pdfStream, pdfFile);

            File thumbnailImgFile = new File("/home/holmanj/testPdf/thumbnail.jpg");
            File previewImgFile = new File("/home/holmanj/testPdf/preview.jpg");

            // thumbnail
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
                process.getErrorStream();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // preview
            // /usr/bin/convert -define pdf:use-cropbox=true -colorspace RGB {pdfFilePath}[0] -thumbnail x500 imgFilePath
            commandParams = new ArrayList<String>();
            commandParams.add("/usr/bin/convert");
            commandParams.add("-define");
            commandParams.add("pdf:use-cropbox=true");
            commandParams.add("-colorspace");
            commandParams.add("RGB");
            commandParams.add(pdfFile.getAbsolutePath() + "[0]");
            commandParams.add("-thumbnail");
            commandParams.add("x500");
            commandParams.add(thumbnailImgFile.getAbsolutePath());

            processBuilder = new ProcessBuilder(commandParams);
            process = processBuilder.start();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                fedoraUtils.setThumbnail(uuid, thumbnailImgFile.getAbsolutePath());
                fedoraUtils.setPreview(uuid, thumbnailImgFile.getAbsolutePath());
            } catch (CreateObjectException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getUsage() {
        return "opraveniOdkazuProReplikaci - znovu vygeneruje náhled (thumbnail) PDF dokumentu a vloží ho do fedory\n" +
                "parametr: uuid dokumentu (vč. \"uuid:\")";
    }
}