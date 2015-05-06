package cz.mzk.k4.tools.scripts.lidovky;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 20.3.15.
 * pro načtení údajů o čísle LN z xml souboru na Hadovi
 */
public class Issue implements Serializable {
    String title;
    String pid;
    List<Page> pages; // title, page

    public Issue() {
        pages = new ArrayList<Page>();
    }

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

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
}
