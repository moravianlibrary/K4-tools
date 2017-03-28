package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.FormatConvertor;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by holmanj on 3/19/14.
 */
public class PresunImgWorker {
    private static final Logger LOGGER = LogManager.getLogger(PresunImgWorker.class);
    private AccessProvider accessProvider;
    private FedoraUtils fedoraUtils;
    private boolean writeEnabled;

    public PresunImgWorker(boolean writeEnabled, AccessProvider accessProvider, FedoraUtils fedoraUtils) {
        this.accessProvider = accessProvider;
        this.fedoraUtils = fedoraUtils;
        this.writeEnabled = writeEnabled;
    }

    // dostane uuid na složku IE (číslo, monografie,..)
//    public void run(String topUuid, String topFolderName) {
    public void run(String pathsFileName, String imagesFolderName) {

        // nacist slozku s obrazkama
        File sourceFolder = new File(imagesFolderName);
        File[] imgFileList = sourceFolder.listFiles();
        List<File> imgFileArray = Arrays.asList(imgFileList);
        Collections.sort(imgFileArray);

        // nacist soubor s cestama
        try {
            BufferedReader br = new BufferedReader(new FileReader(pathsFileName));
            String line = br.readLine();
            int i = 0;
            while (line != null) {
                LOGGER.info((i+1) + ": " + imgFileArray.get(i).getName() + " -> " + line);
                InputStream convertedStream = FormatConvertor.convertTiffToJpg2(imgFileArray.get(i));
//                InputStream convertedStream = FormatConvertor.convertJpgToJpg2(imgFileArray.get(i));
                moveFile(convertedStream, line);
                line = br.readLine();
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void moveFile(InputStream oldFileInputStream, String newFilePath) {
        OutputStream outStream = null;

        try {
            File newFile = new File(newFilePath+".jp2");
            outStream = new FileOutputStream(newFile);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = oldFileInputStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            oldFileInputStream.close();
            outStream.close();

            LOGGER.info("File " + newFile.getAbsolutePath() + " is copied successfully!");

        } catch (IOException e) {
            LOGGER.error("Problém při kopírování skenu do místa: " + newFilePath);
            e.printStackTrace();
        }
    }
}

