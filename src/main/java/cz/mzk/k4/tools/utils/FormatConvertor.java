package cz.mzk.k4.tools.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
//        commandParams.add("-quality");
//        commandParams.add("80%");
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
        }
    }
}
