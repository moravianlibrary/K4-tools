package cz.mzk.k4.tools.scripts.lidovky;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holmanj on 20.3.15.
 * pro načtení údajů o ročníku LN z xml souboru na Hadovi
 */
public class Volume implements Serializable {
    String xmlFileName;
    String year;
    String pid;
    List<Issue> issues; // title, issue

    public Volume() {
        issues = new ArrayList<>();
    }

    public String getXmlFileName() {
        return xmlFileName;
    }

    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }
}
