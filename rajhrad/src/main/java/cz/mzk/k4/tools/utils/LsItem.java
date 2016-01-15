package cz.mzk.k4.tools.utils;

/**
 * Created by rumanekm on 2.5.14.
 */
public class LsItem {
    private String filename;
    private boolean isDirectory;

    public LsItem(String filename, boolean isDirectory) {
        this.filename = filename;
        this.isDirectory = isDirectory;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}
