package cz.mzk.k4.tools.utils;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Martin Rumanek
 * @version: 11/26/13
 */
public class FormatConvertor {


    public static InputStream convertDjvuToJpg(InputStream djvu) throws IOException {
        File djvuFile = File.createTempFile("convert", ".djvu");
        File jpgFile = File.createTempFile("convert", ".jpg");
        FileUtils.copyInputStreamToFile(djvu, djvuFile);

        List<String> commandParams = new ArrayList<String>();
        commandParams.add("convert");
        commandParams.add("-quality");
        commandParams.add("90%");
        commandParams.add(djvuFile.getAbsolutePath());
        commandParams.add(jpgFile.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder(commandParams);
        Process process = processBuilder.start();
        try {
            process.waitFor();
            process.getErrorStream();
            InputStream is = new FileInputStream(jpgFile);

            return is;
        } catch (InterruptedException e) {
            throw new IOException();
        } finally {
            djvuFile.delete();
            jpgFile.delete();
        }
    }
}
