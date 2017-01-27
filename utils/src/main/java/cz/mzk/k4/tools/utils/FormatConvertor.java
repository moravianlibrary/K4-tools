package cz.mzk.k4.tools.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
            String errorOutput = IOUtils.toString(process.getErrorStream());

            if (process.waitFor() != 0 || jpgFile.length() == 0 || !errorOutput.equals("")){
                throw new IOException("Chyba při převodu Djvu na jpg: " + errorOutput);
            }

            return new FileInputStream(jpgFile);
        } catch (InterruptedException e) {
            throw new IOException("Chyba při převodu Djvu na jpg: " + e.getMessage());
        } finally {
            djvuFile.delete();
            jpgFile.delete();
        }
    }

    /**
     * Method converting djvu stream to tiff
     *
     * @param djvu Djvu input stream
     * @return Converted tiff file
     * @throws IOException If the conversion was unsuccessful
     */
    public static File convertDjvuToTiff(File djvu) throws IOException {

        //Create temporary file on disk
        File djvuTempFile = File.createTempFile("convertdjvu", ".djvu");
        File tiffFile = File.createTempFile("converttiff", ".tiff");
        FileUtils.copyFile(djvu, djvuTempFile);

        //Create terminal command
        List<String> cmdParams = new ArrayList<String>();
        cmdParams.add("ddjvu");
        cmdParams.add("-format=tiff");
        cmdParams.add(djvuTempFile.getAbsolutePath());
        cmdParams.add(tiffFile.getAbsolutePath());

        //Run the command
        Process process = new ProcessBuilder(cmdParams).start();

        try {
            String errorOutput = IOUtils.toString(process.getErrorStream());

            if (process.waitFor() != 0 || tiffFile.length() == 0 || !errorOutput.equals("")){
                throw new IOException("Chyba při převodu Djvu na tiff: " + errorOutput);
            }

            return tiffFile;
        } catch (InterruptedException e) {
            throw new IOException("Chyba při převodu djvu na tiff: " + e.getMessage());
        } finally {
            //Delete temporary files that are no longer needed
            djvuTempFile.delete();
        }
    }

    /**
     * Method decompressing tiff file
     *
     * @param tiff Tiff file to be decompressed
     * @return Decompressed tiff file
     * @throws IOException If the decompression was unsuccessful
     */
    public static File decompressTiff(File tiff) throws IOException {

        //Create temporary file on disk
        File decompressedTiffFile = File.createTempFile("decompress", ".tiff");

        //Create terminal command
        List<String> cmdParams = new ArrayList<String>();
        cmdParams.add("convert");
        cmdParams.add(tiff.getAbsolutePath());
        cmdParams.add("+compress");
        cmdParams.add(decompressedTiffFile.getAbsolutePath());

        //Run the command
        Process process = new ProcessBuilder(cmdParams).start();

        try {
            String errorOutput = IOUtils.toString(process.getErrorStream());

            if (process.waitFor() != 0 || decompressedTiffFile.length() == 0 || !errorOutput.equals("")){
                throw new IOException("Chyba při dekompresi tiffu: " + errorOutput);
            }

            return decompressedTiffFile;
        } catch (InterruptedException e) {
            throw new IOException("Chyba při dekompresi tiffu: " + e.getMessage());
        } finally {
            //Delete temporary files that are no longer needed
            tiff.delete();
        }
    }

    /**
     * Method converting decompressed tiff file into jpeg2000 file format
     *
     * @param decompTiff Tiff file to be converted
     * @return Input stream of jpeg2000 file
     * @throws IOException If the conversion was unsuccessful
     */
    public static InputStream convertTiffToJpg2(File decompTiff) throws IOException {

        //Create temporary files on disk
        File jp2File = File.createTempFile("final", ".jp2");

        //Create terminal command
        List<String> cmdParams = new ArrayList<String>();
        cmdParams.add("src/main/resources/djatoka/bin/compress.sh");
        cmdParams.add("src/main/resources/djatoka/");
        cmdParams.add(decompTiff.getAbsolutePath());
        cmdParams.add(jp2File.getAbsolutePath());

        //Run the command
        Process process = new ProcessBuilder(cmdParams).start();

        try {
            String errorOutput = IOUtils.toString(process.getErrorStream());

            if (process.waitFor() != 0 || jp2File.length() == 0 || !errorOutput.equals("")){
                throw new IOException("Chyba konverzniho skriptu (compress.sh) při převodu tiffu na jp2: " + errorOutput);
            }

            return new FileInputStream(jp2File);
        } catch (InterruptedException e) {
            throw new IOException("Chyba při převodu tiffu na jp2: " + e.getMessage());
        } finally {
            //Delete temporary files that are no longer needed
            decompTiff.delete();
            jp2File.delete();
        }
    }

    /**
     * Method converting djvu file to jpeg2000. The conversion to jpeg2000 is made through tiff file format.
     *
     * @param inputDjvu Djvu input stream to be converted
     * @return Input stream of converted file
     * @throws IOException If some part of conversion was unsuccessful
     */
    public static InputStream convertDjvuToJp2(File inputDjvu) throws IOException {

        File tiffConversionTempFiles = FormatConvertor.convertDjvuToTiff(inputDjvu);
        File decompressedTiffTempFiles = FormatConvertor.decompressTiff(tiffConversionTempFiles);
        return FormatConvertor.convertTiffToJpg2(decompressedTiffTempFiles);
    }

    /**
     * Method converting jpeg file to jpeg2000. The conversion to jpeg2000 is made through tiff file format.
     *
     * @param inputJpg jped input stream to be converted
     * @return Input stream of converted file
     * @throws IOException If some part of conversion was unsuccessful
     */
    public static InputStream convertJpgToJp2(File inputJpg) throws IOException {
        File tiffConversionTempFile = FormatConvertor.convertJpgToTiff(inputJpg);
        return FormatConvertor.convertTiffToJpg2(tiffConversionTempFile);
    }

    private static File convertJpgToTiff(File jpg) throws IOException {
        //Create temporary file on disk
        File jpgTempFile = File.createTempFile("convertjpg", ".jpg");
        File tiffFile = File.createTempFile("converttiff", ".tiff");
        FileUtils.copyFile(jpg, jpgTempFile);

        //Create terminal command
        List<String> cmdParams = new ArrayList<String>();
        cmdParams.add("convert");
        cmdParams.add("-compress");
        cmdParams.add("none");
        cmdParams.add(jpgTempFile.getAbsolutePath());
        cmdParams.add(tiffFile.getAbsolutePath());

        //Run the command
        Process process = new ProcessBuilder(cmdParams).start();

        try {
            String errorOutput = IOUtils.toString(process.getErrorStream());

            if (process.waitFor() != 0 || tiffFile.length() == 0 || !errorOutput.equals("")){
                throw new IOException("Chyba při převodu jpg na tiff: " + errorOutput);
            }

            return tiffFile;
        } catch (InterruptedException e) {
            throw new IOException("Chyba při převodu jpg na tiff: " + e.getMessage());
        } finally {
            //Delete temporary files that are no longer needed
            jpgTempFile.delete();
        }
    }
}
