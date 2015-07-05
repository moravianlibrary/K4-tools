package cz.mzk.k4.tools.ocr.domain;

/**
 * Created by holmanj on 16.6.15.
 */
public class Ocr {

    private String ocrText;
    private String ocrAlto;
    private String pid;

    public Ocr( String pid, String ocrText, String ocrAlto) {
        this.ocrText = ocrText;
        this.ocrAlto = ocrAlto;
        this.pid = pid;
    }

    public String getOcrText() {
        return ocrText;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }

    public String getOcrAlto() {
        return ocrAlto;
    }

    public void setOcrAlto(String ocrAlto) {
        this.ocrAlto = ocrAlto;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
