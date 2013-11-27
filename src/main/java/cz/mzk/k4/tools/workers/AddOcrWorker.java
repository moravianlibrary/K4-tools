package cz.mzk.k4.tools.workers;

import cz.mzk.k4.tools.utils.AbbyUtils;
import cz.mzk.k4.tools.utils.AccessProvider;
import cz.mzk.k4.tools.utils.FormatConvertor;
import cz.mzk.k4.tools.utils.fedoraUtils.FedoraUtils;
import cz.mzk.k4.tools.utils.fedoraUtils.exception.CreateObjectException;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author: Martin Rumanek
 * @version: 11/26/13
 */
public class AddOcrWorker extends UuidWorker {
    private FedoraUtils fedoraUtils = new FedoraUtils(new AccessProvider());
    private AbbyUtils abbyUtils = new AbbyUtils();

    public AddOcrWorker(boolean writeEnabled) {
        super(writeEnabled);
    }

    //TODO funguje jenom pro DJVU!
    @Override
    public void run(String uuid) {
        try {
            InputStream rawInputStream = fedoraUtils.getImgFull(uuid, "image/vnd.djvu");


            InputStream tiffInputStream = FormatConvertor.convertDjvuToJpg(rawInputStream);
            byte[] img = org.apache.commons.io.IOUtils.toByteArray(tiffInputStream);
            String[] ocr = abbyUtils.getOcr(img);
            try {
                fedoraUtils.setOcr(uuid, ocr[0]);
                fedoraUtils.setAltoOcr(uuid, ocr[1]);
            } catch (CreateObjectException e) {
                e.printStackTrace();
            }
            System.out.println("OCR to " + uuid + " was added");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
