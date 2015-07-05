package cz.mzk.k4.tools.ocr.domain;

/**
 * Created by holmanj on 15.6.15.
 */
public class Img {

    public Img(String pid, String md5) {
        setPagePid(pid);
        setMd5(md5);
    }

    private String pagePid;
    private String md5;

    public String getPagePid() {
        return pagePid;
    }

    public void setPagePid(String pagePid) {
        this.pagePid = pagePid;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
