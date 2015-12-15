package cz.mzk.k4.tools.domain;

import java.io.Serializable;

/**
 * Created by holmanj on 20.3.15.
 * pro načtení a serializaci údajů o straně
 */
public class Page implements Serializable {
    String title;
    String pid;
    String jpgImgName;
    String imageserverImgLocation;
    String djvuImgName;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getJpgImgName() {
        return jpgImgName;
    }

    public void setJpgImgName(String jpgImgName) {
        this.jpgImgName = jpgImgName;
    }

    public String getImageserverImgLocation() {
        return imageserverImgLocation;
    }

    public void setImageserverImgLocation(String imageserverImgLocation) {
        this.imageserverImgLocation = imageserverImgLocation;
    }

    public String getDjvuImgName() {
        return djvuImgName;
    }

    public void setDjvuImgName(String djvuImgName) {
        this.djvuImgName = djvuImgName;
    }
}
