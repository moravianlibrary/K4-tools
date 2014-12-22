package cz.mzk.k4.tools.utils;

import com.abby.recognitionserver3.FileContainer;
import com.abby.recognitionserver3.OutputDocument;
import com.abby.recognitionserver3.RSSoapService;
import com.abby.recognitionserver3.RSSoapServiceSoap;
import com.abby.recognitionserver3.XmlResult;

import java.io.UnsupportedEncodingException;

/**
* @author: Martin Rumanek
* @version: 11/26/13
*/
public class AbbyUtils {
    public String[] getOcr(byte[] file) {
        String ocrAlto = null;
        String ocrTxt = null;
        RSSoapService service = new RSSoapService();
        RSSoapServiceSoap soap = service.getRSSoapServiceSoap();
        FileContainer data = new FileContainer();
        data.setFileContents(file);
        XmlResult result = soap.processFile("localhost", "meditor", data);

        if (result.isIsFailed()) {
            //TODO
        }

        for (OutputDocument document :result.getInputFiles().getInputFile().get(0).getOutputDocuments().getOutputDocument()) {
            if ("OFF_ALTO".equals(document.getFormatSettings().getFileFormat().toString())) {
                try {
                    ocrAlto = new String(document.getFiles().getFileContainer().get(0).getFileContents(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if ("OFF_TEXT".equals(document.getFormatSettings().getFileFormat().toString())) {
                try {
                    ocrTxt = new String(document.getFiles().getFileContainer().get(0).getFileContents(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        return new String[] {ocrTxt, ocrAlto};
    }
}
