package cz.mzk.k4.tools.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by secikd on 1/27/17.
 */
public class Publication implements Serializable {
    String title;
    String pid;
    List<Page> pages;

    public Publication (String title, String pid) {
        this.title = title;
        this.pid = pid;
        this.pages = new ArrayList<>();
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String pid) {
        this.title = title;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

}
