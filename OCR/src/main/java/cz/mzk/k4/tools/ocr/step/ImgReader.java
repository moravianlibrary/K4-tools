package cz.mzk.k4.tools.ocr.step;

import cz.mzk.k4.tools.ocr.OcrApi.AbbyRestApi;
import cz.mzk.k4.tools.ocr.domain.Img;
import cz.mzk.k4.tools.ocr.domain.QueuedImage;
import cz.mzk.k4.tools.ocr.exceptions.ConflictException;
import cz.mzk.k4.tools.ocr.exceptions.InternalServerErroException;
import cz.mzk.k4.tools.utils.domain.DigitalObjectModel;
import cz.mzk.k4.tools.utils.fedora.FedoraUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.context.annotation.Scope;
import retrofit.mime.TypedFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by holmanj on 15.6.15.
 */

@Scope("Step")
public class ImgReader implements ItemReader<Img> {

    private static final Logger LOGGER = Logger.getLogger(ImgReader.class);
    private static final String JPEG_MIMETYPE = "image/jpeg";
    private static final String JPEG2000_MIMETYPE = "image/jp2";
    private FedoraUtils fedoraUtils;
    private AbbyRestApi abbyApi;
    private boolean overwrite;
    private List<String> pagePids;
    private List<String> kopie; // pro mazání stran z OCR
    private String rootPid; // pro mazání stran z OCR

    public ImgReader(FedoraUtils fedoraUtils, AbbyRestApi abbyApi, String rootPid, boolean overwrite) {
        this.fedoraUtils = fedoraUtils;
        this.abbyApi = abbyApi;
        this.overwrite = overwrite;
        this.rootPid = rootPid;
        LOGGER.info("Spuštěno OCR na dokumentu " + rootPid);
        try {
            Path resultFile = Paths.get("IO/results");
            Files.write(resultFile, ("Spuštěno OCR na dokumentu " + rootPid + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String serializedFile = "IO/" + rootPid + ".ser";
        if (new File(serializedFile).exists()) {
            try {
                pagePids = deserialize(serializedFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            pagePids = fedoraUtils.getChildrenUuids(rootPid, DigitalObjectModel.PAGE);
            pagePids = makeUnique(pagePids);
            serialize(pagePids, serializedFile);
        }

        kopie = new ArrayList(pagePids);
        LOGGER.info("Načteno " + pagePids.size() + " stran");
        try {
            Path resultFile = Paths.get("IO/results");
            Files.write(resultFile, ("Načteno " + pagePids.size() + " stran\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Img read() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ConflictException, InternalServerErroException {

        String pagePid;
        if (!pagePids.isEmpty()) {
            pagePid = pagePids.remove(0);
        } else {
            return null; // konec
        }

        LOGGER.debug("Reading item " + pagePid);
            if (fedoraUtils.getOcr(pagePid) != null && fedoraUtils.getAlto(pagePid) != null && !overwrite) {
                // odstranit ze seznamu
                kopie.remove(pagePid);
                // serializace
                serialize(kopie, "IO/" + rootPid + ".ser");
                return new Img(pagePid, null); // strana už má OCR i ALTO (filter - strana se dál nezpracovává)
            }

        String mimetype;
        InputStream imgStream;

//        try {
//            mimetype = JPEG2000_MIMETYPE;
//            LOGGER.debug("Trying to get JP2 of " + pagePid);
//            imgStream = fedoraUtils.getImgJp2(pagePid);
//        } catch (NullPointerException ex) {
            // stahuje IMG_FULL datastream -> jpeg a menší kvalita než jpeg2000 na imageserveru
            mimetype = JPEG_MIMETYPE;
            LOGGER.debug("Trying to get JPG of " + pagePid);
            imgStream = fedoraUtils.getImgFull(pagePid, mimetype);
//        }

        String md5;
        try {
            LOGGER.debug("Trying to send " + pagePid + " to OCR server");
            md5 = sendImageToOcrEngine(imgStream, pagePid, mimetype);
        } catch (NullPointerException ex) {
            LOGGER.error("Nepodařilo se odeslat stranu " + pagePid);
            throw  ex;
        }

        LOGGER.debug(md5);
        return new Img(pagePid, md5);
    }

    private String sendImageToOcrEngine(InputStream imgStream, String pagePid, String mimeType) throws IOException, ConflictException, InternalServerErroException {
        File temp = new File(pagePid + ".temp");
        FileUtils.copyInputStreamToFile(imgStream, temp);
        TypedFile fileToSend = new TypedFile(mimeType, temp);

        QueuedImage result;
        if (mimeType.equals(JPEG2000_MIMETYPE)) {
            result = abbyApi.sendImageJp2(fileToSend);
        } else {
            result = abbyApi.sendImageJpeg(fileToSend);
        }

        // možná není potřeba - stav bude processing (event. done), nebo vyhodí výjimku
        // na druhou stranu v read listeneru je jen výjimka, ne item -> není přístup k uuid a md5 objektu
        if (!result.getState().equals(QueuedImage.STATE_PROCESSING)) {
            throw new IllegalStateException("Abby API vrátilo stav " + result.getState() + " se zprávou " + result.getMessage() + " u objektu " + pagePid);
        } else if (result.getId() == null || result.getId().equals("")) {
            throw new IllegalStateException("Abby API nevrátilo ID objektu " + pagePid);
        }

        LOGGER.debug("Strana " + pagePid + result.getId() + " byla odeslána na OCR server: " + result.getMessage());
        temp.delete();
        return result.getId();
    }

    private List makeUnique(List pagePids) {
        Set<String> hashSet = new HashSet<>(pagePids);
        pagePids.clear();
        pagePids.addAll(hashSet);
        return pagePids;
    }

    private void serialize(List<String> lidovky, String filename) {
        FileOutputStream fileOut = null;
        ObjectOutputStream out = null;
        try {
            fileOut = new FileOutputStream(filename);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(lidovky);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> deserialize(String filename) throws FileNotFoundException {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        List<String> lidovky = null;
        try {
            fileIn = new FileInputStream(filename);
            in = new ObjectInputStream(fileIn);
            lidovky = (List<String>) in.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new FileNotFoundException();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                fileIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lidovky;
    }

}
