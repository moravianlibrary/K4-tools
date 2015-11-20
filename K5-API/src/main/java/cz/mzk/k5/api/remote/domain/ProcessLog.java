package cz.mzk.k5.api.remote.domain;

import java.util.Base64; // jen java 8

public class ProcessLog {

    private String sout;
    private String serr;
    private Base64.Decoder decoder = Base64.getDecoder();

    // TODO: log pravděpodobně není celý - dodělat offsety?
    public String getSout() {
        byte[] encodedBytes = decoder.decode(sout);
        return new String(encodedBytes);
//        return sout;
    }

    public void setSout(String sout) {
        this.sout = sout;
    }

    public String getSerr() {
        byte[] encodedBytes = decoder.decode(serr);
        return new String(encodedBytes);
//        return serr;
    }

    public void setSerr(String serr) {
        this.serr = serr;
    }

    public String toHtml() {
        String sout = getSout().replace("\n", "</br>");
        sout.replace("FINEST:", "<b>FINEST:</b>");
        sout.replace("FINER:", "<b>FINER:</b>");
        sout.replace("FINE:", "<b>FINE:</b>");
        sout.replace("CONFIG:", "<b>CONFIG:</b>");
        sout.replace("INFO:", "<b>INFO:</b>");
        sout.replace("WARNING:", "<b>WARNING:</b>");
        sout.replace("SEVERE:", "<font color=\"red\"><b>SEVERE:</b></font>");

        String serr = getSerr().replace("\n", "<br>");
        serr.replace("FINEST:", "<b>FINEST:</b>");
        serr.replace("FINER:", "<b>FINER:</b>");
        serr.replace("FINE:", "<b>FINE:</b>");
        serr.replace("CONFIG:", "<b>CONFIG:</b>");
        serr.replace("INFO:", "<b>INFO:</b>");
        serr.replace("WARNING:", "<b>WARNING:</b>");
        serr.replace("SEVERE:", "<font color=\"red\"><b>SEVERE:</b></font>");

        String html = "<h4>Process Log</h4><br>" + "<b>sout:<br></b> " + sout
                + "<br>" + "<b>serr:<br></b> " + serr + "<br>";
        return html;
    }

}
