package cz.mzk.k4.tools.scripts;

import cz.mzk.k4.tools.utils.Script;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rumanekm on 7.5.14.
 */

/**
 * Na audio serveru chybely konverze do mp3
 */
public class RegenerateAudioServer implements Script {

    private static final Logger LOGGER = LogManager.getLogger(RegenerateAudioServer.class);

    @Override
    public void run(List<String> args) {
        String path = args.get(0);
        Iterator<File> iterator = FileUtils.iterateFiles(new File(path), new SuffixFileFilter(".wav"), TrueFileFilter.INSTANCE);

        while (iterator.hasNext()) {
            File file = iterator.next();
            try {
                convertAudio(file);
            } catch (IOException e) {
                LOGGER.error("Konverze se nepodarila" + e.getStackTrace());
            }
        }
    }

    private void convertAudio(File file) throws IOException {
        String wavPath = file.getAbsolutePath();
        String newMp3Path = FilenameUtils.removeExtension(file.getAbsolutePath()) + ".mp3";

        List<String> cmdParams = new ArrayList<String>();
        cmdParams.add("ffmpeg");
        //param overwrite
        cmdParams.add("-y");
        cmdParams.add("-i");
        cmdParams.add(wavPath);
        cmdParams.add(newMp3Path);
        LOGGER.info("Spousti se konverze " + cmdParams.toString());
        Process process = new ProcessBuilder(cmdParams).start();

        try {
            process.waitFor();
            process.getErrorStream();
        } catch (InterruptedException e) {
            throw new IOException("Chyba pri prevodu: " + e.getMessage());
        }
    }

    @Override
    public String getUsage() {
        return "regenerujAudioServer cesta\n" +
                "Projde všechny soubory wav na cestě a dogeneruje k nim mp3.\n";
    }
}
